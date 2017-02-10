package com.dx168.patchserver.core.utils;

import java.io.*;

/**
 * Created by tong on 17/2/9.
 */
public class StreamUtil {
    public static byte[] readStream(InputStream fis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = -1;
        byte[] buffer = new byte[1024];
        while ((len = fis.read(buffer)) != -1) {
            bos.write(buffer,0,len);
        }
        fis.close();
        return bos.toByteArray();
    }

    public static void writeTo(byte[] data,File file) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(data);
            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignored.
                }
            }
        }
    }
}
