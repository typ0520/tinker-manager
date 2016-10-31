package com.ytx.hotfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.ytx.hotfix.bean.AppInfo;
import com.ytx.hotfix.bean.PatchInfo;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.ytx.hotfix.Utils.md5;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public final class HotFixManager {

    private static final String TAG = "HotFixManager";

    private static HotFixManager instance;

    public static HotFixManager getInstance() {
        if (instance == null) {
            instance = new HotFixManager();
        }
        return instance;
    }

    static void free() {
        instance = null;
    }

    public static final String SP_NAME = "HotFix";
    public static final String SP_KEY_USING_PATCH = "using_patch";

    private Context context;
    private String patchDirPath;
    private AppInfo appInfo;

    public void init(Context context, String appId, String appSecret) {
        this.context = context;
        if (!Tinker.with(context).isMainProcess()) {
            return;
        }
        appInfo = new AppInfo();
        appInfo.setAppId(appId);
        appInfo.setAppSecret(appSecret);
        appInfo.setToken(md5(appId + "_" + appSecret));
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            appInfo.setVersionName(pkgInfo.versionName);
            appInfo.setVersionCode(pkgInfo.versionCode);
            String appName = pkgInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            String hotFixDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + appName + File.separator + "HotFix";
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
        if (appInfo == null) {
            throw new NullPointerException("HotFix must be init before using");
        }
        appInfo.setTag(tag);
    }

    private PatchListener patchListener;

    PatchListener getPatchListener() {
        return patchListener;
    }

    public void queryAndApplyPatch() {
        queryAndApplyPatch(null);
    }

    public void queryAndApplyPatch(final @Nullable PatchListener patchListener) {
        if (context == null) {
            throw new NullPointerException("HotFix must be init before using");
        }
        if (!Tinker.with(context).isMainProcess()) {
            return;
        }
        this.patchListener = patchListener;
        HotFixService.get()
                .queryPatch(appInfo.getAppId(), appInfo.getToken(), appInfo.getTag(),
                        appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getPlatform(),
                        appInfo.getOsVersion(), appInfo.getModel(), appInfo.getSdkVersion())
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
                            TinkerInstaller.cleanPatch(context);
                            return;
                        }
                        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                        String usingPatchPath = sp.getString(SP_KEY_USING_PATCH, "");
                        String newPatchPath = getPatchPath(patchInfo.getData().getPatchVersion());
                        if (TextUtils.equals(usingPatchPath, newPatchPath)) {
                            return;
                        }
                        File patchDir = new File(patchDirPath);
                        if (patchDir.exists()) {
                            for (File patch : patchDir.listFiles()) {
                                if (TextUtils.equals(patch.getName(), patchInfo.getData().getPatchVersion() + ".apk")) {
                                    TinkerInstaller.cleanPatch(context);
                                    TinkerInstaller.onReceiveUpgradePatch(context, patch.getAbsolutePath());
                                    return;
                                }
                            }
                        }
                        downloadAndApplyPatch(newPatchPath, patchInfo.getData().getDownloadUrl(), patchInfo.getData().getHash());
                    }

                });

    }

    private void downloadAndApplyPatch(final String newPatchPath, String url, final String hash) {
        HotFixService.get().downloadFile(url)
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
                            TinkerLog.e(TAG, "wrong hash");
                            return;
                        }
                        Utils.writeToDisk(bytes, newPatchPath);
                        //TODO report to server
                        if (patchListener != null) {
                            patchListener.onDownloadSuccess(newPatchPath);
                        }
                        TinkerInstaller.onReceiveUpgradePatch(context, newPatchPath);
                    }
                });
    }

    private boolean checkPatch(byte[] bytes, String hash) {
        String downloadFileHash = Utils.md5(appInfo.getAppId() + "_" + appInfo.getAppSecret() + "_" + Utils.md5(bytes));
        return TextUtils.equals(downloadFileHash, hash);
    }

    @NonNull
    private String getPatchPath(String patchVersion) {
        return patchDirPath + File.separator + patchVersion + ".apk";
    }

}
