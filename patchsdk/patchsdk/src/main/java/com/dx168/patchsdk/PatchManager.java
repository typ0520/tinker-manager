package com.dx168.patchsdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import com.dx168.patchsdk.bean.AppInfo;
import com.dx168.patchsdk.bean.PatchInfo;
import com.dx168.patchsdk.utils.DebugUtils;
import com.dx168.patchsdk.utils.DigestUtils;
import com.dx168.patchsdk.utils.PatchUtils;
import com.dx168.patchsdk.utils.SPUtils;
import org.json.JSONException;
import org.json.JSONObject;
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
import static com.dx168.patchsdk.utils.SPUtils.KEY_STAGE;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public final class PatchManager {

    private static final String TAG = "patchsdk.PatchManager";

    public static final String JIAGU_PATCH_NAME = "patch.apk";

    private static final int SUCCESS_REPORTED = 1;
    private static final int FAILURE_REPORTED = 2;

    private static final String DEBUG_ACTION_PATCH_RESULT = "com.dx168.patchtool.PATCH_RESULT";
    private static final String DEBUG_ACTION_LOAD_RESULT = "com.dx168.patchtool.LOAD_RESULT";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_RESULT = "result";

    private static final int STAGE_IDLE = 0; //没有动作
    private static final int STAGE_QUERY = 1; //正在查询, 等待服务器返回
    private static final int STAGE_DOWNLOAD = 2; //正在下载
    private static final int STAGE_PATCH = 3; //正在修复

    //返回码前缀
    public static final String ERROR_CODE_LOAD = "LoadError"; //
    public static final String ERROR_CODE_LOAD_EXCEPTION = "LoadException"; //
    public static final String ERROR_PACKAGE_CHECK = "PackageCheck"; //
    public static final String ERROR_CODE_PATCH_LISTENER = "PatchListener"; //
    public static final String ERROR_CODE_PATCH_RESULT = "PatchResult"; //

    /** 下载补丁时异常 */
    public static final int ERROR_DOWNLOAD_FAIL = -1;
    /** 检测下载补丁的签名时异常 */
    public static final int ERROR_DOWNLOAD_CHECK_FAIL = -2;
    /** 补丁在patchListener检测时异常 */
    public static final int ERROR_LISTENER_CHECK_FAIL = -3;
    /** 补丁合成异常 */
    public static final int ERROR_PATCH_FAIL = -4;
    /** 补丁加载异常 */
    public static final int ERROR_LOAD_FAIL = -5;

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
    private String versionDirPath;
    private AppInfo appInfo;
    private IPatchManager actualManager;
    private FullUpdateHandler fullUpdateHandler = new FullUpdateHandler();

    public void init(Context context, String baseUrl, String appId, String appSecret, IPatchManager actualManager) {
        this.context = context.getApplicationContext();
        this.actualManager = actualManager;
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
        appInfo = new AppInfo();
        appInfo.setAppId(appId);
        appInfo.setAppSecret(appSecret);
        appInfo.setToken(DigestUtils.md5DigestAsHex(appId + "_" + appSecret));
        appInfo.setDeviceId(PatchUtils.getDeviceId(context));
        appInfo.setPackageName(context.getPackageName());
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            appInfo.setVersionName(pkgInfo.versionName);
            appInfo.setVersionCode(pkgInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(baseUrl) && !baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        PatchServer.init(baseUrl);
        String patchDirPath = context.getFilesDir() + "/patch";
        versionDirPath = patchDirPath + "/" + appInfo.getVersionName();
        File patchDir = new File(patchDirPath);
        if (patchDir.exists()) {
            for (File versionDir : patchDir.listFiles()) {
                if (TextUtils.equals(appInfo.getVersionName(), versionDir.getName())) {
                    continue;
                }
                versionDir.delete();
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
    }

    public void setFullUpdateHandler(FullUpdateHandler fullUpdateHandler) {
        this.fullUpdateHandler = fullUpdateHandler;
    }

    public void register(Listener listener) {
        listeners.add(listener);
        Runnable r = loadResultQueue.poll();
        while (r != null) {
            r.run();
            r = loadResultQueue.poll();
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
        queryAndPatch(false);
    }

    public void queryAndPatch(final boolean withFullUpdateInfo) {
        if (context == null) {
            throw new NullPointerException("PatchManager must be init before using");
        }
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        int stage = SPUtils.get(context, KEY_STAGE, 0);
        if (stage > 0) {
            Log.e(TAG, "already started, stage is " + stage);
            return;
        }
        SPUtils.put(context, KEY_STAGE, STAGE_QUERY);
        final String loadedPatchPath = SPUtils.get(context, KEY_LOADED_PATCH, "");
        File debugPatch = DebugUtils.findDebugPatch(appInfo);
        if (debugPatch != null && TextUtils.equals(loadedPatchPath, debugPatch.getAbsolutePath())) {
            Log.d(TAG, "patch is working " + debugPatch);
            SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
            return;
        }
        if (debugPatch != null) {
            SPUtils.put(context, KEY_STAGE, STAGE_PATCH);
            actualManager.patch(context, debugPatch.getAbsolutePath());
            for (Listener listener : listeners) {
                listener.onQuerySuccess(debugPatch.getAbsolutePath());
            }
            return;
        }
        PatchServer.get()
                .queryPatch(appInfo.getAppId(), appInfo.getToken(), appInfo.getTag(),
                        appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getPlatform(),
                        appInfo.getOsVersion(), appInfo.getModel(), appInfo.getChannel(),
                        appInfo.getSdkVersion(), appInfo.getDeviceId(),withFullUpdateInfo, new PatchServer.PatchServerCallback() {
                            @Override
                            public void onSuccess(int code, byte[] bytes) {
                                if (bytes == null) {
                                    SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                                    for (Listener listener : listeners) {
                                        listener.onQueryFailure(new Exception("response is null, code=" + code));
                                    }
                                    return;
                                }
                                String response = new String(bytes);
                                PatchInfo patchInfo = PatchUtils.toPatchInfo(response);
                                if (patchInfo == null) {
                                    SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                                    for (Listener listener : listeners) {
                                        listener.onQueryFailure(new Exception("can not parse response to object: " + response + ", code=" + code));
                                    }
                                    return;
                                }
                                if (withFullUpdateInfo && fullUpdateHandler != null && patchInfo.getFullUpdateInfo() != null) {
                                    fullUpdateHandler.handleFullUpdate(context,patchInfo.getFullUpdateInfo());
                                }

                                int resCode = patchInfo.getCode();
                                if (resCode != 200) {
                                    SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                                    for (Listener listener : listeners) {
                                        listener.onQueryFailure(new Exception("code=" + resCode));
                                    }
                                    return;
                                }
                                for (Listener listener : listeners) {
                                    listener.onQuerySuccess(patchInfo.toString());
                                }
                                if (patchInfo.getData() == null) {
                                    File versionDir = new File(versionDirPath);
                                    if (versionDir.exists()) {
                                        versionDir.delete();
                                    }
                                    SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                                    actualManager.cleanPatch(context);
                                    return;
                                }
                                String newPatchPath = getPatchPath(patchInfo.getData());
                                if (TextUtils.equals(loadedPatchPath, newPatchPath)) {
                                    SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                                    return;
                                }
                                File versionDir = new File(versionDirPath);
                                if (versionDir.exists()) {
                                    for (File patch : versionDir.listFiles()) {
                                        String patchName = getPatchName(patchInfo.getData());
                                        if (TextUtils.equals(patch.getName(), patchName)) {
                                            String downloadPatchHash = patchInfo.getData().getHash();
                                            if (!checkPatch(patch, downloadPatchHash)) {
                                                SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                                                Log.e(TAG, "cache patch's hash is wrong");
                                                for (Listener listener : listeners) {
                                                    listener.onDownloadFailure(new Exception("cache patch's hash is wrong"));
                                                }
                                                return;
                                            }
                                            SPUtils.put(context, KEY_STAGE, STAGE_PATCH);
                                            actualManager.patch(context, patch.getAbsolutePath());
                                            return;
                                        }
                                    }
                                }
                                downloadAndPatch(newPatchPath, patchInfo);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (e != null) {
                                    e.printStackTrace();
                                }
                                SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                                for (Listener listener : listeners) {
                                    listener.onQueryFailure(e);
                                }
                            }
                        });
    }

    public void queryFullUpdateInfo() {
        if (context == null) {
            throw new NullPointerException("PatchManager must be init before using");
        }
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }

        PatchServer.get()
                .queryFullUpdateInfo(appInfo.getAppId(), appInfo.getToken(),
                        appInfo.getVersionName(), appInfo.getChannel(),
                        appInfo.getSdkVersion(), new PatchServer.PatchServerCallback() {
                            @Override
                            public void onSuccess(int code, byte[] bytes) {
                                if (bytes == null) {
                                    return;
                                }
                                String response = new String(bytes);
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response).getJSONObject("data");
                                    if (jsonObject == null) {
                                        throw new NullPointerException("full update info == null");
                                    }
                                } catch (Throwable e) {
                                    if (fullUpdateHandler != null) {
                                        fullUpdateHandler.handleError(e);
                                    }
                                    return;
                                }

                                if (fullUpdateHandler != null) {
                                    fullUpdateHandler.handleFullUpdate(context,jsonObject);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (e != null) {
                                    e.printStackTrace();
                                }

                                if (fullUpdateHandler != null) {
                                    fullUpdateHandler.handleError(e);
                                }
                            }
                        });
    }

    private void downloadAndPatch(final String newPatchPath, final PatchInfo patchInfo) {
        SPUtils.put(context, KEY_STAGE, STAGE_DOWNLOAD);
        String downloadUrl = patchInfo.getData().getDownloadUrl();
        PatchServer.get()
                .downloadPatch(downloadUrl, new PatchServer.PatchServerCallback() {
                    @Override
                    public void onSuccess(int code, byte[] bytes) {
                        String downloadPatchHash = patchInfo.getData().getHash();
                        if (!checkPatch(bytes, downloadPatchHash)) {
                            Log.e(TAG, "downloaded patch's hash is wrong: " + new String(bytes));
                            SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
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
                            SPUtils.put(context, KEY_STAGE, STAGE_PATCH);
                            actualManager.patch(context, newPatchPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
                            for (Listener listener : listeners) {
                                listener.onDownloadFailure(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
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
        return versionDirPath + "/" + getPatchName(data);
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
        onPatchSuccess(context, patchPath);
    }

    public void onPatchSuccess(Context context, String patchPath) {
        SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
        if (patchPath.endsWith("/" + JIAGU_PATCH_NAME)) {
            patchPath = patchPath.substring(0, patchPath.lastIndexOf("/")) + ".apk";
        }
        SPUtils.put(context, KEY_PATCHED_PATCH, patchPath);
        if (PatchUtils.isDebugPatch(patchPath)) {
            Intent intent = new Intent(DEBUG_ACTION_PATCH_RESULT);
            intent.putExtra(KEY_PACKAGE_NAME, appInfo.getPackageName());
            intent.putExtra(KEY_RESULT, true);
            context.sendBroadcast(intent);
        }
        for (Listener listener : listeners) {
            listener.onPatchSuccess();
        }
    }

    public void onPatchFailure(Context context, String patchPath,String error) {
        SPUtils.put(context, KEY_STAGE, STAGE_IDLE);
        if (patchPath.endsWith("/" + JIAGU_PATCH_NAME)) {
            patchPath = patchPath.substring(0, patchPath.lastIndexOf("/")) + ".apk";
        }
        if (PatchUtils.isDebugPatch(patchPath)) {
            Intent intent = new Intent(DEBUG_ACTION_PATCH_RESULT);
            intent.putExtra(KEY_PACKAGE_NAME, appInfo.getPackageName());
            intent.putExtra(KEY_RESULT, false);
            context.sendBroadcast(intent);
        } else {
            report(patchPath, false, error);
        }
        for (Listener listener : listeners) {
            listener.onPatchFailure(error);
        }
    }

    /**
     * 补丁合成失败
     *
     * @param patchPath
     */
    public void onPatchFailure(String patchPath,String error) {
        onPatchFailure(context, patchPath, error);
    }

    private Queue<Runnable> loadResultQueue = new LinkedList<>();

    /**
     * 补丁加载成功
     */
    public void onLoadSuccess() {
        if (context == null) {
            loadResultQueue.offer(new Runnable() {
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
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
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
        if (PatchUtils.isDebugPatch(patchPath)) {
            Intent intent = new Intent(DEBUG_ACTION_LOAD_RESULT);
            intent.putExtra(KEY_PACKAGE_NAME, appInfo.getPackageName());
            intent.putExtra(KEY_RESULT, true);
            context.sendBroadcast(intent);
        } else {
            //加载成功 返回码 0
            report(patchPath, true, PatchManager.ERROR_CODE_LOAD + 0);
        }
    }

    /**
     * 补丁加载失败
     */
    public void onLoadFailure(final String error) {
        if (context == null) {
            loadResultQueue.offer(new Runnable() {
                @Override
                public void run() {
                    onLoadFailureInternal(error);
                }
            });
        } else {
            onLoadFailureInternal(error);
        }
    }

    private void onLoadFailureInternal(String error) {
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        for (Listener listener : listeners) {
            try {
                listener.onLoadFailure(error);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String patchPath = SPUtils.get(context, KEY_PATCHED_PATCH, "");
        if (TextUtils.isEmpty(patchPath)) {
            return;
        }
        if (PatchUtils.isDebugPatch(patchPath)) {
            Intent intent = new Intent(DEBUG_ACTION_LOAD_RESULT);
            intent.putExtra(KEY_PACKAGE_NAME, appInfo.getPackageName());
            intent.putExtra(KEY_RESULT, false);
            context.sendBroadcast(intent);
        } else {
            report(patchPath, false, error);
        }
    }



    /**
     * 上报补丁结果
     * @param patchPath
     * @param result
     * @param error
     */
    private void report(String patchPath, final boolean result, String error) {
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
                        result, error, new PatchServer.PatchServerCallback() {
                            @Override
                            public void onSuccess(int code, byte[] bytes) {
                                SPUtils.put(context, patchName, result ? SUCCESS_REPORTED : FAILURE_REPORTED);
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
    }
}
