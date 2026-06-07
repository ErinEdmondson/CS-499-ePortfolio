package com.example.erinedmondson_option1_inventoryapp.viewmodel;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;

/*
======================================================
SMS VIEWMODEL- Handles SMS logic for SMS permissions
screen.
======================================================
 */

// ViewModel for SMS permissions logic
public class SmsViewModel extends AndroidViewModel {

    public SmsViewModel(@NonNull Application application) {
        super(application);
    }

    // Check if app currently has SMS permissions
    public boolean isSmsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Display instructions for turning permissions off
    public String getDisableInstructions() {
        return "To turn off SMS permission: Settings > Apps > Inventory Management > Permissions > SMS";
    }
}

