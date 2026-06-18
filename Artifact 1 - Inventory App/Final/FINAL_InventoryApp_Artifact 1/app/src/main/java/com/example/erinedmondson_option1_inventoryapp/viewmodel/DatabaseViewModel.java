package com.example.erinedmondson_option1_inventoryapp.viewmodel;


import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.erinedmondson_option1_inventoryapp.model.DatabaseHelper;
import com.example.erinedmondson_option1_inventoryapp.model.InventoryBST;
import com.example.erinedmondson_option1_inventoryapp.model.InventoryHashMap;
import com.example.erinedmondson_option1_inventoryapp.model.InventoryItem;
import com.example.erinedmondson_option1_inventoryapp.model.SmsHelper;

import java.util.Collections;
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

    private final InventoryHashMap itemHashMap = new InventoryHashMap();
    private final InventoryBST itemBST = new InventoryBST();

    // States of sorting quantity: 0 = Unsorted, 1 = Descending order, 2 = Ascending order
    private int quantitySortState = 0;
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

        itemHashMap.clear(); // Clear old data
        itemBST.clear();
        // Rebuild structures with updated inventory list
        for (InventoryItem item : updatedItems) {
            // Store items in HashMap using item name as search key
            itemHashMap.put(item.getName(), item);
            // Insert item into BST
            itemBST.insert(item);
        }
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
    // Search for item by name via custom HashMap
    public void searchItem(String name) {
        InventoryItem foundItem = itemHashMap.get(name);
        // If item is NOT null (exists), display only that item in RecyclerView
        if (foundItem != null) {
            itemsList.setValue(Collections.singletonList(foundItem));
            // Otherwise, display message
        } else {
            toastMessage.setValue("Item not found");
        }
    }
    // Sort items alphabetically using BST
    public void sortItems() {
        quantitySortState = 0; // Reset qty sorting
        // In-order traversal to return items in order
        List<InventoryItem> sortedList = itemBST.inOrderTraversal();
        // Update displayed list with sorted items
        itemsList.setValue(sortedList);
    }
    // Toggles quantity sorting between default, descending, and ascending
    public void toggleQuantitySort() {
        quantitySortState = (quantitySortState + 1) % 3;

        // State 0 is normal inventory list display
        if (quantitySortState == 0) {
            loadInventoryItems();
            // Get full list from database
        } else {
            List<InventoryItem> currentList = dbHelper.getAllItems();
            // If the state is 1, sort from highest to lowest
            if (quantitySortState == 1) {
                Collections.sort(currentList, (a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()));
                // If state is 2, sort from lowest to highest
            } else if (quantitySortState == 2) {
                Collections.sort(currentList, (a, b) -> Integer.compare(a.getQuantity(), b.getQuantity()));
            }
            // Update displayed list
            itemsList.setValue(currentList);
        }
    }
    // Clear search or sort and reloads full inventory list
    public void clearSearch() {
        quantitySortState = 0;
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
