package com.example.android.inventory;

/**
 * Created by berso on 6/8/17.
 */

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;
import com.example.android.inventory.data.InventoryDbHelper;
import com.example.android.inventory.data.InvetoryUtilities;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


//SAVE ITEM-----------------------------------------------------------------------------------------

    private static final int CAMERA_REQUEST = 1;
    private static final int ITEM_LOADER = 1;

    private EditText mNameEditText;
    private EditText mDescEditText;
    private EditText mQuantityEditText;
    private EditText mPriceText;
    private EditText mSoldText;
    private ImageView imageView;


    private int mQuantity;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String photo;
    private Uri mCurrentPetUri;
    private String mProduct;


    //String to control the mode EDIT or ADD
    private String mMode;

    private boolean mItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

//CONSTRUCTOR---------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Gets the data repository in write mode
        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
        Intent intent = getIntent();
        Uri curretnPetUri = intent.getData();
        mQuantity = intent.getIntExtra("quantity",0);
        Button insert = (Button) findViewById(R.id.insert);
        Button delete = (Button) findViewById(R.id.delete);
        Button order  = (Button) findViewById(R.id.order);
        photo = intent.getStringExtra("photo");
        if(curretnPetUri == null){
            //ADD MODE
            setTitle(R.string.editor_activity_title_new_item);
            invalidateOptionsMenu();
            mMode = "ADD";
            delete.setVisibility(View.GONE);
            order.setVisibility(View.GONE);
        }else{
            //EDIT MODE
            mMode = "EDIT";
            setTitle(R.string.editor_activity_title_edit_Item);
            mCurrentPetUri = curretnPetUri;
            getLoaderManager().initLoader(ITEM_LOADER,null, this);
            insert.setText("UPDATE");

        }
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescEditText = (EditText) findViewById(R.id.edit_product_desc);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceText = (EditText) findViewById(R.id.edit_product_price);
        mSoldText = (EditText) findViewById(R.id.edit_product_sold);
        imageView = (ImageView) findViewById(R.id.image_view);
        Button plusButton = (Button) findViewById(R.id.plus_button);
        Button minusButton = (Button) findViewById(R.id.minus_button);


        mNameEditText.setOnTouchListener(mTouchListener);
        mDescEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceText.setOnTouchListener(mTouchListener);
        mSoldText.setOnTouchListener(mTouchListener);
        imageView.setOnTouchListener(mTouchListener);
        plusButton.setOnTouchListener(mTouchListener);
        minusButton.setOnTouchListener(mTouchListener);

        //listener to take photo
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        });

        //insert (update) listener
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
            }
        });

        //delete listener
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        //order listener
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderProduct();
            }
        });

    }


//RESULT FROM CAMERA REQUEST------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            imageView.setImageBitmap(image);
            photo = "yes";
        }
    }

//BUTTON METHODS:

//SAVE ITEM-----------------------------------------------------------------------------------------
    private void saveProduct() {

        String nameString = mNameEditText.getText().toString().trim();
        String descString = mDescEditText.getText().toString().trim();
        String price = "0.0";
        price = mPriceText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();
        String sold = mSoldText.getText().toString().trim();
        String image = "noPhoto";

        //check if product have a name
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.product_no_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //test if ther's a Bitmap to save
        if (!image.equals(photo)) {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            image = InvetoryUtilities.saveToInternalStorage(getApplicationContext(), bitmap);
        }
// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        Uri newUri = null;
        String mtoast = "";
        if (!TextUtils.isEmpty(nameString)) {
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, descString);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
            values.put(InventoryEntry.COLUMN_PRODUCT_SOLD, sold);
            values.put(InventoryEntry.COLUMN_IMAGE, image);

            switch (mMode) {
                case "ADD":
                    newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                    mtoast = getString(R.string.editor_insert_Item_successful);
                    break;
                case "EDIT":
                    int updateRows = getContentResolver().update(mCurrentPetUri, values, null, null);
                    mtoast = getString(R.string.editor_update_Item_successful);
                    break;
            }
        }

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null && mCurrentPetUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_Item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, mtoast,
                    Toast.LENGTH_SHORT).show();
            //Close activity
            finish();
        }
    }

//DELETE ITEM----------------------------------------------------------------------------------------

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentPetUri != null) {
            // Call the ContentResolver to delete the productat the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the productthat we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

//ORDER---------------------------------------------------------------------------------------------

    private void orderProduct() {
        String[] TO = {"orders@"+mProduct+"_Supplier.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order " + mProduct);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please ship " + mProduct +
                " in quantities 50");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }
//LOADER--------------------------------------------------------------------------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_SOLD,
                InventoryEntry.COLUMN_IMAGE};
        return new CursorLoader(this, mCurrentPetUri,projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int descColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int soldColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SOLD);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            int sold = cursor.getInt(soldColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescEditText.setText(desc);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceText.setText((Float.toString(price)));
            mSoldText.setText(Integer.toString(sold));
            if (!image.equals("noPhoto")){
                imageView.setImageBitmap(InvetoryUtilities.loadImageFromStorage(image));
            }

            mProduct = name;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescEditText.setText("");
        mQuantityEditText.setText("");
        mPriceText.setText("");
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

//QUANTITY + - CONTROLS---------------------------------------------------------------------------------

    public void increment(View view) {
        mQuantity++;
        mQuantityEditText.setText(Integer.toString(mQuantity));
    }


    public void decrement(View view) {
        mQuantity--;
        if (mQuantity < 1) {
            mQuantity = 0;
            Toast.makeText(this, "O products", Toast.LENGTH_SHORT).show();
        }
        mQuantityEditText.setText(Integer.toString(mQuantity));
    }



//MENU----------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                return true;

            case android.R.id.home:
                //if user didnt make changes
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                //User has made som change

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        //Go back if we have no changes
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        //otherwise Protect user from loosing info
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
}
