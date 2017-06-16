package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.R;
import com.example.android.inventory.data.InventoryContract.InventoryEntry;


/**
 * Created by berso on 6/7/17.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private static final String TAG = InventoryCursorAdapter.class.getSimpleName();;

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //prepare ListView objects
        ImageView product_thumbnail = (ImageView) view.findViewById(R.id.product_thumbnail);
        TextView product_name = (TextView) view.findViewById(R.id.inventory_item_name_text);
        TextView product_quantity = (TextView) view.findViewById(R.id.inventory_item_current_quantity_text);
        final TextView product_sold = (TextView) view.findViewById(R.id.inventory_item_current_sold_text);
        TextView price_textview = (TextView) view.findViewById(R.id.price);
        Button product_price = (Button) view.findViewById(R.id.sale_button);

        //get column indexes
        int thumbnailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int salesColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SOLD);

        int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));

        String price = cursor.getString(priceColumnIndex);
        if(TextUtils.isEmpty(price)){
            price = "0";
        }

        //set variables with cursor info for each listview
        String productImage = cursor.getString(thumbnailColumnIndex);
        final String productName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int products_sold = cursor.getInt(salesColumnIndex);
        String productQuantity = String.valueOf(cursor.getInt(quantityColumnIndex)) + " Inventory";
        String productPrice = "Price $" + price;
        String productSold = String.valueOf( cursor.getInt(salesColumnIndex) )+ " Sold";

        //generate Uri
        final Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        //set variables on ListView objects
        product_name.setText(productName);
        price_textview.setText(productPrice);
        product_quantity.setText(productQuantity);
        product_sold.setText(productSold);

        //check if product have a photo or set default
        if (!productImage.equals("noPhoto")){
            product_thumbnail.setImageBitmap(InvetoryUtilities.loadImageFromStorage(productImage));
        }else{
            product_thumbnail.setImageResource(R.drawable.ic_photo_camera_black_24dp);
        }

        //listener to register sell products
        product_price.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    int qq = quantity;
                    int yy = products_sold;
                    Log.d(TAG, "new quabtity= " + qq);
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, --qq);
                    values.put(InventoryEntry.COLUMN_PRODUCT_SOLD, ++yy);
                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentProductUri, null);
                } else {
                    Toast.makeText(context, "Item out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
