package com.dx168.patchtool.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jianjun.lin on 2016/11/30.
 */

public class FileUtils {

    public static boolean writeToDisk(byte[] bytes, String targetPath) {
        try {
            File targetFile = new File(targetPath);
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(targetFile);
                outputStream.write(bytes, 0, bytes.length);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
