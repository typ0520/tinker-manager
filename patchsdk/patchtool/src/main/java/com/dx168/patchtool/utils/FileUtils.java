package com.dx168.patchtool.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jianjun.lin on 2016/11/30.
 */

public class FileUtils {

    private static final String TAG = "patchtool.FileUtils";

    public static void writeToDisk(byte[] bytes, String targetPath) throws IOException {
        File tmpFile = new File(targetPath + ".tmp");
        if (!tmpFile.getParentFile().exists()) {
            tmpFile.getParentFile().mkdirs();
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(tmpFile);
            os.write(bytes, 0, bytes.length);
            os.flush();
            tmpFile.renameTo(new File(targetPath));
        } finally {
            tmpFile.delete();
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
