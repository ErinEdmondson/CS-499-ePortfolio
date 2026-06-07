package com.example.erinedmondson_option1_inventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.util.List;

// Connect adapter to recyclerview list UI
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    // List of inventory items to display
    private final List<InventoryItem> mData;

    // Used for building row layout
    private final LayoutInflater mInflater;

    // Listener for deleting
    private DeleteClickListener mDeleteClickListener;

    // Listeners for quantity, plus, and minus clicks

    private QuantityClickListener mQuantityClickListener;
    private PlusClickListener mPlusClickListener;
    private MinusClickListener mMinusClickListener;

    public RecyclerViewAdapter(Context context, List<InventoryItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // Create new row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Load into view object
        View view = mInflater.inflate(R.layout.item_inventory_rows, parent, false);
        // Return ViewHolder that references UI elements
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get inventory item at this position
        InventoryItem item = mData.get(position);
        // Set item name
        holder.tvItemName.setText(item.getName());
        // Set quantity
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
    }

    // Return how many rows should display
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // Return item based on position
    public InventoryItem getItem(int id) {
        return mData.get(id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // UI elements
        TextView tvItemName;
        TextView tvQuantity;
        TextView tvPlus;
        TextView tvMinus;
        MaterialButton btnDelete;


        ViewHolder(View itemView) {
            super(itemView);

            // Link variables to XML views
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvMinus = itemView.findViewById(R.id.tvMinus);
            tvPlus = itemView.findViewById(R.id.tvPlus);


            // Quantity click
            tvQuantity.setOnClickListener(v -> {
                // IF listener is NOT null, call it
                if (mQuantityClickListener != null) {
                    // Get current row position
                    int pos = getAdapterPosition();
                    // IF position is valid, send to activity.
                    if (pos != RecyclerView.NO_POSITION) {
                        mQuantityClickListener.onQuantityClick(v, pos);
                    }
                }
            });

            // Follow same logic for PLUS click listener
            tvPlus.setOnClickListener(v -> {
                if (mPlusClickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        mPlusClickListener.onPlusClick(v, pos);
                    }
                }
            });

            // Follow same logic for MINUS click listener
            tvMinus.setOnClickListener(v -> {
                if (mMinusClickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        mMinusClickListener.onMinusClick(v, pos);
                    }
                }
            });

            // Follow same logic for DELETE click listener
            btnDelete.setOnClickListener(v -> {
                if (mDeleteClickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        mDeleteClickListener.onDeleteClick(v, pos);
                    }
                }
            });
        }

    }

    // Setters to define what happens when delete, quantity, plus, and minus are clicked
    public void setDeleteClickListener(DeleteClickListener deleteClickListener) {
        this.mDeleteClickListener = deleteClickListener;
    }
    public void setQuantityClickListener(QuantityClickListener quantityClickListener) {
        this.mQuantityClickListener = quantityClickListener;
    }
    public void setPlusClickListener(PlusClickListener plusClickListener) {
        this.mPlusClickListener = plusClickListener;
    }
    public void setMinusClickListener(MinusClickListener minusClickListener) {
        this.mMinusClickListener = minusClickListener;
    }


    // Interface for each callback
    public interface DeleteClickListener {
        void onDeleteClick(View view, int position);
    }
    public interface QuantityClickListener {
        void onQuantityClick(View view, int position);
    }
    public interface PlusClickListener {
        void onPlusClick(View view, int position);
    }

    public interface MinusClickListener {
        void onMinusClick(View view, int position);
    }
}
