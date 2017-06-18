package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;

/**
 * Created by XY on 12.6.2017.
 */

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.current_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        final int rowUpdated = cursor.getInt(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry._ID));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sellProduct(context, quantityTextView, rowUpdated);
            }
        });

        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);

        String itemName = cursor.getString(nameColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);

        nameTextView.setText(itemName);
        quantityTextView.setText(String.valueOf(itemQuantity));
        priceTextView.setText(String.valueOf(itemPrice));
    }

    private static int sellProduct(Context context, TextView current_quantity, int rowUpdated) {
        int availableQuantity = Integer.parseInt(current_quantity.getText().toString());
        int rowsUpdated = 0;
        if (availableQuantity > 0){
            availableQuantity = availableQuantity -1;
            String quantityToString = Integer.toString(availableQuantity);
            ContentValues values = new ContentValues();
            values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, quantityToString);
            Uri uri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, rowUpdated);
            rowsUpdated = context.getContentResolver().update(uri, values, null, null);
        }

        else {
            Toast.makeText(context, R.string.quantity_below_zero, Toast.LENGTH_SHORT).show();
        }
        return rowsUpdated;
    }
}