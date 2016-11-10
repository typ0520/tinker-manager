package com.dx168.patchsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.dx168.patchsdk.bean.AppInfo;
import com.dx168.patchsdk.bean.PatchInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.schedulers.Schedulers;

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

    private void free() {
        instance = null;
        PatchServer.free();
    }

    public static final String SP_NAME = "patchsdk";
    public static final String SP_KEY_USING_PATCH = "using_patch";

    private Context context;
    private ActualPatchManager apm;
    private String patchDirPath;
    private AppInfo appInfo;
    private String url;

    public void init(Context context, String appId, String appSecret, String url, ActualPatchManager apm) {
        this.context = context;
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        this.apm = apm;
        this.url = url;
        appInfo = new AppInfo();
        appInfo.setAppId(appId);
        appInfo.setAppSecret(appSecret);
        appInfo.setToken(PatchUtils.md5(appId + "_" + appSecret));
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
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setTag(String tag) {
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        if (appInfo == null) {
            throw new NullPointerException("PatchManager must be init before using");
        }
        appInfo.setTag(tag);
    }

    public void setChannel(String channel) {
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        if (appInfo == null) {
            throw new NullPointerException("PatchManager must be init before using");
        }
        appInfo.setChannel(channel);
    }

    private PatchListener patchListener;

    public void queryAndApplyPatch() {
        queryAndApplyPatch(null);
    }

    public void queryAndApplyPatch(final PatchListener patchListener) {
        if (context == null) {
            throw new NullPointerException("PatchManager must be init before using");
        }
        if (!PatchUtils.isMainProcess(context)) {
            return;
        }
        this.patchListener = patchListener;
        PatchServer.getInstance().get()
                .queryPatch(url, appInfo.getAppId(), appInfo.getToken(), appInfo.getTag(),
                        appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getPlatform(),
                        appInfo.getOsVersion(), appInfo.getModel(),appInfo.getChannel(), appInfo.getSdkVersion())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<PatchInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (patchListener != null) {
                            patchListener.onQueryFailure(e);
                        }
                    }

                    @Override
                    public void onNext(final PatchInfo patchInfo) {
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
                        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                        String usingPatchPath = sp.getString(SP_KEY_USING_PATCH, "");
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
                                if (TextUtils.equals(patch.getName(), patchInfo.getData().getPatchVersion() + ".apk")) {
                                    if (!checkPatch(patch, patchInfo.getData().getHash())) {
                                        Log.e(TAG, "cache patch's hash is wrong");
                                        if (patchListener != null) {
                                            patchListener.onDownloadFailure(new Exception("cache patch's hash is wrong"));
                                        }
                                        return;
                                    }
                                    apm.cleanPatch(context);
                                    apm.applyPatch(context, patch.getAbsolutePath());
                                    return;
                                }
                            }
                        }
                        downloadAndApplyPatch(newPatchPath, patchInfo.getData().getDownloadUrl(), patchInfo.getData().getHash());
                    }

                });

    }

    private void downloadAndApplyPatch(final String newPatchPath, String url, final String hash) {
        PatchServer.getInstance().get()
                .downloadFile(url)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (patchListener != null) {
                            patchListener.onDownloadFailure(e);
                        }
                    }

                    @Override
                    public void onNext(ResponseBody body) {
                        byte[] bytes = null;
                        try {
                            bytes = body.bytes();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!checkPatch(bytes, hash)) {
                            Log.e(TAG, "downloaded patch's hash is wrong");
                            if (patchListener != null) {
                                patchListener.onDownloadFailure(new Exception("downloaded patch's hash is wrong"));
                            }
                            return;
                        }
                        PatchUtils.writeToDisk(bytes, newPatchPath);
                        if (patchListener != null) {
                            patchListener.onDownloadSuccess(newPatchPath);
                        }
                        apm.applyPatch(context, newPatchPath);
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
        String downloadFileHash = PatchUtils.md5(appInfo.getAppId() + "_" + appInfo.getAppSecret() + "_" + PatchUtils.md5(bytes));
        return TextUtils.equals(downloadFileHash, hash);
    }

    private String getPatchPath(PatchInfo.Data data) {
        return patchDirPath + File.separator + data.getPatchVersion() + "_" + data.getHash() + ".apk";
    }

    public void onApplySuccess(String rawPatchFilePath) {
        SharedPreferences sp = context.getSharedPreferences(PatchManager.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PatchManager.SP_KEY_USING_PATCH, rawPatchFilePath);
        editor.apply();
        //TODO report to server
        if (patchListener != null) {
            patchListener.onApplySuccess();
            patchListener.onCompleted();
        }
    }

    public void onApplyFailure(String msg) {
        //TODO report to server
        if (patchListener != null) {
            patchListener.onApplyFailure(msg);
        }
    }

}
