package com.dx168.patchsdk.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.dx168.patchsdk.bean.PatchInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.dx168.patchsdk.PatchManager.JIAGU_PATCH_NAME;

/**
 * Created by jianjun.lin on 2016/10/27.
 */
public class PatchUtils {

    private static final String TAG = "patchsdk.PatchUtils";
    private static String processName = null;

    public static String getDeviceId(Context context) {
        String deviceId;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        } catch (SecurityException e) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return TextUtils.isEmpty(deviceId) ? "" : deviceId;
    }

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String number = tManager.getLine1Number();
        return TextUtils.isEmpty(number) ? "" : number;
    }

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

    public static String release(String patchPath) {
        try {
            ZipFile zipFile = new ZipFile(patchPath);
            boolean isFullPatch = zipFile.getEntry("FULL_PATCH") != null;
            if (!isFullPatch) {
                return patchPath;
            }
            String dir = patchPath.substring(0, patchPath.length() - 4);
            patchPath = dir + "/" + JIAGU_PATCH_NAME;
            if (new File(dir).exists()) {
                return patchPath;
            }
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.startsWith("classes") && name.endsWith(".dex")) {
                    FileUtils.copyFile(zipFile.getInputStream(zipEntry), dir + "/dex/" + name);
                }
            }
            FileUtils.copyFile(zipFile.getInputStream(zipFile.getEntry(JIAGU_PATCH_NAME)), patchPath);
            return patchPath;
        } catch (IOException e) {
            e.printStackTrace();
            return patchPath;
        }
    }

    public static boolean isMainProcess(Context context) {
        String pkgName = context.getPackageName();
        String processName = getProcessName(context);
        if (processName == null || processName.length() == 0) {
            processName = "";
        }
        return pkgName.equals(processName);
    }

    /**
     * add process name cache
     *
     * @param context
     * @return
     */
    public static String getProcessName(final Context context) {
        if (processName != null) {
            return processName;
        }
        //will not null
        processName = getProcessNameInternal(context);
        return processName;
    }

    private static String getProcessNameInternal(final Context context) {
        int myPid = android.os.Process.myPid();

        if (context == null || myPid <= 0) {
            return "";
        }

        ActivityManager.RunningAppProcessInfo myProcess = null;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            for (ActivityManager.RunningAppProcessInfo process : activityManager.getRunningAppProcesses()) {
                if (process.pid == myPid) {
                    myProcess = process;
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getProcessNameInternal exception:" + e.getMessage());
        }

        if (myProcess != null) {
            return myProcess.processName;
        }

        byte[] b = new byte[128];
        FileInputStream in = null;
        try {
            in = new FileInputStream("/proc/" + myPid + "/cmdline");
            int len = in.read(b);
            if (len > 0) {
                for (int i = 0; i < len; i++) { // lots of '0' in tail , remove them
                    if (b[i] > 128 || b[i] <= 0) {
                        len = i;
                        break;
                    }
                }
                return new String(b, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }

        return "";
    }

    public static PatchInfo toPatchInfo(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            PatchInfo patchInfo = new PatchInfo();
            patchInfo.setCode(jsonObject.optInt("code"));
            patchInfo.setMessage(jsonObject.optString("message"));
            JSONObject dataJSONObject = jsonObject.optJSONObject("data");
            if (dataJSONObject != null) {
                PatchInfo.Data data = new PatchInfo.Data();
                data.setVersionName(dataJSONObject.optString("versionName"));
                data.setUid(dataJSONObject.optString("uid"));
                data.setPatchVersion(dataJSONObject.optString("patchVersion"));
                data.setDownloadUrl(dataJSONObject.optString("downloadUrl"));
                data.setPatchSize(dataJSONObject.optLong("patchSize"));
                data.setHash(dataJSONObject.optString("hash"));
                data.setHashJiagu(dataJSONObject.optString("hashJiagu"));
                data.setDownloadUrlJiagu(dataJSONObject.optString("downloadUrlJiagu"));
                patchInfo.setData(data);
            }

            JSONObject extraObj = jsonObject.optJSONObject("extra");
            patchInfo.setFullUpdateInfo(extraObj);
            return patchInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isMainProcessRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).processName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDebugPatch(String path) {
        return path.contains("/com.dx168.patchtool/");
    }

}
