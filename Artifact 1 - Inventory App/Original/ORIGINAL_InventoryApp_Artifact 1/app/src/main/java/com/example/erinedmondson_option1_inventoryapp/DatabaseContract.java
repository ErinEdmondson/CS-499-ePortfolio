package com.example.erinedmondson_option1_inventoryapp;

import android.provider.BaseColumns;


// Refer to https://developer.android.com/training/data-storage/sqlite for guidance
public final class DatabaseContract {

    private DatabaseContract() {}

    // Defines names for table/columns for user creation
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
    }

    // Define names for table/columns for item creation
    public static class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "items";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_QUANTITY = "quantity";
    }
}