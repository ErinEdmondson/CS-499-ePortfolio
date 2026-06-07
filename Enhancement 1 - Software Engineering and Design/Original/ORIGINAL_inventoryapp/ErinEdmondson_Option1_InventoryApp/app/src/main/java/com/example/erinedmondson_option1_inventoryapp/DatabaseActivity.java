package com.example.erinedmondson_option1_inventoryapp;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

// Referred to for an example:
// https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example

// Inventory list from SQLite. Allows user to edit and manage items.

public class DatabaseActivity extends AppCompatActivity {
    // RecyclerView with list of inventory items
    private RecyclerView rvInventory;
    // Use adapter to connect list
    private RecyclerViewAdapter adapter;
    // Hold items pulled from database in a list
    private ArrayList<InventoryItem> items;
    // CRUD functions
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_database);
        dbHelper = new DatabaseHelper(this); // Create helper for this screen
        dbHelper.seedStarterItemsIfEmpty(); // Adds starter items if items in table are empty
        items = new ArrayList<>(); // New list to store inventory
        // Pull all items from SQLite and add them to list
        items.addAll(dbHelper.getAllItems());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Link RecyclerView variable to XML
        rvInventory = findViewById(R.id.rvInventory);
        // Vertical list
        rvInventory.setLayoutManager(new LinearLayoutManager(this));

      /*  items = new ArrayList<>();
        items.add(new InventoryItem("Apple", 17));
        items.add(new InventoryItem("Notebook", 27));
        items.add(new InventoryItem("Monitor", 4));
        items.add(new InventoryItem("Keyboard", 15));
        items.add(new InventoryItem("Desk", 3));
        items.add(new InventoryItem("Lamp", 34)); */

        // Create adapter and connect it to list
        adapter = new RecyclerViewAdapter(this, items);
        // Clicker to increase quantity by 1 when the plus symbol is clicked
        adapter.setPlusClickListener((view, position) -> {
            InventoryItem item = items.get(position); // Get item where row was clicked

            int newQty = item.getQuantity() + 1; // increase

            dbHelper.updateItemQuantity(item.getId(), newQty); // Update database with new number

            // Update local list to show UI change
            item.setQuantity(newQty);
            adapter.notifyItemChanged(position); // Refresh this row
        });
        // Follow same logic for minus button. Decrease quantity by 1

        adapter.setMinusClickListener((view, position) -> {
            InventoryItem item = items.get(position);

            int currentQty = item.getQuantity();
            int newQty = Math.max(0, currentQty - 1);  // prevent negative numbers

            dbHelper.updateItemQuantity(item.getId(), newQty);

            item.setQuantity(newQty);
            adapter.notifyItemChanged(position);

            // IF the item goes from ABOVE 5 to 5 or below, send an inventory alert
            if (currentQty > 5 && newQty <= 5) {
                // Trigger SMS alert. Will only trigger if permissions are enabled
                SmsHelper.sendLowInventoryAlert(this, item.getName(), newQty);
                // Message showing alert triggered
                Toast.makeText(
                        this,
                        "Low inventory alert sent for " + item.getName(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Click listener for DELETE. Removes items from database and UI
        // Follow similar logic that was done for minus and plus.
        adapter.setDeleteClickListener((view, position) -> {
            InventoryItem item = items.get(position);

            dbHelper.deleteItem(item.getId()); // Delete from database

            items.remove(position); // Remove from list to reflect on UI
            adapter.notifyItemRemoved(position);
        });

        // Click listener for QUANTITY. Increase by 1
        // Follow same logic as above
        adapter.setQuantityClickListener((view, position) -> {
            InventoryItem item = items.get(position);

            int newQty = item.getQuantity() + 1;

            dbHelper.updateItemQuantity(item.getId(), newQty);

            item.setQuantity(newQty);
            adapter.notifyItemChanged(position);
        });

       /* adapter.setDeleteClickListener((view, position) -> {
            items.remove(position);
            adapter.notifyItemRemoved(position);
        }); */

        rvInventory.setAdapter(adapter);

    }
    // Temporary UI transition from Inventory screen to SMS alerts
    public void openSmsAlerts(android.view.View view) {
        startActivity(new android.content.Intent(this, SmsActivity.class));
    }
    // Temporary UI for adding an item
    public void openAddItem(View view) {
        Intent intent = new Intent(this, AddItem.class);
        startActivity(intent);
    }
    // Log user out and return to login screen
    public void logoutUser(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    // Runs when returning to Database screen
    @Override
    protected void onResume() {
        super.onResume();
        items.clear(); // Clear old list
        items.addAll(dbHelper.getAllItems()); // Load updated items
        adapter.notifyDataSetChanged(); // Refresh

        // List to display ALL items considered low inventory levels.
        StringBuilder lowList = new StringBuilder();
        for (InventoryItem item : items) {
            if (item.getQuantity() <= 5) { // IF item is 5 or less
                if (lowList.length() > 0) lowList.append(", "); // Add it to the low inventory message
                lowList.append(item.getName()).append(" (").append(item.getQuantity()).append(")");
            }
        }

        // IF 1 or more low inventory item exists, show a warning toast that lists them all
        if (lowList.length() > 0) {
            Toast.makeText(this, "Low inventory: " + lowList, Toast.LENGTH_LONG
            ).show();
        }
    }
}
