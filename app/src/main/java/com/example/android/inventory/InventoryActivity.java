package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;
import com.example.android.inventory.data.InventoryCursorAdapter;
import com.example.android.inventory.data.InventoryDbHelper;
import com.example.android.inventory.data.InvetoryUtilities;


//Display a list of items of the inventory
public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //GLOBAL VARIABLES------------------------------------------------------------------------------

    private InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
    // The adapter that binds our data to the ListView
    InventoryCursorAdapter mCursorAdapter;
    private static final int IMAGE_LOADER = 1;
    // The callbacks through which we will interact with the LoaderManager.

    //this variable is used to define if the product have a photo or not, to avoid a crash.
    private String photo;

    //CONSTRUCTOR-----------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        photo = "noPhoto";


        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                intent.putExtra("photo",photo);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this,null);
        listView.setAdapter(mCursorAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                Uri currentProduct = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                Cursor item =   (Cursor) mCursorAdapter.getItem(position);
                int quantityColumnIndex = item.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
                int thumbnailColumnIndex = item.getColumnIndex(InventoryEntry.COLUMN_IMAGE);
                int currentQuantity = item.getInt(quantityColumnIndex);
                String currentImage = item.getString(thumbnailColumnIndex);
                intent.setData(currentProduct);
                intent.putExtra("quantity",currentQuantity);
                intent.putExtra("photo",currentImage);
                startActivity(intent);

            }
        });

        getLoaderManager().initLoader(IMAGE_LOADER,null, this);

    }

    //LOADER METHODS--------------------------------------------------------------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

         String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_SOLD,
                InventoryEntry.COLUMN_IMAGE
        };

        return new CursorLoader(this, InventoryEntry.CONTENT_URI,
                projection, null, null, null);
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    //MENU METHODS----------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                Toast.makeText(this, getString(R.string.editor_insert_Item_successful),
                        Toast.LENGTH_SHORT).show();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //INSERT DUMMY DATA-----------------------------------------------------------------------------
    private void insertItem() {
        //Dummy value variables
        String productName = InvetoryUtilities.generateProductName();
        int quantity = InvetoryUtilities.generateQuantity();
        float price = InvetoryUtilities.generatePrice();
        String productImage = "noPhoto";

        //Generate Content Values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, "product description");
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity );
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price );
        values.put(InventoryEntry.COLUMN_IMAGE, productImage );

        //Insert row
        getContentResolver().insert(InventoryEntry.CONTENT_URI,values);
    }

    //DELETE ALL PRODUCTS---------------------------------------------------------------------------
    // Prompt the user to confirm that they want to delete this pet.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_items_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Delete the product from the database.
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Uri uri = InventoryEntry.CONTENT_URI;
        int rowsDeleted = getContentResolver().delete(uri, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_Items_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }
}


