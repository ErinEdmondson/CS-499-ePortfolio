package com.example.erinedmondson_option1_inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.ContentValues;
import java.util.ArrayList;
import java.util.List;

// CRUD operations handling
public class DatabaseHelper extends SQLiteOpenHelper {

    // Name of database stored
    private static final String DATABASE_NAME = "inventory.db";

    // Version of DB stored
    private static final int DATABASE_VERSION = 1;

    // Constructor for connecting to DB
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // When database is created
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create user table
        String createUsers = "CREATE TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ("
                + DatabaseContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.UserEntry.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, "
                + DatabaseContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL);";

        // Create items table
        String createItems = "CREATE TABLE " + DatabaseContract.ItemEntry.TABLE_NAME + " ("
                + DatabaseContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.ItemEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + DatabaseContract.ItemEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        // Execute commands to create tables
        db.execSQL(createUsers);
        db.execSQL(createItems);
    }

    // When database version changes, delete old tables.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ItemEntry.TABLE_NAME);
        onCreate(db); // Recreate
    }

    // Username and password passed as arguments, return true or false
    public boolean validateLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase(); // Read only to check data

        // Find where username AND password match
        String selection = DatabaseContract.UserEntry.COLUMN_USERNAME + "=? AND " +
                DatabaseContract.UserEntry.COLUMN_PASSWORD + "=?";

        // Fill in username and password
        String[] selectionArgs = { username, password };

        // Query users table for data where username and password match user input
        // Store in cursor object
        Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                new String[]{ DatabaseContract.UserEntry._ID },
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // If at least one record matches, return value to login successfully.
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    // Follow same logic as above. Checks username.
    // If username already exists, avoid duplication
    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();

        String selection = DatabaseContract.UserEntry.COLUMN_USERNAME + "=?";
        String[] selectionArgs = { username };

        Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                new String[]{ DatabaseContract.UserEntry._ID },
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Create new user acct in database
    // Insert username and password into users table
    // IF insert fails (because username already exists), return false
    // IF insert is successful, return true
    public boolean createUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UserEntry.COLUMN_USERNAME, username);
        values.put(DatabaseContract.UserEntry.COLUMN_PASSWORD, password);

        long result = db.insert(DatabaseContract.UserEntry.TABLE_NAME, null, values);
        return result != -1;
    }

    // Follow logic above. This is for ADDING ITEMS
    public long addItem(String name, int quantity) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ItemEntry.COLUMN_NAME, name);
        values.put(DatabaseContract.ItemEntry.COLUMN_QUANTITY, quantity);

        return db.insert(DatabaseContract.ItemEntry.TABLE_NAME, null, values);
    }

    // Retrieve all inventory from database
    public List<InventoryItem> getAllItems() {
        SQLiteDatabase db = getReadableDatabase();

        // Create list to store items
        List<InventoryItem> items = new ArrayList<>();

        // Define which columns to retrieve
        String[] columns = {
                DatabaseContract.ItemEntry._ID,
                DatabaseContract.ItemEntry.COLUMN_NAME,
                DatabaseContract.ItemEntry.COLUMN_QUANTITY
        };

        // Query items and sort by ID
        Cursor cursor = db.query(
                DatabaseContract.ItemEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                DatabaseContract.ItemEntry._ID + " ASC"
        );

        // Get index position
        try {
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseContract.ItemEntry._ID);
            int nameIndex = cursor.getColumnIndexOrThrow(DatabaseContract.ItemEntry.COLUMN_NAME);
            int qtyIndex = cursor.getColumnIndexOrThrow(DatabaseContract.ItemEntry.COLUMN_QUANTITY);

            // Loop through each row
            while (cursor.moveToNext()) {
                // Read value from current
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                int qty = cursor.getInt(qtyIndex);

                // Create object and add it to list
                items.add(new InventoryItem(id, name, qty));
            }
        } finally { // Always close cursor
            cursor.close();
        }

        // Return full list of inventory items
        return items;
    }

    // Update the quantity of a specific item
    public int updateItemQuantity(long id, int newQuantity) {
        SQLiteDatabase db = getWritableDatabase();

        // Store updated quantity
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ItemEntry.COLUMN_QUANTITY, newQuantity);

        // Define which row needs to update
        String where = DatabaseContract.ItemEntry._ID + "=?";
        String[] whereArgs = { String.valueOf(id) };

        // Update database and return rows affected
        return db.update(DatabaseContract.ItemEntry.TABLE_NAME, values, where, whereArgs);
    }

    // Follow same logic as above, but DELETE item
    public int deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();

        String where = DatabaseContract.ItemEntry._ID + "=?";
        String[] whereArgs = { String.valueOf(id) };

        return db.delete(DatabaseContract.ItemEntry.TABLE_NAME, where, whereArgs);
    }

    // Insert starter inventory items if the items table is empty
    // This makes sure the app does not display an empty inventory list on first launch
    public void seedStarterItemsIfEmpty() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseContract.ItemEntry.TABLE_NAME,
                null
        );

        int count = 0;
        try {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }

        if (count == 0) {
            addItem("Apple", 17);
            addItem("Notebook", 27);
            addItem("Monitor", 4);
            addItem("Keyboard", 15);
            addItem("Desk", 3);
            addItem("Lamp", 34);
        }
    }
}