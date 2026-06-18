package com.example.erinedmondson_option1_inventoryapp.model;

/*
======================================================
PASSWORD HASHER - Handles password hashing for accounts.
======================================================
 */

// Reference for implementing algorithm
// https://stackoverflow.com/questions/22580853/reliable-implementation-of-pbkdf2-hmac-sha256-for-java

import android.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

    // Set amount of times to run hashing process
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256; // 256 bits for length of generated hash
    private static final int SALT_BYTES = 16;  // 16 bytes for length of salt

    public static byte[] generateSalt() {
        // Create secure random number generator
        SecureRandom random = new SecureRandom();
        // Create empty byte array to hold the salt
        byte[] salt = new byte[SALT_BYTES];
        // Random bytes are put into the array
        random.nextBytes(salt);
        return salt;
    }

    public static byte[] hashPassword(char[] password, byte[] salt) {
        // Hashing rules
        // Uses user password, the random salt, runs 10000 times, and sets final hash size.
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            // Uses PBKDF2 hashing algorithm with SHA-256
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            // Generate hashed password returned as bytes
            return skf.generateSecret(spec).getEncoded();
            // If hashing fails, stop and return error
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
            // Clear the password from memory
        } finally {
            spec.clearPassword();
        }
    }

    // Convert into base64 string for safe text storage
    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    // Convert to base64 string back to bytes when reading salt from database
    public static byte[] fromBase64(String str) {
        return Base64.decode(str, Base64.NO_WRAP);
    }
}
