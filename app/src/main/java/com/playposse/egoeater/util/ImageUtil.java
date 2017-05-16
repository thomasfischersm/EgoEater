package com.playposse.egoeater.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * A utility to deal with image data.
 */
public final class ImageUtil {

    private static final String LOG_TAG = ImageUtil.class.getSimpleName();

    private ImageUtil() {
    }

    public static byte[] convertBitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static Bitmap downScaleIfNecessary(Bitmap bitmap, int maxWidth, int maxHeight) {
        if ((bitmap.getHeight() > maxWidth) || (bitmap.getWidth() > maxWidth)) {
            double ratio0 = bitmap.getHeight() / (double) maxHeight;
            int width0 = (int) (bitmap.getWidth() / ratio0);
            int height0 = maxHeight;

            double ratio1 = bitmap.getWidth() / (double) maxWidth;
            int width1 = maxWidth;
            int height1 = (int) (bitmap.getHeight() / ratio1);

            if (height0 < height1) {
                Log.i(LOG_TAG, "downScaleIfNecessary: Downscale image from "
                        + bitmap.getWidth() + "," + bitmap.getHeight()
                        + " to " + width0 + "," + height0);
                bitmap = Bitmap.createScaledBitmap(bitmap, width0, height0, true);
            } else {
                Log.i(LOG_TAG, "downScaleIfNecessary: Downscale image from "
                        + bitmap.getWidth() + "," + bitmap.getHeight()
                        + " to " + width1 + "," + height1);
                bitmap = Bitmap.createScaledBitmap(bitmap, width1, height1, true);
            }
        }
        return bitmap;
    }
}
