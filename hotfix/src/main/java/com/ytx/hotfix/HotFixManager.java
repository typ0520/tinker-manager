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
import com.ytx.hotfix.bean.AppInfo;
import com.ytx.hotfix.bean.PatchInfo;

import java.io.File;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public final class HotFixManager {

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
        //TODO
        appInfo.setTag("");
        appInfo.setToken(appSecret);
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

    private PatchListener patchListener;

    public PatchListener getPatchListener() {
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
                        downloadAndApplyPatch(newPatchPath, patchInfo.getData().getDownloadUrl(), patchListener);
                    }

                });


    }

    private void downloadAndApplyPatch(final String newPatchPath, String url, final PatchListener patchListener) {
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
                        Utils.writeToDisk(body, newPatchPath);
                        if (patchListener != null) {
                            patchListener.onDownloadSuccess(newPatchPath);
                        }
                        //TODO report to server
                        TinkerInstaller.onReceiveUpgradePatch(context, newPatchPath);
                    }
                });
    }

    @NonNull
    private String getPatchPath(String patchVersion) {
        return patchDirPath + File.separator + patchVersion + ".apk";
    }

}
