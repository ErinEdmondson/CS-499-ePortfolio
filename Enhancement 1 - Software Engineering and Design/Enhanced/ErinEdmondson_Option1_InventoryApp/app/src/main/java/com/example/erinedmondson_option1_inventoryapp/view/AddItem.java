package com.example.erinedmondson_option1_inventoryapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.erinedmondson_option1_inventoryapp.R;
import com.example.erinedmondson_option1_inventoryapp.viewmodel.AddItemViewModel;


/*
======================================================
ADD ITEM ACTIVITY- Screen for adding inventory.
Separates UI and database logic.
======================================================
 */

public class AddItem extends AppCompatActivity {

    private AddItemViewModel viewModel; // ViewModel to allow interaction with SQLite
    private EditText etItemName; // Fields for name and quantity
    private EditText etItemQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        viewModel = new ViewModelProvider(this).get(AddItemViewModel.class); // Redirect input validation and adding item to database.
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

        AddItemViewModel.AddItemResult result = viewModel.confirmAddItem(name, qtyText);
        // IF item is successfully inserted, show verification message and close
        if (result.isSuccess) {
            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            finish();

            // ELSE show error message that adding item was not successful
        } else {
            Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
