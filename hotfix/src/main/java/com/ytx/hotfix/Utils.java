package com.ytx.hotfix;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Created by jianjun.lin on 2016/10/27.
 */
public class Utils {

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        String deviceId = telephonyManager.getDeviceId();
        return TextUtils.isEmpty(deviceId) ? "" : deviceId;
    }

    public static boolean isX86CPU() {
        return Build.CPU_ABI.toLowerCase().contains("x86");
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService("phone");
        String number = tManager.getLine1Number();
        return TextUtils.isEmpty(number) ? "" : number;
    }


    public static boolean writeToDisk(ResponseBody body, String targetPath) {
        try {
            File targetFile = new File(targetPath);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] buf = new byte[32 * 1024];
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(targetFile);
                while (true) {
                    int read = inputStream.read(buf);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(buf, 0, read);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

}
