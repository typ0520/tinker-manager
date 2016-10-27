package com.ytx.hotfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.ytx.hotfix.bean.AppInfo;
import com.ytx.hotfix.bean.PatchInfo;

import java.io.File;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.R.attr.versionName;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public class HotFixManager {

    public static HotFixManager getInstance() {
        return HotFixManagerHolder.INSTANCE;
    }

    private static final class HotFixManagerHolder {
        static final HotFixManager INSTANCE = new HotFixManager();
    }

    public static final String SP_NAME = "HotFix";
    public static final String SP_KEY_USING_PATCH = "using_patch";

    private Context context;
    private String patchDirPath;
    private AppInfo appInfo;

    public void init(Context context, String appId) {
        this.context = context;
        appInfo = new AppInfo();
        appInfo.setAppId(appId);

        appInfo.setOsVersion(Utils.getSystemVersion());
        appInfo.setX86CPU(Utils.isX86CPU());
        appInfo.setPhoneNumber(Utils.getPhoneNumber(context));

        //TODO
        appInfo.setTag("");
        appInfo.setAppSecret("");

        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            appInfo.setVersionName(pkgInfo.versionName);
            appInfo.setVersionCode(pkgInfo.versionCode);
            String appName = pkgInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            String hotFixDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + appName + File.separator + "HotFix";
            patchDirPath = hotFixDirPath + File.separator + versionName;
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

    public void queryAndApplyPatch() {
        queryAndApplyPatch(null);
    }

    public void queryAndApplyPatch(final @Nullable PatchListener patchListener) {
        if (context == null) {
            throw new NullPointerException("HotFix must be init before using");
        }
        HotFixService.get()
                .queryPatch(appInfo)
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
                        if (patchListener != null) {
                            patchListener.onQuerySuccess(patchInfo.toString());
                        }
                        if (patchInfo.getCode() != 200) {
                            Toast.makeText(context, patchInfo.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
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
                                    HotFixPatchReporter patchReporter = (HotFixPatchReporter) Tinker.with(context).getPatchReporter();
                                    patchReporter.setPatchListener(patchListener);
                                    TinkerInstaller.onReceiveUpgradePatch(context, patch.getAbsolutePath());
                                    return;
                                }
                            }
                        }
                        downloadAndApplyPatch(newPatchPath, patchInfo.getData().getUrl(), patchListener);
                    }

                });
    }

    private void downloadAndApplyPatch(final String newPatchPath, String url, final PatchListener patchListener) {
        HotFixService.get().downloadFile(url)
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
                        HotFixPatchReporter patchReporter = (HotFixPatchReporter) Tinker.with(context).getPatchReporter();
                        patchReporter.setPatchListener(patchListener);
                        TinkerInstaller.onReceiveUpgradePatch(context, newPatchPath);

                    }
                });
    }

    @NonNull
    private String getPatchPath(String patchVersion) {
        return patchDirPath + File.separator + patchVersion + ".apk";
    }

}
