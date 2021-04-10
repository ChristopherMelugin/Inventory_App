package com.cmelugin.inventory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.cmelugin.inventory.InventoryActivity.EXTRA_USERNAME;

public class AddItemActivity extends AppCompatActivity {

    private final int REQUEST_SMS = 0;
    private EditText mTitle;
    private EditText mQuantity;
    private CheckBox mNotify;
    private Database mItemDb;
    private String mUsername;
    private InventoryItem newItem = new InventoryItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_item);

    mTitle = findViewById(R.id.new_item_name);
    mQuantity = findViewById(R.id.new_item_qty);
    mNotify = findViewById(R.id.notify_low_qty);
    Intent intent = getIntent();
    mUsername = intent.getStringExtra(EXTRA_USERNAME);

    mItemDb = Database.getInstance(getApplicationContext());

    // Define and build spinner for tags
    Spinner spinner = (Spinner) findViewById(R.id.tag_list);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.planets_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    }

    // Sends the values to the database for insertion
    public void addItemToDb(View view) {
        if ((!this.mTitle.getText().toString().equals("")) && (!this.mQuantity.getText().toString().equals(""))) {
            newItem.setTitle(mTitle.getText().toString());
            newItem.setQuantity(Integer.parseInt(mQuantity.getText().toString()));
            newItem.setUsername(mUsername);
            if (mNotify.isChecked()) {
                newItem.setNotifyOnLow(1);
            }
            else {
                newItem.setNotifyOnLow(0);
            }
            mItemDb.addItem(newItem);
            finish();
        }
        else {
            Toast.makeText(this, "Both a title and quantity are required", Toast.LENGTH_LONG).show();
        }
    }


    // Prompt and otherwise check for permissions
    public void checkPermissions(View view) {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            // Permission has been granted proceed
        }
        else {
            // Permission has been denied or not requested yet. Request again
            // Provide rationale
            if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "Permission needed to notify when items are low.", Toast.LENGTH_SHORT).show(); }

            // Make the request
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }



}
