package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by berso on 6/7/17.
 */

public class InventoryContract {

    private InventoryContract() {//do not remove
    }


    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

        public static final String PATH_INVENTORY = "inventory";



    public static class InventoryEntry implements BaseColumns {

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of itemds.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        String x = CONTENT_URI.toString();
      //  Log.v("CONTENT_URI=",x);

        public static final String TABLE_NAME = "Inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRODUCT_DESCRIPTION = "product_description";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_SOLD = "sold";
        public static final String COLUMN_IMAGE = "image";


    }


}
