package com.example.erinedmondson_option1_inventoryapp;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.erinedmondson_option1_inventoryapp.model.DatabaseContract;
import com.example.erinedmondson_option1_inventoryapp.model.DatabaseHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// Use AndroidJUnit4
@RunWith(AndroidJUnit4.class)
public class AuthenticationDatabaseTest {

    // Test db name
    private static final String DB_NAME = "inventory.db";

    private Context context;
    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        // Get app context for testing
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Delete old test db
        context.deleteDatabase(DB_NAME);
        // Create helper
        dbHelper = new DatabaseHelper(context);
        // Open writeable db
        dbHelper.getWritableDatabase();
    }

    @After
    // Close DB helper, delete test db after each test
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase(DB_NAME);
    }

    @Test

    public void createUser_validCredentials_loginSucceeds() {
        // Create user
        assertTrue(dbHelper.createUser("erin", "SecurePass123!"));
        // Login should be successful
        assertTrue(dbHelper.validateLogin("erin", "SecurePass123!"));
    }

    @Test
    public void validateLogin_wrongPassword_loginFails() {
        // Create user
        assertTrue(dbHelper.createUser("erin", "SecurePass123!"));
        // Wrong password will fail
        assertFalse(dbHelper.validateLogin("erin", "WrongPassword"));
    }

    @Test
    public void createUser_duplicateUsername_isRejected() {
        // Create user
        assertTrue(dbHelper.createUser("erin", "SecurePass123!"));
        // Duplicate will fail
        assertFalse(dbHelper.createUser("erin", "AnotherPass123!"));
    }

    @Test
    public void validateLogin_sqlInjectionStyleUsername_doesNotBypassLogin() {
        // Create user
        assertTrue(dbHelper.createUser("erin", "SecurePass123!"));
        // SQL injection style input will fail
        assertFalse(dbHelper.validateLogin("' OR '1'='1", "SecurePass123!"));
    }

    @Test
    public void createUser_passwordIsNotStoredAsPlaintext() {
        String rawPassword = "SecurePass123!"; // Original password
        assertTrue(dbHelper.createUser("erin", rawPassword)); // Create

        String[] fields = getStoredPasswordAndSalt("erin"); // Get password and salt

        // Password should not be null
        assertNotNull(fields[0]);
        // Salt field should not be null
        assertNotNull(fields[1]);
        // Stored password should not match raw
        assertNotEquals(rawPassword, fields[0]);
        // Salt should not be empty
        assertFalse(fields[1].isEmpty());
    }

    @Test
    public void createUser_samePasswordForTwoUsers_generatesDifferentSaltsAndHashes() {
        String sharedPassword = "SamePassword123!"; // Same password for both users

        assertTrue(dbHelper.createUser("erin", sharedPassword)); // Create user 1
        assertTrue(dbHelper.createUser("testuser", sharedPassword)); // Create user 2

        // Get first user credentials
        String[] erinFields = getStoredPasswordAndSalt("erin");
        // Get second user credentials
        String[] testUserFields = getStoredPasswordAndSalt("testuser");

        // Salt should be different
        assertNotEquals(erinFields[1], testUserFields[1]);
        // Hashes should be different
        assertNotEquals(erinFields[0], testUserFields[0]);
    }

    @Test
    public void databaseUpgrade_preservesLegacyUserAndItemAndMigratesPassword() {
        // Close current db
        dbHelper.close();
        // Delete db and create legacy version
        context.deleteDatabase(DB_NAME);

        SQLiteDatabase legacyDb = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        // Create old users table without salt column
        legacyDb.execSQL("CREATE TABLE users ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "password TEXT NOT NULL);");

        // Create old items table
        legacyDb.execSQL("CREATE TABLE items ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL DEFAULT 0);");

        // Insert legacy user and items
        legacyDb.execSQL("INSERT INTO users (username, password) VALUES ('legacyUser', 'LegacyPass123!');");
        legacyDb.execSQL("INSERT INTO items (name, quantity) VALUES ('Legacy Item', 7);");

        // Set old db version
        legacyDb.setVersion(4);
        // Close
        legacyDb.close();

        // Re-open with current helper
        dbHelper = new DatabaseHelper(context);
        // Trigger upgrade
        SQLiteDatabase upgradedDb = dbHelper.getWritableDatabase();

        // DB should upgrade to version 5
        assertEquals(5, upgradedDb.getVersion());
        // Legacy should log in still
        assertTrue(dbHelper.validateLogin("legacyUser", "LegacyPass123!"));

        // Get migrated credentials
        String[] fields = getStoredPasswordAndSalt("legacyUser");
        // Password should not be plain text
        assertNotEquals("LegacyPass123!", fields[0]);
        // Salt should not be null
        assertNotNull(fields[1]);
        // Salt should not be empty
        assertFalse(fields[1].isEmpty());

        // Check if legacy item still exists
        Cursor itemCursor = upgradedDb.query(
                DatabaseContract.ItemEntry.TABLE_NAME,
                new String[]{
                        DatabaseContract.ItemEntry.COLUMN_NAME,
                        DatabaseContract.ItemEntry.COLUMN_QUANTITY
                },
                DatabaseContract.ItemEntry.COLUMN_NAME + "=?",
                new String[]{"Legacy Item"},
                null,
                null,
                null
        );

        try {
            // Item should be found and still be 7
            assertTrue(itemCursor.moveToFirst());
            assertEquals(7, itemCursor.getInt(
                    itemCursor.getColumnIndexOrThrow(DatabaseContract.ItemEntry.COLUMN_QUANTITY)
            ));
        } finally {
            itemCursor.close();
        }
    }

    private String[] getStoredPasswordAndSalt(String username) {
        // Open readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query stored password and dalt for username
        Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                new String[]{
                        DatabaseContract.UserEntry.COLUMN_PASSWORD,
                        DatabaseContract.UserEntry.COLUMN_SALT
                },
                DatabaseContract.UserEntry.COLUMN_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );

        try {
            // User should be found
            assertTrue(cursor.moveToFirst());

            // Get stored hash and salt
            String password = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_PASSWORD)
            );
            String salt = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_SALT)
            );

            // Return values
            return new String[]{password, salt};
        } finally {
            cursor.close();
        }
    }
}









