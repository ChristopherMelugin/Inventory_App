package com.cmelugin.inventory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import static com.cmelugin.inventory.InventoryActivity.EXTRA_USERNAME;

public class AddTagActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private EditText mTag;
    private String mUsername;
    private Tag tag = new Tag();
    private long tagId;
    private Database mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);
        mTag = findViewById(R.id.new_tag_name);
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(EXTRA_USERNAME);
        mDb = Database.getInstance(getApplicationContext());
        List<Tag> tags = mDb.getTags(mUsername);

        // Define and build spinner for tags
        Spinner spinner = (Spinner) findViewById(R.id.tag_list);
        ArrayAdapter<Tag> adapter = new ArrayAdapter<Tag>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, tags);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
    }

    // For selecting options in the Spinner
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        tagId = mDb.getTagForSelections(mUsername, parent.getItemAtPosition(pos).toString());
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    // Sends the values to the database for insertion
    public void addTagToDb(View view) {
        if ((!this.mTag.getText().toString().equals(""))) {
            tag.setTag(mTag.getText().toString());
            tag.setUsername(mUsername);
            mDb.addTag(tag);
            setResult(RESULT_OK);
            finish();
        }
        else {
            Toast.makeText(this, "Need something in the tag field", Toast.LENGTH_LONG).show();
        }
    }

    // Return data for filter to previous activity so that only the filtered items load
    public void filterItems(View view) {
        Intent intent = new Intent();
        intent.putExtra("tagId", tagId);
        setResult(RESULT_OK, intent);
        finish();
    }

    // Return no data for filter to previous activity so that the whole list loads
    public void clearFilters(View view) {
        setResult(RESULT_OK);

        finish();
    }

}
