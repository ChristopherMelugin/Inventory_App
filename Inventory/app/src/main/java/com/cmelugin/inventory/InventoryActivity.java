package com.cmelugin.inventory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "com.cmelugin.inventory.loginvalue";
    public static final String CHANNEL_ID = "channel_low_stock";
    private RecyclerViewAdapter mAdapter;
    private Database mDb;
    private String mUsername;
    private RecyclerView recyclerView;
    private int mSelectedInventoryPosition = RecyclerView.NO_POSITION;
    private InventoryItem mInventoryItem;
    private boolean sAbc, sQty;

    // Popup declarations
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText popup_item_name, popup_item_qty;
    private Button save_mods;

    public Menu menu;
    private long tagId;
    private String filter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(EXTRA_USERNAME);
        setupAdapter();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.invToolbar);
        setActionBar(mToolbar);
        String title = mUsername + "'s Inventory";
        getActionBar().setTitle(title);
        onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.clear();
        setupAdapter();
    }

    // Build the adapter. Also used to refresh inventory items.
    public void setupAdapter() {
        mDb = Database.getInstance(getApplicationContext());
        recyclerView = findViewById(R.id.grid_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        mAdapter = new RecyclerViewAdapter(loadInventory(mUsername, filter));
        recyclerView.setAdapter(mAdapter);
    }

    private List<InventoryItem> loadInventory(String username, String filter) {
        List<InventoryItem> items;
        if (filter == null) {
            items = mDb.getInventoryItems(username);
        }
        else {
            items = mDb.getFilteredInventoryItems(username, filter);
        }
        if (sAbc == true) {
            Collections.sort(items, new compareTitles());
        }
        else if(sQty == true) {
            Collections.sort(items, new compareQty());
        }
        return items;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.invToolbar);
        mToolbar.inflateMenu(R.menu.main_menu);
        mToolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onOptionsItemSelected(item);
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.manage_tags:
                Intent intent = new Intent(this, AddTagActivity.class);
                intent.putExtra(InventoryActivity.EXTRA_USERNAME, mUsername);
                startActivityForResult(intent, 1);
                return true;
            case R.id.sortAbc:
                sortAbc();
                return true;
            case R.id.sortQty:
                sortQty();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!(data == null)) {
            tagId = data.getLongExtra("tagId", tagId);
            Tag tag = mDb.getTagForPopup(mUsername, String.valueOf(tagId));
            filter = tag.getTag();
        }
        else {
            filter = null;
        }
        onResume();
    }

    // Title sort button function
    public void sortAbc() {
        sAbc = !sAbc;
        sQty = false;
        onResume();
    }

    // Quantity sort button function
    public void sortQty() {
        sAbc = false;
        sQty = !sQty;
        onResume();
    }

    // Title comparator
    public static class compareTitles implements Comparator<InventoryItem> {
        @Override
        public int compare(InventoryItem item0, InventoryItem item1) {
            return item0.getTitle().compareTo(item1.getTitle());
        }
    }

    // Quantity comparator
    public static class compareQty implements Comparator<InventoryItem> {
        @Override
        public int compare(InventoryItem item0, InventoryItem item1) {
            return item1.getQuantity() - (item0.getQuantity());
        }
    }

    public void onItemSelected(InventoryItem item) {
        mInventoryItem = item;
    }

    public void onItemLongClick(InventoryItem item) {
        mInventoryItem = item;
        List<Tag> tags = mDb.getTags(mUsername);
        dialogBuilder = new AlertDialog.Builder(this);
        final View inventoryPopupView = getLayoutInflater().inflate(R.layout.inventory_popup, null);
        popup_item_name = (EditText) inventoryPopupView.findViewById(R.id.popup_item_name);
        popup_item_qty = (EditText) inventoryPopupView.findViewById(R.id.popup_item_qty);
        save_mods = (Button) inventoryPopupView.findViewById(R.id.popup_save);

        // Get the tag that is mapped to the item
        int mappedTag = mDb.getMapTag(String.valueOf(item.getId()));
        Tag retrievedTag = mDb.getTagForPopup(mUsername, String.valueOf(mappedTag));

        // Define and build spinner for tags
        Spinner spinner = (Spinner) inventoryPopupView.findViewById(R.id.tag_list);
        ArrayAdapter<Tag> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, tags);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Sets item's tag as visible when popup is loaded
        spinner.setSelection(adapter.getPosition(retrievedTag));

        // Behavior for when an item is clicked in the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            // For selecting options in the Spinner
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                tagId = mDb.getTagForSelections(mUsername, parent.getItemAtPosition(pos).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set visible fields to the selected items relevant properties
        popup_item_name.setText(item.getTitle());
        popup_item_qty.setText(String.valueOf(item.getQuantity()));

        dialogBuilder.setView(inventoryPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        save_mods.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String name = popup_item_name.getText().toString();
                String quantity = popup_item_qty.getText().toString();
                if (!name.equals("")) {
                    mDb.updateItemName(mInventoryItem.getId(), name);
                }
                if (!quantity.equals("")) {
                    mDb.updateQuantity(mInventoryItem.getId(), Integer.parseInt(quantity));
                }
                mDb.newMap(mInventoryItem.getId(), tagId);
                dialog.dismiss();
                mAdapter.notifyItemChanged(mInventoryItem.getId());
                onResume();
                checkForLow(mInventoryItem);
            }
        });
    }

    // Update quantities
    public void onAnyUpdateQty(View view) {
        int number = Integer.parseInt(view.getTooltipText().toString());
        if ((mInventoryItem.getQuantity()) + number >= 0) {
            int newQty = mInventoryItem.getQuantity() + number;
            mDb.updateQuantity(mInventoryItem.getId(), newQty);
            mInventoryItem.setQuantity(newQty);
            // Refresh list
            mAdapter.notifyItemChanged(mInventoryItem.getId());
            onResume();
            checkForLow(mInventoryItem);
        }
        else {
            Toast.makeText(this, "Can't have less than ZERO items!!!", Toast.LENGTH_SHORT).show();
        }
    }

    // On click for new item. Launches AddItemActivity
    public void onAddItemClick(View view) {
        Intent intent = new Intent(this, AddItemActivity.class);
        intent.putExtra(InventoryActivity.EXTRA_USERNAME, mUsername);
        startActivity(intent);
    }

    // On click for delete button. Launches a dialog to confirm
    public void onDeleteButtonClick(View view) {
        // Check to see if item is selected before deleting it
        if (mInventoryItem != null) {

            // Set up confirm dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.confirm_dialog_message)
                    .setTitle(R.string.confirm_dialog_title)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Confirm
                            mDb.deleteItem(mInventoryItem);
                            mAdapter.removeItem(mInventoryItem);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    // Called when decreasing quantities, and if
    private void checkForLow(InventoryItem item) {
        if (item.getNotifyOnLow() == 1) {
            if (item.getQuantity() <= 1) {
                createNotificationChannel();
                createNotification(item.getTitle());
            }
        }
    }

    // Notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return;

        CharSequence name = "Low stock";
        String description = "item is out of stock";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        //Register channel with system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    // Notification builder
    private void createNotification(String text) {
        // Create notification with properties
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("Stock alert")
                .setContentText("Item out of stock: " + text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        int notifyInt = text.hashCode();

        // Get compatibility NotificationManager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Post notification using ID. if same ID, this notification replaces previous one
        notificationManager.notify(notifyInt, notification);
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener  {

        private InventoryItem mInventoryItem;
        private TextView mTextView;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.recycler_view_item, parent, false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mTextView = itemView.findViewById(R.id.cardTitle);
        }

        public void bind(InventoryItem item, int position) {
            mInventoryItem = item;
            String cardText = item.getTitle() + "\n" + item.getQuantity();
            mTextView.setText(cardText);

            if (mSelectedInventoryPosition == position) {
                // Make item stand out
                mTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
            } else {
                // Make background color something else.
                mTextView.setBackgroundColor(getResources().getColor(R.color.colorSecondaryLight));
            }
        }

        // Called when an item is selected
        @Override
        public void onClick(View view) {
            mAdapter.notifyItemChanged(mSelectedInventoryPosition);
            mSelectedInventoryPosition = getAdapterPosition();
            mAdapter.notifyItemChanged(mSelectedInventoryPosition);
            onItemSelected(mInventoryItem);
        }

        @Override
        public boolean onLongClick(View view) {
            mAdapter.notifyItemChanged(mSelectedInventoryPosition);
            mSelectedInventoryPosition = getAdapterPosition();
            mAdapter.notifyItemChanged(mSelectedInventoryPosition);
            onItemLongClick(mInventoryItem);
            return true;
        }
    }

    
    public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<InventoryItem> mInventoryItems;

        //data is passed into the constructor
        public RecyclerViewAdapter(List<InventoryItem> items) {
            mInventoryItems = items;
        }

        // inflates the cell layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new ViewHolder(layoutInflater, parent);
        }

        // binds the data to the TextView in each cell
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(mInventoryItems.get(position), position);
        }

        // total number of cells
        @Override
        public int getItemCount() {
            return mInventoryItems.size();
        }

        // Removes item from the list when it is deleted
        public void removeItem(InventoryItem item) {
            // Find item in the list
            int index = mInventoryItems.indexOf(item);
            if (index >= 0) {
                // Remove item
                mInventoryItems.remove(index);
                notifyItemRemoved(index);
            }
        }

        // Clears the screen so that new items or changed items can be displayed
        public void clear() {
            int size = mInventoryItems.size();
            mInventoryItems.clear();
            notifyItemRangeRemoved(0, size);
        }
    }
}

