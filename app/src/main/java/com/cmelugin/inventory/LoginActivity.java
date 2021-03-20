package com.cmelugin.inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private LoginValue mLogin = new LoginValue();
    private Database mLoginDb;
    private EditText mUser;
    private EditText mPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginDb = Database.getInstance(getApplicationContext());

    }

    public void onRegisterClick(View view) {
        mUser = findViewById(R.id.username);
        mPass = findViewById(R.id.password);
        int success = 2;

        if ((!mUser.getText().toString().equals("")) && (!mPass.getText().toString().equals(""))) {
            mLogin.setUsername(this.mUser.getText().toString());
            mLogin.setPassword(this.mPass.getText().toString());
            success = mLoginDb.addUser(mLogin);
            if (success == 0) {
                Intent intent = new Intent(this, InventoryActivity.class);
                intent.putExtra(InventoryActivity.EXTRA_USERNAME, mUser.getText().toString());
                startActivity(intent);
            }
        }
        else {
            Toast.makeText(this, "Need username and password", Toast.LENGTH_SHORT).show();
            success = 3;
        }

        switch (success) {
            case 0:
                // successful creation of new username
                Toast.makeText(this, "Successfully added new user", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void onSignInClick(View view) {
        mUser = findViewById(R.id.username);
        mPass = findViewById(R.id.password);

        LoginValue mSignIn = new LoginValue();
        mSignIn.setUsername(this.mUser.getText().toString());
        mSignIn.setPassword(this.mPass.getText().toString());
        boolean authentic = mLoginDb.checkCredentials(mSignIn);

        if (authentic) {
            Toast.makeText(this, "Welcome", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, InventoryActivity.class);
            intent.putExtra(InventoryActivity.EXTRA_USERNAME, mSignIn.getUsername());
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
        }
    }
}