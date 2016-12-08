package com.dx168.patchsdk;

import android.content.Context;
import android.content.Intent;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public static void free() {
        instance = null;
        PatchServer.free();
    }

    public static final String SP_NAME = "patchsdk";
    public static final String SP_KEY_USING_PATCH = "using_patch";

    private Context context;
    private ActualPatchManager apm;
    private String patchDirPath;
    private AppInfo appInfo;

    /**
     * may be reset by gc
     */
    private Map<String, PatchInfo> patchInfoMap = new HashMap<>();
    private boolean isDebugPatch = false;

    public void init(Context context, String baseUrl, String appId, String appSecret, ActualPatchManager apm) {
        this.context = context;
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        if (!TextUtils.isEmpty(baseUrl) && !baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        this.apm = apm;
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
            String hotFixDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + context.getPackageName() + File.separator + "patchsdk";
            patchDirPath = hotFixDirPath + File.separator + appInfo.getVersionName();
            File hotFixDir = new File(hotFixDirPath);
            if (hotFixDir.exists()) {
                for (File patchDir : hotFixDir.listFiles()) {
                    if (TextUtils.equals(appInfo.getVersionName(), patchDir.getName())) {
                        continue;
                    }
                    patchDir.delete();
                }
                SharedPreferences sp = context.getSharedPreferences(PatchManager.SP_NAME, Context.MODE_PRIVATE);
                Set<String> spKeys = sp.getAll().keySet();
                SharedPreferences.Editor editor = sp.edit();
                for (String key : spKeys) {
                    if (key.startsWith(appInfo.getVersionName()) || TextUtils.equals(SP_KEY_USING_PATCH, key)) {
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

    private PatchListener patchListener;

    public void queryAndApplyPatch() {
        queryAndApplyPatch(null);
    }

    public void queryAndApplyPatch(PatchListener listener) {
        if (context == null) {
            throw new NullPointerException("PatchManager must be init before using");
        }
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        this.patchListener = listener;

        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        final String usingPatchPath = sp.getString(SP_KEY_USING_PATCH, "");
        File debugPatch = DebugUtils.findDebugPatch(appInfo);
        if (debugPatch != null && TextUtils.equals(usingPatchPath, debugPatch.getAbsolutePath())) {
            Toast.makeText(context, "已应用成功调试补丁 " + debugPatch.getName(), Toast.LENGTH_LONG).show();
            return;
        }
        if (debugPatch != null) {
            isDebugPatch = true;
            Toast.makeText(context, "开始应用调试补丁", Toast.LENGTH_LONG).show();
            DebugUtils.sendNotify(context, "开始应用调试补丁");
            apm.cleanPatch(context);
            apm.applyPatch(context, debugPatch.getAbsolutePath());
            if (patchListener != null) {
                patchListener.onQuerySuccess(debugPatch.getAbsolutePath());
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
                                    return;
                                }
                                String response = new String(bytes);
                                PatchInfo patchInfo = PatchUtils.convertJsonToPatchInfo(response);
                                if (patchInfo == null) {
                                    if (patchListener != null) {
                                        patchListener.onQueryFailure(new Exception("can not parse response to object: " + response));
                                    }
                                    return;
                                }
                                if (patchInfo.getCode() != 200) {
                                    if (patchListener != null) {
                                        patchListener.onQueryFailure(new Exception("code=" + patchInfo.getCode()));
                                    }
                                    return;
                                }
                                if (patchListener != null) {
                                    patchListener.onQuerySuccess(patchInfo.toString());
                                }
                                if (patchInfo.getData() == null) {
                                    File patchDir = new File(patchDirPath);
                                    if (patchDir.exists()) {
                                        patchDir.delete();
                                    }
                                    apm.cleanPatch(context);
                                    if (patchListener != null) {
                                        patchListener.onCompleted();
                                    }
                                    return;
                                }
                                String newPatchPath = getPatchPath(patchInfo.getData());
                                if (TextUtils.equals(usingPatchPath, newPatchPath)) {
                                    if (patchListener != null) {
                                        patchListener.onCompleted();
                                    }
                                    return;
                                }
                                File patchDir = new File(patchDirPath);
                                if (patchDir.exists()) {
                                    for (File patch : patchDir.listFiles()) {
                                        String patchName = getPatchName(patchInfo.getData());
                                        if (TextUtils.equals(patch.getName(), patchName)) {
                                            if (!checkPatch(patch, patchInfo.getData().getHash())) {
                                                Log.e(TAG, "cache patch's hash is wrong");
                                                if (patchListener != null) {
                                                    patchListener.onDownloadFailure(new Exception("cache patch's hash is wrong"));
                                                }
                                                return;
                                            }
                                            patchInfoMap.put(patch.getAbsolutePath(), patchInfo);
                                            apm.cleanPatch(context);
                                            apm.applyPatch(context, patch.getAbsolutePath());
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
                                if (patchListener != null) {
                                    patchListener.onQueryFailure(e);
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
                            Log.e(TAG, "downloaded patch's hash is wrong");
                            if (patchListener != null) {
                                patchListener.onDownloadFailure(new Exception("download patch's hash is wrong"));
                            }
                            return;
                        }
                        try {
                            PatchUtils.writeToDisk(bytes, newPatchPath);
                            if (patchListener != null) {
                                patchListener.onDownloadSuccess(newPatchPath);
                            }
                            patchInfoMap.put(newPatchPath, patchInfo);
                            apm.applyPatch(context, newPatchPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (patchListener != null) {
                                patchListener.onDownloadFailure(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        if (patchListener != null) {
                            patchListener.onDownloadFailure(e);
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
        return patchDirPath + File.separator + getPatchName(data);
    }

    private String getPatchName(PatchInfo.Data data) {
        return data.getPatchVersion() + "_" + data.getHash() + ".apk";
    }

    public void onApplySuccess(String patchPath) {
        SharedPreferences sp = context.getSharedPreferences(PatchManager.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PatchManager.SP_KEY_USING_PATCH, patchPath);
        editor.apply();
        Intent intent = new Intent(context, ApplyResultService.class);
        intent.putExtra("IS_APPLY_SUCCESS", true);
        if (isDebugPatch) {
            intent.putExtra("MSG", "应用调试补丁成功");
        } else {
            report(patchPath, true);
        }
        context.startService(intent);
    }

    public void onApplyFailure(String patchPath, String msg) {
        Intent intent = new Intent(context, ApplyResultService.class);
        intent.putExtra("IS_APPLY_SUCCESS", false);
        if (isDebugPatch) {
            intent.putExtra("MSG", "应用调试补丁失败");
        } else {
            report(patchPath, false);
        }
        context.startService(intent);
    }

    PatchListener getPatchListener() {
        return patchListener;
    }

    private static final int APPLY_SUCCESS_REPORTED = 1;
    private static final int APPLY_FAILURE_REPORTED = 2;

    private void report(String patchPath, final boolean applyResult) {
        PatchInfo patchInfo = patchInfoMap.get(patchPath);
        if (patchInfo == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(PatchManager.SP_NAME, Context.MODE_PRIVATE);
        final String patchName = appInfo.getVersionName() + "_" + getPatchName(patchInfo.getData());
        int reportApplyFlag = sp.getInt(patchName, -1);
        /**
         * 如果已经上报过成功，不管本次是否修复成功，都不上报
         * 如果已经上报过失败，且本次修复成功，则上报成功
         * 如果已经上报过失败，且本次修复失败，则不上报
         */
        if (reportApplyFlag == APPLY_SUCCESS_REPORTED
                || (!applyResult && reportApplyFlag == APPLY_FAILURE_REPORTED)) {
            return;
        }
        PatchServer.get()
                .report(appInfo.getAppId(), appInfo.getToken(), appInfo.getTag(),
                        appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getPlatform(),
                        appInfo.getOsVersion(), appInfo.getModel(), appInfo.getChannel(),
                        appInfo.getSdkVersion(), appInfo.getDeviceId(), patchInfo.getData().getUid(),
                        applyResult, new PatchServer.PatchServerCallback() {
                            @Override
                            public void onSuccess(int code, byte[] bytes) {
                                SharedPreferences sp = context.getSharedPreferences(PatchManager.SP_NAME, Context.MODE_PRIVATE);
                                int reportApplyFlag;
                                if (applyResult) {
                                    reportApplyFlag = APPLY_SUCCESS_REPORTED;
                                } else {
                                    reportApplyFlag = APPLY_FAILURE_REPORTED;
                                }
                                sp.edit().putInt(patchName, reportApplyFlag).apply();
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
        patchInfoMap.remove(patchPath);
    }

}
