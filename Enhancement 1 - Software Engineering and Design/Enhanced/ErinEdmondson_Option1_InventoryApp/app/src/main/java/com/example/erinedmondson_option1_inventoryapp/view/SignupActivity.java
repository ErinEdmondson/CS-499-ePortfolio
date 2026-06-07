package com.example.erinedmondson_option1_inventoryapp.view;

import android.os.Bundle;
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
SIGNUP ACTIVITY- Screen for creating an account.
======================================================
 */

public class SignupActivity extends AppCompatActivity {

    // Handle login and sign up logic
    private LoginViewModel viewModel;

    // Text bloxes for username and password
    private com.google.android.material.textfield.TextInputEditText etSignupUsername;
    private com.google.android.material.textfield.TextInputEditText etSignupPassword;


    // Layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Get viewModel for account logic
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Connect variables to XML input
        etSignupUsername = findViewById(R.id.etSignupUsername);
        etSignupPassword = findViewById(R.id.etSignupPassword);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void confirmSignUp(View view) {
        // Get username and password. Uses empty string if box is null
        String username = (etSignupUsername.getText() == null) ? "" : etSignupUsername.getText().toString().trim();
        String password = (etSignupPassword.getText() == null) ? "" : etSignupPassword.getText().toString().trim();

        // If fields are empty, stops sign up
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the user exists in database already, display message
        if (viewModel.userExists(username)) {
            Toast.makeText(this, "Username already exists. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create new user
        boolean created = viewModel.createUser(username, password);
        // If login was successful, show validation message
        if (created) {
            Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
            finish();
            // Otherwise, show error message
        } else {
            Toast.makeText(this, "Sign up failed.", Toast.LENGTH_SHORT).show();
        }
    }
}

