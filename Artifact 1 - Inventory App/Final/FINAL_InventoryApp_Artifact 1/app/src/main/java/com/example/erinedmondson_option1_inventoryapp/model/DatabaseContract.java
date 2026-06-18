package com.example.erinedmondson_option1_inventoryapp.model; // Model layer

import android.provider.BaseColumns;

/*
======================================================
DATABASE CONTRACT - This stores all table and column
names used throughout the application.
======================================================
 */

// Refer to https://developer.android.com/training/data-storage/sqlite for guidance
public final class DatabaseContract {

    private DatabaseContract() {} // Prevent object creation

    // Defines names for table/columns for user creation
    // Salt column added to verify hashed passwords
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_SALT = "salt";
    }

    // Define names for table/columns for item creation
    public static class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "items";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_QUANTITY = "quantity";
    }
}