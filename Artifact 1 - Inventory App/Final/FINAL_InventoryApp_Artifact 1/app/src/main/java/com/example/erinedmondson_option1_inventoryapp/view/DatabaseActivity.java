package com.example.erinedmondson_option1_inventoryapp.view;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        // Update RecyclerView adapter when list changes
        viewModel.getItemsList().observe(this, items -> {
            adapter.updateData(items);
        });
        //Observe toast messages. Display on screen
        viewModel.getToastMessage().observe(this, msg -> {
            Toast.makeText(DatabaseActivity.this, msg, Toast.LENGTH_LONG).show();
        });
        // When a low inventory alert is triggered, show confirmation
        viewModel.getSmsAlertTriggered().observe(this, itemName -> {
            Toast.makeText(
                    DatabaseActivity.this,
                    "Low inventory alert sent for " + itemName,
                    Toast.LENGTH_SHORT
            ).show();
        });
        // Get search input
        EditText etSearch = findViewById(R.id.etSearch);

        // Listen for search actions to allow user to press search, done or hit enter
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                // Get user input
                String query = etSearch.getText().toString().trim();
                // Search only if there is user input
                if (!query.isEmpty()) {
                    viewModel.searchItem(query);
                } else {
                    Toast.makeText(DatabaseActivity.this, "Please enter an item name", Toast.LENGTH_SHORT).show();
                }
                return true; // Return true when keyboard action is handled
            }
            return false; // Allows other keyboard actions to continue normally
        });
        // Click listener for search
        // Search for item name entered
        findViewById(R.id.btSearch).setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            // If input is empty
            if (!query.isEmpty()) {
                viewModel.searchItem(query);
                // Display message
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });
        // Clear button click listener
        findViewById(R.id.btClear).setOnClickListener(v -> {
            etSearch.setText("");
            viewModel.clearSearch();
        });
        // Click listener for sorting alphabetically
        findViewById(R.id.btSort).setOnClickListener(v -> {
            viewModel.sortItems();
        });
        // Click listener for sorting by quantity
        findViewById(R.id.btSortQty).setOnClickListener(v -> {
            viewModel.toggleQuantitySort();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        // Get ID of menu item
        int id = item.getItemId();
        // If LOGOUT is selected, return to login screen
        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close current activity so user can not go back in
            return true;

            // If SMS Alerts are selected, open alert screen
        } else if (id == R.id.action_sms_alerts) {
            startActivity(new Intent(this, SmsActivity.class));
            return true;
            // If user selected add item, open add item screen
        } else if (id == R.id.action_add_item) {
            openAddItem(null);
            return true;
        }
        // Parent class handles any other menu items
        return super.onOptionsItemSelected(item);
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
