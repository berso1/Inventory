package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;


/**
 * Created by berso on 6/4/17.
 */


/**
 * {@link ContentProvider} for Pets app.
 */
public class InventoryProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the pets table */
    private static final int ITEMS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int ITEM_ID = 101;

    private InventoryDbHelper mDbHelper;





    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY, ITEMS);


        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // For the ITEMS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

//INSERT------------------------------------------------------------------

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name.equals("")){
            throw new IllegalArgumentException("Item requires a name");
        }

        String price = values.getAsString(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Item requires a price");
        }

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

//UPDATE--------------------------------------------------------------------------

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, values,selection,selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

//DELETE---------------------------------------------------------------------------

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted =0;
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted > 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                getContext().getContentResolver().notifyChange(uri,null);
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted > 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    //GET TYPE------------------------------------------------------------------------

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
