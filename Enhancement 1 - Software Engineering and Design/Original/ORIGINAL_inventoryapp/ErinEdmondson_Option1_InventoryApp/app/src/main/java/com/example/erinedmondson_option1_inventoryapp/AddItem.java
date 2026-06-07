package com.example.erinedmondson_option1_inventoryapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddItem extends AppCompatActivity {

    private DatabaseHelper dbHelper; // Helper to interact with SQLite
    private EditText etItemName; // Fields for name and quantity
    private EditText etItemQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        dbHelper = new DatabaseHelper(this); // Create instance
        etItemName = findViewById(R.id.etItemName); // Connect EditText variables to fields in XML file
        etItemQuantity = findViewById(R.id.etItemQuantity);

        if (getSupportActionBar() != null) { // Enable back arrow. Prevents user from getting stuck
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Prevent content from being covered by adjusting the padding.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override // Back button action handling. Close screen and go to previous
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // When user confirms
    public void confirmAddItem(View view) {
        // Take user input. CANNOT BE NULL
        String name = etItemName.getText() == null ? "" : etItemName.getText().toString().trim();
        String qtyText = etItemQuantity.getText() == null ? "" : etItemQuantity.getText().toString().trim();

        // IF name or quantity is empty, show error and stop
        if (name.isEmpty() || qtyText.isEmpty()) {
            Toast.makeText(this, "Enter item name and quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty;
        // Try converting text to a number
        try {
            qty = Integer.parseInt(qtyText);
            // IF number is not valid, show error and stop
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must be a number", Toast.LENGTH_SHORT).show();
            return;
        }
        // IF quantity is less than zero, show error and stop
        if (qty < 0) {
            Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }
        // Insert into database
        long newId = dbHelper.addItem(name, qty);
        // IF item is successfully inserted, show verification message and close
        if (newId != -1) {
            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            finish();

            // ELSE show error message that adding item was not successful
        } else {
            Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
        }
    }
}