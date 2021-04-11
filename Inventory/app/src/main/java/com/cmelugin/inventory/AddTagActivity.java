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
    private Database mTagDb;
    private String mUsername;
    private Tag tag = new Tag();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);
        mTag = findViewById(R.id.new_tag_name);
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(EXTRA_USERNAME);
        mTagDb = Database.getInstance(getApplicationContext());
    }

    // Sends the values to the database for insertion
    public void addTagToDb(View view) {
        if ((!this.mTag.getText().toString().equals(""))) {
            tag.setTag(mTag.getText().toString());
            tag.setUsername(mUsername);
            mTagDb.addTag(tag);
            finish();
        }
        else {
            Toast.makeText(this, "Need something in the tag field", Toast.LENGTH_LONG).show();
        }
    }
}
