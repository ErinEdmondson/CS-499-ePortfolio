package com.example.erinedmondson_option1_inventoryapp.model;


/*
======================================================
INVENTORY HASHMAP- Used for searching items
======================================================
 */
import java.util.LinkedList;
public class InventoryHashMap {

    // Represents one key-value pair in hash map
    private static class Entry {
        String key; // Item name used as search key
        InventoryItem value; // Item connected to that key

        Entry(String key, InventoryItem value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity; // Number of buckets in hash map
    private final LinkedList<Entry>[] buckets; // Array of linked lists to handle collisions

    // Ignore unchecked type warning
    // Constructor
    @SuppressWarnings("unchecked")
    public InventoryHashMap(int capacity) {
        this.capacity = capacity; // Create new hashmap and choose amount of buckets in it

        // Create bucket array
        this.buckets = new LinkedList[capacity];

        // Initialize as an empty linked list
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new LinkedList<>();
        }
    }

    // Default if no capacity is provided
    public InventoryHashMap() {
        this(16);
    }

    // Convert key to bucket index
    private int getHash(String key) {
        if (key == null) return 0;
        // Use hashCode and limit to bucket array size
        int hash = key.hashCode() % capacity;
        // Make sure index is positive
        return hash < 0 ? hash + capacity : hash;
    }

    // Add or update item
    public void put(String key, InventoryItem item) {
        int index = getHash(key); // Find bucket location
        LinkedList<Entry> bucket = buckets[index];

        // Check if item exists in bucket
        for (Entry entry : bucket) {
            if (entry.key.equalsIgnoreCase(key)) {
                entry.value = item; // Update item
                return;
            }
        }

        // Add new entry if no key found
        bucket.add(new Entry(key, item));
    }

    // Retrieve item by name
    public InventoryItem get(String key) {
        int index = getHash(key); // Find bucket for key
        LinkedList<Entry> bucket = buckets[index];

        // Search bucket for matching key
        for (Entry entry : bucket) {
            if (entry.key.equalsIgnoreCase(key)) {
                return entry.value; // return match
            }
        }

        return null; // return null if no match
    }

    // Remove all entries from HashMap
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buckets[i].clear();
        }
    }
}
