package com.example.erinedmondson_option1_inventoryapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.example.erinedmondson_option1_inventoryapp.model.DatabaseHelper;

/*
======================================================
ADD ITEM VIEWMODEL- Handles logic for adding items.
======================================================
 */

public class AddItemViewModel extends AndroidViewModel {

    // Database helper for interaction
    private final DatabaseHelper dbHelper;

    // Pass app context to AndroidViewModel
    // Create helper.
    public AddItemViewModel(@NonNull Application application) {
        super(application);
        this.dbHelper = new DatabaseHelper(application);
    }

    // Store results
    // Indicate if it was added successfully
    // Store error message if not added successfully
    public static class AddItemResult {
        public final boolean isSuccess;
        public final String errorMessage;

        // Set message status
        public AddItemResult(boolean isSuccess, String errorMessage) {
            this.isSuccess = isSuccess;
            this.errorMessage = errorMessage;
        }
    }
    public AddItemResult confirmAddItem(String name, String qtyText) { // Logic handling for checking and adding items
        // IF name or quantity is empty, show error and stop
        if (name.isEmpty() || qtyText.isEmpty()) {
            return new AddItemResult(false, "Enter item name and quantity.");
        }
        // Try converting text to a number
        int qty;
        try {
            // If number is not valid, show error and stop
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            return new AddItemResult(false, "Quantity cannot be negative.");
        }
        // Insert into database
        long newId = dbHelper.addItem(name, qty);
        // IF item is successfully inserted, show verification message and close
        if (newId != -1) {
            return new AddItemResult(true, null);
            // ELSE show error message that adding item was not successful
        } else {
            return new AddItemResult(false, "Failed to add item.");
        }
    }
}