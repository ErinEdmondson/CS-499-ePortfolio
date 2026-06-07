package com.example.erinedmondson_option1_inventoryapp;

import android.os.Bundle;
import android.Manifest; // Import to point back to permissions
import android.content.pm.PackageManager; // Check permission
import android.view.View; // android:onClick
import android.widget.TextView; // Update text
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // Permissions request
import androidx.core.content.ContextCompat; // State of permissions
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull; // Import to fix caution for @NonNull

public class SmsActivity extends AppCompatActivity {

    private TextView tvSmsStatus; // Reference to the textview that displays the status of SMS notifications
    private static final int REQUEST_SMS = 1; // Used a number to easily identify/recall requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms);
        if (getSupportActionBar() != null) { // IF top bar exists, show back arrow
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvSmsStatus = findViewById(R.id.tvSmsStatus); // Link variable to SMS activity XML

        // Check app permissions to send SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            tvSmsStatus.setText(R.string.sms_status_granted);
        } else {
            tvSmsStatus.setText(R.string.sms_status_denied);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // return to inventory screen
        return true;
    }

    public void enableSms(View view) { // When the button is clicked to enable
        // IF the permission to send sms notifications is GRANTED
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // UPDATE the status
            tvSmsStatus.setText(R.string.sms_status_granted);

        } else { // ELSE permission request popup again
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS);
        }
    }

    // When button to disable is clicked
    public void disableSms(View view) {
        Toast.makeText(
                this,
                "To turn off SMS permission: Settings > Apps > ErinEdmondson_Option1_InventoryApp > Permissions > SMS",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override // Receive response from user
    public void onRequestPermissionsResult(int request, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(request, permissions, results); // Default handling as backup

        if (request == REQUEST_SMS) { // IF result is for SMS request
            // IF response is given and if user enabled notifications
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                // UPDATE UI
                tvSmsStatus.setText(R.string.sms_status_granted);

            }  // OTHERWISE permissions denied
            else {
                tvSmsStatus.setText(R.string.sms_status_denied);
            }
        }
    }
}