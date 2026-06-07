package com.example.erinedmondson_option1_inventoryapp;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;

import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper; // Helper for login validations
    private TextInputEditText etUsername; // Input fields for username and passwords
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        dbHelper = new DatabaseHelper(this); // Create instance of database helper
        dbHelper.getWritableDatabase(); // Ensure db is created
        etUsername = findViewById(R.id.etUsername); // Link variables to xml
        etPassword = findViewById(R.id.etPassword);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Implement simple navigation (without validation) to demonstrate UI transitions
    public void goToInventory(View view) {
        // Read username and password based on inputs. AVOID NULL
        String username = (etUsername.getText() == null) ? "" : etUsername.getText().toString().trim();
        String password = (etPassword.getText() == null) ? "" : etPassword.getText().toString().trim();

        // IF username or password are EMPTY, show error message and stop
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check database to see if credentials are valid
        boolean valid = dbHelper.validateLogin(username, password);

        // IF valid, open inventory screen
        if (valid) {
            startActivity(new Intent(this, DatabaseActivity.class));
            // ELSE show error message
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
    // When sign up is clicked
    public void signUpClicked(View view) {
        // Read username and password from inputs. AVOID NULL
        String username = (etUsername.getText() == null) ? "" : etUsername.getText().toString().trim();
        String password = (etPassword.getText() == null) ? "" : etPassword.getText().toString().trim();

        // IF username or password is empty, show message.
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // IF username exists, show message
        if (dbHelper.userExists(username)) {
            Toast.makeText(this, "Username already exists. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Creates record of credentials.
        boolean created = dbHelper.createUser(username, password);

        // IF account was created successfully, show verification message
        if (created) {
            Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
            etPassword.setText(""); // optional: clears password box
            // ELSE show error message
        } else {
            Toast.makeText(this, "Sign up failed.", Toast.LENGTH_SHORT).show();
        }
    }
}