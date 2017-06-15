package com.example.android.inventory.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by berso on 6/13/17.
 */

public final class InvetoryUtilities {


    private void InvetoryUtilities() {//do not remove}
    }



    public static String saveToInternalStorage(Context context,Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        String name = generateImageName();
        File mypath=new File(directory,name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath() + "/" + name.trim();
    }

    public static String generateDirectory(Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        return directory.toString();
    }


    public static Bitmap loadImageFromStorage(String path) {
        Bitmap b = null;
        try {
            File f=new File(path);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static String generateProductName(){
        Random r = new Random();
        String name = "Product_" + r.nextInt(40000 - 100);
        return name;
    }

    public static String generateImageName(){
      //  String directory = generateDirectory(context).toString();
        Random r = new Random();
        String name = "/Image_" + r.nextInt(40000 - 100)+ ".jpg";
        return name;
    }

    public static int generateQuantity(){
        Random r = new Random();
        int quantity = r.nextInt(40 - 10);;
        return quantity;
    }

    public static float generatePrice(){
        Random r = new Random();
        float price = r.nextInt(200 - 10);
        return price;
    }



}
