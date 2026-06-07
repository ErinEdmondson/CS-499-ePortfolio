package com.example.erinedmondson_option1_inventoryapp.model;


/*
======================================================
INVENTORY ITEM- Represents a single inventory item.
Used to move inventory data between layers.
======================================================
 */

public class InventoryItem { // Model class for single inventory item
    // Cannot change after creation
    private final long id;
    // Name of inventory item, cannot be changed after creation
    private final String name;
    // Quantity of item can be updated
    private int quantity;

    // Constructor to create a new object
    public InventoryItem(long id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    // Return database ID
    public long getId() {
        return id;
    }

    // return name
    public String getName() {
        return name;
    }

    // return current quantity
    public int getQuantity() {
        return quantity;
    }

    // Update quantity
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
