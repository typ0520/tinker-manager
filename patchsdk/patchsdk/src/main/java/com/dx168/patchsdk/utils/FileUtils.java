package com.dx168.patchsdk.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jianjun.lin on 2017/1/16.
 */

public class FileUtils {

    private static final String TAG = "patchsdk.FileUtils";

    public static void copyFile(File source, String dest) throws IOException {
        copyFile(new FileInputStream(source), dest);
    }

    public static void copyFile(InputStream is, String dest) throws IOException {
        copyFile(is, new File(dest));
    }

    public static void copyFile(File source, File dest) throws IOException {
        copyFile(new FileInputStream(source), dest);
    }

    public static void copyFile(InputStream is, File dest) throws IOException {
        FileOutputStream os = null;
        File parent = dest.getParentFile();
        if (parent != null && (!parent.exists())) {
            parent.mkdirs();
        }
        try {
            os = new FileOutputStream(dest, false);
            byte[] buffer = new byte[16 * 1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            closeQuietly(is);
            closeQuietly(os);
        }
    }

    /**
     * Closes the given {@code Closeable}. Suppresses any IO exceptions.
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to close resource", e);
        }
    }

}
