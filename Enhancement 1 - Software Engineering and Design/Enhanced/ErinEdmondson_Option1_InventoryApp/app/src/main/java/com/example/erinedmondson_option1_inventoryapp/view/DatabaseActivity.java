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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import com.example.erinedmondson_option1_inventoryapp.R;
import com.example.erinedmondson_option1_inventoryapp.model.InventoryItem;
import com.example.erinedmondson_option1_inventoryapp.viewmodel.DatabaseViewModel;


import java.util.ArrayList;

/*
======================================================
DATABASE ACTIVITY- Screen for inventory management.
Displays the list of items and necessary functions.
======================================================
 */

// Referred to for an example:
// https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example

// Inventory list from SQLite. Allows user to edit and manage items.

public class DatabaseActivity extends AppCompatActivity {
    // RecyclerView with list of inventory items
    private RecyclerView rvInventory;
    // Use adapter to connect list
    private RecyclerViewAdapter adapter;
    private DatabaseViewModel viewModel;
    // CRUD functions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_database);

        viewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Link RecyclerView variable to XML
        rvInventory = findViewById(R.id.rvInventory);
        // Vertical list
        rvInventory.setLayoutManager(new LinearLayoutManager(this));

        // Create adapter and connect it to empty list
        adapter = new RecyclerViewAdapter(this, new ArrayList<>());
        rvInventory.setAdapter(adapter);

        viewModel.getItemsList().observe(this, items -> {
            adapter.updateData(items);
        });

        viewModel.getToastMessage().observe(this, msg -> {
            Toast.makeText(DatabaseActivity.this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.getSmsAlertTriggered().observe(this, itemName -> {
            Toast.makeText(
                    DatabaseActivity.this,
                    "Low inventory alert sent for " + itemName,
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Clicker to increase quantity by 1 when the plus symbol is clicked
        adapter.setPlusClickListener((view, position) -> {
            InventoryItem item = adapter.getItemAt(position);
            viewModel.incrementItemQuantity(item);
        });

        // Follow same logic for minus button. Decrease quantity by 1

        adapter.setMinusClickListener((view, position) -> {
            InventoryItem item = adapter.getItemAt(position);
            viewModel.decrementItemQuantity(item, DatabaseActivity.this);
        });

        // Click listener for DELETE. Removes items from database and UI
        // Follow similar logic that was done for minus and plus.
        adapter.setDeleteClickListener((view, position) -> {
            InventoryItem item = adapter.getItemAt(position);
            viewModel.deleteInventoryItem(item);
        });

        // Click listener for QUANTITY. Increase by 1
        // Follow same logic as above
        adapter.setQuantityClickListener((view, position) -> {
            InventoryItem item = adapter.getItemAt(position);
            viewModel.incrementItemQuantity(item);
        });
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

        viewModel.loadInventoryItems();
        viewModel.evaluateLowStockAlerts();
    }
}
