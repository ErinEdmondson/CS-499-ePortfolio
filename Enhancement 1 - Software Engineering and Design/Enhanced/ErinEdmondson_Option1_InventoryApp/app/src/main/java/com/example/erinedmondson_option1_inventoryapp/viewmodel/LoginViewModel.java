package com.example.erinedmondson_option1_inventoryapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.example.erinedmondson_option1_inventoryapp.model.DatabaseHelper;


/*
======================================================
LOGIN VIEWMODEL- Handles logic for logging in.
======================================================
 */

public class LoginViewModel extends AndroidViewModel {
    // Helper for checking and creating users
    private final DatabaseHelper dbHelper;
    public LoginViewModel(@NonNull Application application) {
        super(application);
        // Create helper using app context
        this.dbHelper = new DatabaseHelper(application);
        // Open or create database if it doesn't already exist
        this.dbHelper.getWritableDatabase();
    }
    public boolean validateLogin(String username, String password) {
        // Prevent login if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        // Check credentials against database
        return dbHelper.validateLogin(username, password);
    }
    // Check if the username already exists
    public boolean userExists(String username) {
        return dbHelper.userExists(username);
    }
    public boolean createUser(String username, String password) {
        // Prevent account creation if field is empty
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        // Create new user
        return dbHelper.createUser(username, password);
    }
}

