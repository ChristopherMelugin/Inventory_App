package com.cmelugin.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.cmelugin.inventory.InventoryActivity.EXTRA_USERNAME;

public class AddTagActivity extends AppCompatActivity {

    private EditText mTag;
    private Database mItemDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);
        mTag = findViewById(R.id.new_tag_name);
        mItemDb = Database.getInstance(getApplicationContext());
    }

    // Sends the values to the database for insertion
    public void addTagToDb(View view) {
        if ((!this.mTag.getText().toString().equals(""))) {
            mItemDb.addTag(mTag.getText().toString());
            finish();
        }
        else {
            Toast.makeText(this, "Need something in the tag field", Toast.LENGTH_LONG).show();
        }
    }
}
