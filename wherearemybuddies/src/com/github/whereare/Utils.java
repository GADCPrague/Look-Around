package com.github.whereare;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 *
 * @author radim
 */
public class Utils {

    public static String formatDistance(float distance) {
        if (distance < 1000f) {
            return Float.toString(distance) + "m";
        } else if (distance < 10000f) {
            return formatDec(distance / 1000f, 1) + "km";
        } else {
            return ((int) (distance / 1000f)) + "km";
        }
    }

    private static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
}
