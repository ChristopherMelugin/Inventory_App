package com.cmelugin.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class Database extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "inventory.db";
    private static final int VERSION = 1;
    private static Database mItemDb;


    public static Database getInstance(Context context) {
        if (mItemDb == null) {
            mItemDb = new Database(context);
        }
        return mItemDb;
    }

    public Database(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    // Define tables
    private static final class InventoryTable {
        private static final String TABLE = "items";
        private static final String COL_ID = "_id";
        private static final String COL_NAME = "name";
        private static final String COL_QTY = "quantity";
        private static final String COL_LOW_NOTIFY = "notify";
        private static final String COL_USERNAME = "username";
    }

    private static final class LoginTable {
        private static final String TABLE = "users";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
    }

    private static final class TagTable {
        private static final String TABLE = "tags";
        private static final String TAG_ID = "_id";
        private static final String TAG_NAME = "tag";
        private static final String TAG_USERNAME = "username";
    }

    private static final class ItemTagMappingTable {
        private static final String TABLE = "map";
        private static final String ITEM_REFERENCE = "item";
        private static final String TAG_REFERENCE = "tag";
    }

    // Build tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "
                + InventoryTable.TABLE + " ("
                + InventoryTable.COL_ID + " integer primary key autoincrement, "
                + InventoryTable.COL_NAME + ", "
                + InventoryTable.COL_QTY + " int, "
                + InventoryTable.COL_LOW_NOTIFY + " int, "
                + InventoryTable.COL_USERNAME + " )");

        db.execSQL("create table "
                + LoginTable.TABLE + " ("
                + LoginTable.COL_USERNAME + " primary key, "
                + LoginTable.COL_PASSWORD + ")");

        db.execSQL("create table "
                + TagTable.TABLE + " ("
                + TagTable.TAG_ID + " integer primary key autoincrement, "
                + TagTable.TAG_NAME + ", "
                + TagTable.TAG_USERNAME + ")");

        db.execSQL("create table "
                + ItemTagMappingTable.TABLE + " ("
                + ItemTagMappingTable.ITEM_REFERENCE + " integer, "
                + ItemTagMappingTable.TAG_REFERENCE + " integer )");

    }

    // Call to add username and password to database
    public int addUser(LoginValue user) {
        SQLiteDatabase db = getWritableDatabase();

        // Check and see if username exists
        String sql = "select * from " + LoginTable.TABLE + " where " + LoginTable.COL_USERNAME + " = ?";
        Cursor cursor = (db.rawQuery(sql, new String[] { user.getUsername() }));

        if  (cursor.getCount() == 0) {
            // If the query returns 0 count then the username doesn't exist and can be added
            ContentValues values = new ContentValues();
            values.put(LoginTable.COL_USERNAME, user.getUsername());
            values.put(LoginTable.COL_PASSWORD, user.getPassword());
            db.insert(LoginTable.TABLE, null, values);
            return 0;
        }
        else {
            return 1;
        }
    }

    // Reads item data from database and inserts it into an array list
    public List<InventoryItem> getInventoryItems(String username) {
        List<InventoryItem> items = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "select * from " + InventoryTable.TABLE + " where " + InventoryTable.COL_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { username });

        if(cursor.moveToFirst()) {
            do {
                InventoryItem item = new InventoryItem();
                item.setId(cursor.getInt(0));
                item.setTitle(cursor.getString(1));
                item.setQuantity(cursor.getInt(2));
                item.setNotifyOnLow(cursor.getInt(3));
                item.setUsername(cursor.getString(4));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    // Reads tag data from database and inserts it into an array list
    public List<Tag> getTags(String username) {
        List<Tag> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TagTable.TABLE+ " where " + TagTable.TAG_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { username });

        if(cursor.moveToFirst()) {
            do {
                Tag tag = new Tag();
                tag.setId(cursor.getInt(0));
                tag.setTag(cursor.getString(1));
                tag.setUsername(cursor.getString(2));
                tags.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + InventoryTable.TABLE);
        onCreate(db);
    }

    // Call when adding a new tag in the add tag activity
    public void addTag(Tag tag) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TagTable.TAG_NAME, tag.getTag());
        values.put(TagTable.TAG_USERNAME, tag.getUsername());
        db.insert(TagTable.TABLE, null, values);
    }

    // call when all fields are full in the add item activity
    public void addItem(InventoryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryTable.COL_NAME, item.getTitle());
        values.put(InventoryTable.COL_QTY, item.getQuantity());
        values.put(InventoryTable.COL_LOW_NOTIFY, item.getNotifyOnLow());
        values.put(InventoryTable.COL_USERNAME, item.getUsername());
        db.insert(InventoryTable.TABLE, null, values);
    }

    // Call to update the quantity in the database
    public void updateQuantity(int itemId, int newTotal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues newQuantity = new ContentValues();
        newQuantity.put(InventoryTable.COL_QTY, newTotal);
        db.update(InventoryTable.TABLE, newQuantity, InventoryTable.COL_ID + " = " + itemId, null);
    }

    // Call to update the name of an item in the database
    public void updateItemName(int itemId, String updatedName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues newName = new ContentValues();
        newName.put(InventoryTable.COL_NAME, updatedName);
        db.update(InventoryTable.TABLE, newName, InventoryTable.COL_ID + " = " + itemId, null);
    }

    // Call to delete an item in the database
    public void deleteItem(InventoryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(InventoryTable.TABLE,InventoryTable.COL_ID + " = " + item.getId(), null);
    }

    // Check to see if username and password match
    public boolean checkCredentials(LoginValue user) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from " + LoginTable.TABLE + " where " + LoginTable.COL_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { user.getUsername()});
        if (cursor.moveToFirst()) {
            String storedPass = cursor.getString(1);
            cursor.close();

            if (storedPass.equals(user.getPassword())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
