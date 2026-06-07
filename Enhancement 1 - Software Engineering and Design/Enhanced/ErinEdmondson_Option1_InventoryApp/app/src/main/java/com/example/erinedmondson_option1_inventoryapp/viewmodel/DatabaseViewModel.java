package com.example.erinedmondson_option1_inventoryapp.viewmodel;


import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.erinedmondson_option1_inventoryapp.model.DatabaseHelper;
import com.example.erinedmondson_option1_inventoryapp.model.InventoryItem;
import com.example.erinedmondson_option1_inventoryapp.model.SmsHelper;

import java.util.List;

/*
======================================================
DATABASE VIEWMODEL- Handles database logic for the
main inventory screen. Uses LiveData for activity
to automatically update.
======================================================
 */

// ViewModel for database logic connected to inventory screen
public class DatabaseViewModel extends AndroidViewModel {
    // helper for reading and updating inventory
    private final DatabaseHelper dbHelper;

    // Hold current list for UI
    private final MutableLiveData<List<InventoryItem>> itemsList = new MutableLiveData<>();

    // Hold messages for activity to display
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    // Hold item name for SMS alert
    private final MutableLiveData<String> smsAlertTriggered = new MutableLiveData<>();
    public DatabaseViewModel(@NonNull Application application) {
        super(application);
        // Create database helper using app context
        this.dbHelper = new DatabaseHelper(application);
    }

    // Activity observes item updates, toast messages, and alert events
    public LiveData<List<InventoryItem>> getItemsList() {
        return itemsList;
    }
    public LiveData<String> getToastMessage() {
        return toastMessage;
    }
    public LiveData<String> getSmsAlertTriggered() {
        return smsAlertTriggered;
    }

    public void loadInventoryItems() {
        // Add starter item if db is empty
        dbHelper.seedStarterItemsIfEmpty();
        // Get most recent list from database
        List<InventoryItem> updatedItems = dbHelper.getAllItems();
        // Update UI
        itemsList.setValue(updatedItems);
    }
    public void incrementItemQuantity(InventoryItem item) {
        // Add 1 to current quantity
        int newQty = item.getQuantity() + 1;
        // Update quantity in database
        dbHelper.updateItemQuantity(item.getId(), newQty);
        // Reloads list for updated UI
        loadInventoryItems();
    }
    // Use same logic for increment. Do not let quantity to go below 0
    public void decrementItemQuantity(InventoryItem item, Context context) {
        int currentQty = item.getQuantity();
        int newQty = Math.max(0, currentQty - 1);

        dbHelper.updateItemQuantity(item.getId(), newQty);

        // Send low inventory alert when qty drops to 5 or below
        if (currentQty > 5 && newQty <= 5) {
            SmsHelper.sendLowInventoryAlert(context, item.getName(), newQty);
            // Notify UI that alert was triggered
            smsAlertTriggered.setValue(item.getName());
        }
        // Reload list and update
        loadInventoryItems();
    }
    public void deleteInventoryItem(InventoryItem item) {
        // Delete item
        dbHelper.deleteItem(item.getId());
        // Reload and update
        loadInventoryItems();
    }
    public void evaluateLowStockAlerts() {
        // Get current inventory from LiveData
        List<InventoryItem> currentItems = itemsList.getValue();

        // Stop if list is null
        if (currentItems == null) return;
        // One message for all items low
        StringBuilder lowList = new StringBuilder();
        // Loop through all inventory
        for (InventoryItem item : currentItems) {
            // IF quantity is less than or equal to 5
            if (item.getQuantity() <= 5) {
                // Add comma before next item name
                if (lowList.length() > 0) lowList.append(", ");
                // Add name and qty to message
                lowList.append(item.getName()).append(" (").append(item.getQuantity()).append(")");
            }
        }
        // If there are low qty items, send message to UI
        if (lowList.length() > 0) {
            toastMessage.setValue("Low inventory: " + lowList);
        }
    }
}
