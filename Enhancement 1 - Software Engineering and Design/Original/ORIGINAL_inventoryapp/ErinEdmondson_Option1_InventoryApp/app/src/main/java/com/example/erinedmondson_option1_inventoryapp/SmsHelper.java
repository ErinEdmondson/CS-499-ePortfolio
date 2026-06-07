package com.example.erinedmondson_option1_inventoryapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.content.ContextCompat;

public class SmsHelper {

    // Number used for testing
    private static final String EMULATOR_PHONE = "5554";

    // Check current permissions
    public static boolean canSendSms(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Send alert when inventory is low

    public static void sendLowInventoryAlert(Context context, String itemName, int qty) {

        // IF permission is NOT granted, do NOT send message
        if (!canSendSms(context))
            return;
        // Inventory low message
        String msg = "Low inventory alert: " + itemName + " is low (" + qty + ").";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(EMULATOR_PHONE, null, msg, null, null);
    }
}