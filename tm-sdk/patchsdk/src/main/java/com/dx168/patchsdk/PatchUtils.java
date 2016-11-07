package com.dx168.patchsdk;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jianjun.lin on 2016/10/27.
 */
public class PatchUtils {

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        String deviceId = telephonyManager.getDeviceId();
        return TextUtils.isEmpty(deviceId) ? "" : deviceId;
    }

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService("phone");
        String number = tManager.getLine1Number();
        return TextUtils.isEmpty(number) ? "" : number;
    }


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

    public static String md5(String string) {
        return md5(string.getBytes());
    }

    public static String md5(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(bytes);
        return new BigInteger(1, md.digest()).toString(16);
    }

}
