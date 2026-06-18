package com.example.erinedmondson_option1_inventoryapp.model;

import java.util.ArrayList;
import java.util.List;


/*
======================================================
INVENTORY BST- Used for sorting items
======================================================
 */
public class InventoryBST {

    // Node represents one item
    private static class Node {
        InventoryItem item; // Item stored in node
        Node left; // Left child for storing smaller values
        Node right; // Right child for storing larger values

        Node(InventoryItem item) {
            this.item = item;
            left = null;
            right = null;
        }
    }

    private Node root; // Start tree

    public InventoryBST() {
        this.root = null; // Begin empty
    }

    // Insert item into BST
    public void insert(InventoryItem item) {
        root = insertRec(root, item);
    }

    // Recursive method that finds correct place for item
    private Node insertRec(Node root, InventoryItem item) {
        if (root == null) {
            root = new Node(item); // Create new node if spot is empty
            return root;
        }
        // If new item is before current, go left
        if (item.getName().compareToIgnoreCase(root.item.getName()) < 0) {
            root.left = insertRec(root.left, item);
            // If new item name comes after current, go right
        } else if (item.getName().compareToIgnoreCase(root.item.getName()) > 0) {
            root.right = insertRec(root.right, item);

        } else {
            // If names are same, compare IDs and allow name to be stored
            if (item.getId() < root.item.getId()) {
                root.left = insertRec(root.left, item);

            } else if (item.getId() > root.item.getId()) {
                root.right = insertRec(root.right, item);
            }
        }

        return root;
    }

    // Return items in alphabetical order
    public List<InventoryItem> inOrderTraversal() {
        List<InventoryItem> sortedList = new ArrayList<>();
        inOrderRec(root, sortedList);
        return sortedList;
    }

    // Recursive in-order traversal left, current, right
    private void inOrderRec(Node root, List<InventoryItem> list) {
        if (root != null) {
            inOrderRec(root.left, list); // Visit smaller items first
            list.add(root.item); //  Add current
            inOrderRec(root.right, list); // Visit larger last
        }
    }

    // Clear tree
    public void clear() {
        root = null;
    }
}
