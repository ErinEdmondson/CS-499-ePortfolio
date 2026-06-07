package com.example.erinedmondson_option1_inventoryapp.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

/*
======================================================
DATABASE HELPER - This handles all SQLite operations.
This includes:
- Creating tables
- Login validation
- Account creation
- Hashing and salting
- Adding items
- Retrieving items
- Updating items
- Deleting items
======================================================
 */

// CRUD operations handling for SQLite
public class DatabaseHelper extends SQLiteOpenHelper {

    // Name of database stored
    private static final String DATABASE_NAME = "inventory.db";

    // Version of DB stored
    // Updated to version 4. Drops old table to create new table for hashing and salting.
    // Accidentally kept using old DB schema due to not deleting app
    private static final int DATABASE_VERSION = 4;

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
                + DatabaseContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL, "
                + DatabaseContract.UserEntry.COLUMN_SALT + " TEXT NOT NULL);";

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

    // Username and password passed as arguments, return true or false using PBKDF2
    public boolean validateLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase(); // Read only to check data

        // Find where username AND password match
        String selection = DatabaseContract.UserEntry.COLUMN_USERNAME + "=?";
                String[] selectionArgs = {username};


        // Query users table for data where username and password match user input
        // Store in cursor object
        Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                new String[]{ DatabaseContract.UserEntry.COLUMN_PASSWORD, DatabaseContract.UserEntry.COLUMN_SALT },
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // Change to TRUE if password that is entered matches the stored hashed password.
        boolean isValid = false;
        try {
            // If at least one row is returned, a record was found
            // If it returns false, no match found
            if (cursor.moveToFirst()) {
                // Get index for the stored hash and salt. If there is an issue, throw error.
                int passIndex = cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_PASSWORD);
                int saltIndex = cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_SALT);
                // Read stored info from database. Is a Base64 string stored as text.
                String storedHashStr = cursor.getString(passIndex);
                String storedSaltStr = cursor.getString(saltIndex);

                // Convert back into byte array for hashing function
                byte[] saltBytes = PasswordHasher.fromBase64(storedSaltStr);
                // Hash user input using stored salt.
                byte[] computedHashBytes = PasswordHasher.hashPassword(password.toCharArray(), saltBytes);
                // Convert new hash into base64 string to make same format for comparing.
                String computedHashStr = PasswordHasher.toBase64(computedHashBytes);

                // If they match, isValid is true. Otherwise, false.
                isValid = storedHashStr.equals(computedHashStr);
            }
        } finally {
            cursor.close();
        }
        // Return true if the password matched.
        // Return false if no match or user is found.
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
   // Generate SecureRandom salt and hash password - PBKDF2WithHmacSHA256
    public boolean createUser(String username, String password) {

        try {
            // Open in write mode to insert data
            SQLiteDatabase db = getWritableDatabase();

            // Generate random, unique salt
            byte[] saltBytes = PasswordHasher.generateSalt();
            // Hash user's password with generated salt
            byte[] hashBytes = PasswordHasher.hashPassword(password.toCharArray(), saltBytes);

            // Convert salt and hash to base64 string to store
            String saltStr = PasswordHasher.toBase64(saltBytes);
            String hashStr = PasswordHasher.toBase64(hashBytes);

            // Empty container for values
            ContentValues values = new ContentValues();
            // Store username, hashed password, and salt used for hashing.
            values.put(DatabaseContract.UserEntry.COLUMN_USERNAME, username);
            values.put(DatabaseContract.UserEntry.COLUMN_PASSWORD, hashStr);
            values.put(DatabaseContract.UserEntry.COLUMN_SALT, saltStr);

            // Insert new user into database. Throw exception if something fails
            long result = db.insertOrThrow(DatabaseContract.UserEntry.TABLE_NAME, null, values);
            // If successful, output does NOT equal -1 so return true.
            return result != -1;
            // Log error for debugging, and return false if createUser fails.
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error in createUser", e);
            return false;
        }
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