package com.dx168.patchsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dx168.patchsdk.bean.AppInfo;
import com.dx168.patchsdk.bean.PatchInfo;
import com.dx168.patchsdk.utils.DebugUtils;
import com.dx168.patchsdk.utils.DigestUtils;
import com.dx168.patchsdk.utils.PatchUtils;
import com.dx168.patchsdk.utils.SPUtils;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.dx168.patchsdk.utils.SPUtils.KEY_LOADED_PATCH;
import static com.dx168.patchsdk.utils.SPUtils.KEY_PATCHED_PATCH;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public final class PatchManager {

    private static final String TAG = "PatchManager";

    private static PatchManager instance;

    public static PatchManager getInstance() {
        if (instance == null) {
            instance = new PatchManager();
        }
        return instance;
    }

    public void free() {
        instance = null;
        PatchServer.get().free();
    }

    private Context context;
    private List<Listener> listeners = new ArrayList<>();
    private String patchDirPath;
    private AppInfo appInfo;

    /**
     * may be reset by gc
     */
    private boolean isDebugPatch = false;

    public void init(Context context, String baseUrl, String appId, String appSecret) {
        this.context = context;
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        if (!TextUtils.isEmpty(baseUrl) && !baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        appInfo = new AppInfo();
        appInfo.setAppId(appId);
        appInfo.setAppSecret(appSecret);
        appInfo.setToken(DigestUtils.md5DigestAsHex(appId + "_" + appSecret));
        appInfo.setDeviceId(PatchUtils.getDeviceId(context));
        appInfo.setPackageName(context.getPackageName());
        PatchServer.init(baseUrl);
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            appInfo.setVersionName(pkgInfo.versionName);
            appInfo.setVersionCode(pkgInfo.versionCode);
            String sdkDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + "/patchsdk";
            patchDirPath = sdkDirPath + "/" + appInfo.getVersionName();
            File sdkDir = new File(sdkDirPath);
            if (sdkDir.exists()) {
                for (File patchDir : sdkDir.listFiles()) {
                    if (TextUtils.equals(appInfo.getVersionName(), patchDir.getName())) {
                        continue;
                    }
                    patchDir.delete();
                }
                SharedPreferences sp = SPUtils.getSharedPreferences(context);
                Set<String> spKeys = sp.getAll().keySet();
                SharedPreferences.Editor editor = sp.edit();
                for (String key : spKeys) {
                    if (key.startsWith(appInfo.getVersionName())
                            || TextUtils.equals(KEY_LOADED_PATCH, key)
                            || TextUtils.equals(KEY_PATCHED_PATCH, key)) {
                        continue;
                    }
                    editor.remove(key);
                }
                editor.commit();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void register(Listener listener) {
        listeners.add(listener);
        Runnable r = mTasks.poll();
        while (r != null) {
            r.run();
            r = mTasks.poll();
        }
    }

    public void unregister(Listener listener) {
        listeners.remove(listener);
    }

    public void setTag(String tag) {
        if (context == null) {
            return;
            //throw new NullPointerException("PatchManager must be init before using");
        }
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        appInfo.setTag(tag);
    }

    public void setChannel(String channel) {
        if (context == null) {
            return;
            //throw new NullPointerException("PatchManager must be init before using");
        }
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        appInfo.setChannel(channel);
    }

    public void queryAndPatch() {
        if (context == null) {
            throw new NullPointerException("PatchManager must be init before using");
        }
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        final String loadedPatchPath = SPUtils.get(context, KEY_LOADED_PATCH, "");
        File debugPatch = DebugUtils.findDebugPatch(appInfo);
        if (debugPatch != null && TextUtils.equals(loadedPatchPath, debugPatch.getAbsolutePath())) {
            Toast.makeText(context, "已应用成功调试补丁 " + debugPatch.getName(), Toast.LENGTH_LONG).show();
            return;
        }
        if (debugPatch != null) {
            isDebugPatch = true;
            Toast.makeText(context, "开始应用调试补丁", Toast.LENGTH_LONG).show();
            DebugUtils.sendNotification(context, "开始应用调试补丁");
            TinkerInstaller.onReceiveUpgradePatch(context, debugPatch.getAbsolutePath());
            for (Listener listener : listeners) {
                listener.onQuerySuccess(debugPatch.getAbsolutePath());
            }
            return;
        }

        PatchServer.get()
                .queryPatch(appInfo.getAppId(), appInfo.getToken(), appInfo.getTag(),
                        appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getPlatform(),
                        appInfo.getOsVersion(), appInfo.getModel(), appInfo.getChannel(),
                        appInfo.getSdkVersion(), appInfo.getDeviceId(), new PatchServer.PatchServerCallback() {
                            @Override
                            public void onSuccess(int code, byte[] bytes) {
                                if (bytes == null) {
                                    for (Listener listener : listeners) {
                                        listener.onQueryFailure(new Exception("response is null, code=" + code));
                                    }
                                    return;
                                }
                                String response = new String(bytes);
                                PatchInfo patchInfo = PatchUtils.convertJsonToPatchInfo(response);
                                if (patchInfo == null) {
                                    for (Listener listener : listeners) {
                                        listener.onQueryFailure(new Exception("can not parse response to object: " + response + ", code=" + code));
                                    }
                                    return;
                                }
                                int resCode = patchInfo.getCode();
                                if (resCode != 200) {
                                    for (Listener listener : listeners) {
                                        listener.onQueryFailure(new Exception("code=" + resCode));
                                    }
                                    return;
                                }
                                for (Listener listener : listeners) {
                                    listener.onQuerySuccess(patchInfo.toString());
                                }
                                if (patchInfo.getData() == null) {
                                    File patchDir = new File(patchDirPath);
                                    if (patchDir.exists()) {
                                        patchDir.delete();
                                    }
                                    TinkerInstaller.cleanPatch(context);
                                    return;
                                }
                                String newPatchPath = getPatchPath(patchInfo.getData());
                                if (TextUtils.equals(loadedPatchPath, newPatchPath)) {
                                    return;
                                }
                                File patchDir = new File(patchDirPath);
                                if (patchDir.exists()) {
                                    for (File patch : patchDir.listFiles()) {
                                        String patchName = getPatchName(patchInfo.getData());
                                        if (TextUtils.equals(patch.getName(), patchName)) {
                                            if (!checkPatch(patch, patchInfo.getData().getHash())) {
                                                Log.e(TAG, "cache patch's hash is wrong");
                                                for (Listener listener : listeners) {
                                                    listener.onDownloadFailure(new Exception("cache patch's hash is wrong"));
                                                }
                                                return;
                                            }
                                            TinkerInstaller.onReceiveUpgradePatch(context, patch.getAbsolutePath());
                                            return;
                                        }
                                    }
                                }
                                downloadAndApplyPatch(newPatchPath, patchInfo);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (e != null) {
                                    e.printStackTrace();
                                }
                                for (Listener listener : listeners) {
                                    listener.onQueryFailure(e);
                                }
                            }
                        });
    }

    private void downloadAndApplyPatch(final String newPatchPath, final PatchInfo patchInfo) {
        PatchServer.get()
                .downloadPatch(patchInfo.getData().getDownloadUrl(), new PatchServer.PatchServerCallback() {
                    @Override
                    public void onSuccess(int code, byte[] bytes) {
                        if (!checkPatch(bytes, patchInfo.getData().getHash())) {
                            Log.e(TAG, "downloaded patch's hash is wrong: " + new String(bytes));

                            for (Listener listener : listeners) {
                                listener.onDownloadFailure(new Exception("download patch's hash is wrong"));
                            }
                            return;
                        }
                        try {
                            PatchUtils.writeToDisk(bytes, newPatchPath);
                            for (Listener listener : listeners) {
                                listener.onDownloadSuccess(newPatchPath);
                            }
                            TinkerInstaller.onReceiveUpgradePatch(context, newPatchPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            for (Listener listener : listeners) {
                                listener.onDownloadFailure(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        for (Listener listener : listeners) {
                            listener.onDownloadFailure(e);
                        }
                    }
                });
    }

    private boolean checkPatch(File patch, String hash) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        byte[] bytes = null;
        try {
            fis = new FileInputStream(patch);
            bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            bytes = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return checkPatch(bytes, hash);
    }

    private boolean checkPatch(byte[] bytes, String hash) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        String downloadFileHash = DigestUtils.md5DigestAsHex(appInfo.getAppId() + "_" + appInfo.getAppSecret() + "_" + DigestUtils.md5DigestAsHex(bytes));
        return TextUtils.equals(downloadFileHash, hash);
    }

    private String getPatchPath(PatchInfo.Data data) {
        return patchDirPath + "/" + getPatchName(data);
    }

    private String getPatchName(PatchInfo.Data data) {
        return data.getPatchVersion() + "_" + data.getUid() + ".apk";
    }

    private String getUid(String patchPath) {
        return patchPath.substring(patchPath.lastIndexOf("_") + 1, patchPath.length() - 4);
    }

    /**
     * 补丁合成成功
     *
     * @param patchPath
     */
    public void onPatchSuccess(String patchPath) {
        if (patchPath.endsWith("/patch.apk")) {
            patchPath = patchPath.substring(0, patchPath.lastIndexOf("/")) + ".apk";
        }
        SPUtils.put(context, KEY_PATCHED_PATCH, patchPath);
        if (isDebugPatch) {
            String msg = "调试补丁合成成功";
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            DebugUtils.sendNotification(context, msg);
        }
        for (Listener listener : listeners) {
            listener.onPatchSuccess();
        }
    }

    /**
     * 补丁合成失败
     *
     * @param patchPath
     */
    public void onPatchFailure(String patchPath) {
        if (patchPath.endsWith("/patch.apk")) {
            patchPath = patchPath.substring(0, patchPath.lastIndexOf("/")) + ".apk";
        }
        if (isDebugPatch) {
            String msg = "调试补丁合成失败";
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            DebugUtils.sendNotification(context, msg);
        } else {
            report(patchPath, false);
        }
        for (Listener listener : listeners) {
            listener.onPatchFailure();
        }
    }

    private Queue<Runnable> mTasks = new LinkedList<>();

    /**
     * 补丁应用成功
     */
    public void onLoadSuccess() {
        if (context == null) {
            mTasks.add(new Runnable() {
                @Override
                public void run() {
                    onLoadSuccessInternal();
                }
            });
        } else {
            onLoadSuccessInternal();
        }
    }

    private void onLoadSuccessInternal() {
        for (Listener listener : listeners) {
            try {
                listener.onLoadSuccess();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String patchPath = SPUtils.get(context, KEY_PATCHED_PATCH, "");
        if (TextUtils.isEmpty(patchPath)) {
            return;
        }
        SPUtils.put(context, KEY_LOADED_PATCH, patchPath);
        if (isDebugPatch) {
            String msg = "调试补丁应用成功";
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            DebugUtils.sendNotification(context, msg);
        } else {
            report(patchPath, true);
        }
    }

    /**
     * 补丁应用失败
     */
    public void onLoadFailure() {
        if (context == null) {
            mTasks.add(new Runnable() {
                @Override
                public void run() {
                    onLoadFailureInternal();
                }
            });
        } else {
            onLoadFailureInternal();
        }
    }

    private void onLoadFailureInternal() {
        for (Listener listener : listeners) {
            try {
                listener.onLoadFailure();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String patchPath = SPUtils.get(context, KEY_PATCHED_PATCH, "");
        if (TextUtils.isEmpty(patchPath)) {
            return;
        }
        if (isDebugPatch) {
            String msg = "调试补丁应用失败";
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            DebugUtils.sendNotification(context, msg);
        } else {
            report(patchPath, false);
        }
    }

    private static final int SUCCESS_REPORTED = 1;
    private static final int FAILURE_REPORTED = 2;

    private void report(String patchPath, final boolean result) {
        final String patchName = appInfo.getVersionName() + "_" + patchPath;
        int reportFlag = SPUtils.get(context, patchName, -1);
        /**
         * 如果已经上报过成功，不管本次是否修复成功，都不上报
         * 如果已经上报过失败，且本次修复成功，则上报成功
         * 如果已经上报过失败，且本次修复失败，则不上报
         */
        if (reportFlag == SUCCESS_REPORTED || (!result && reportFlag == FAILURE_REPORTED)) {
            return;
        }
        PatchServer.get()
                .report(appInfo.getAppId(), appInfo.getToken(), appInfo.getTag(),
                        appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getPlatform(),
                        appInfo.getOsVersion(), appInfo.getModel(), appInfo.getChannel(),
                        appInfo.getSdkVersion(), appInfo.getDeviceId(), getUid(patchPath),
                        result, new PatchServer.PatchServerCallback() {
                            @Override
                            public void onSuccess(int code, byte[] bytes) {
                                int reportApplyFlag;
                                if (result) {
                                    reportApplyFlag = SUCCESS_REPORTED;
                                } else {
                                    reportApplyFlag = FAILURE_REPORTED;
                                }
                                SPUtils.put(context, patchName, reportApplyFlag);
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
    }

}
