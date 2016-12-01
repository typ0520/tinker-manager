package com.dx168.patchtool.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jianjun.lin on 2016/11/30.
 */

public class FileUtils {

    public static void writeToDisk(byte[] bytes, String targetPath) throws IOException {
        File tmpFile = new File(targetPath + ".tmp");
        if (!tmpFile.getParentFile().exists()) {
            tmpFile.getParentFile().mkdirs();
        }
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tmpFile);
            outputStream.write(bytes, 0, bytes.length);
            outputStream.flush();
            tmpFile.renameTo(new File(targetPath));
        } finally {
            tmpFile.delete();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
