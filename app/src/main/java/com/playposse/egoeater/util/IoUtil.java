package com.playposse.egoeater.util;

import android.content.Context;

import com.google.common.io.Files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility to help with input/output operations.
 */
public final class IoUtil {

    public static byte[] readRawResource(Context context, int resId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = context.getResources().openRawResource(resId);

        byte[] buffer = new byte[8192];
        int readSize;
        while ((readSize = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, readSize);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }
}
