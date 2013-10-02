package com.nurun.activemtl.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class BitmapUtil {

    public static Bitmap getResizedBitmap(Uri fileUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileUri.getPath(), options);
        options.inSampleSize = calculateInSampleSize(options, 480, 320);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileUri.getPath(), options);
    }
    
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
