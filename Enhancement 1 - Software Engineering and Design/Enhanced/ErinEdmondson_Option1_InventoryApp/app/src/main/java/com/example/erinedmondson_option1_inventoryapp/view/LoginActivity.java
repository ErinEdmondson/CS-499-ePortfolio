package com.example.erinedmondson_option1_inventoryapp.view;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.erinedmondson_option1_inventoryapp.R;
import com.example.erinedmondson_option1_inventoryapp.viewmodel.LoginViewModel;


/*
======================================================
LOGIN ACTIVITY- Screen for logging in.
======================================================
 */

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel; // For login validation
    private com.google.android.material.textfield.TextInputEditText etUsername; // Input fields for username and passwords
    private com.google.android.material.textfield.TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Get viewModel for this activity
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

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
        boolean valid = viewModel.validateLogin(username, password);

        // IF valid, open inventory screen
        if (valid) {
            startActivity(new Intent(this, DatabaseActivity.class));
            // ELSE show error message
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
    // When sign up is clicked, move to separate account creation screen
    public void signUpClicked(View view) {

        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);

    }
}