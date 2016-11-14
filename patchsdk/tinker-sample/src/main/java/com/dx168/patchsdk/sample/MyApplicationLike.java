package com.dx168.patchsdk.sample;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.dx168.patchsdk.ActualPatchManager;
import com.dx168.patchsdk.PatchListener;
import com.dx168.patchsdk.PatchManager;
import com.dx168.patchsdk.sample.tinker.TinkerApplicationLike;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by jianjun.lin on 2016/10/31.
 */
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.dx168.patchsdk.sample.MyApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class MyApplicationLike extends TinkerApplicationLike {

    private OriginalApplication originalApplication;

    public MyApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent, Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent, resources, classLoader, assetManager);
        originalApplication = new OriginalApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PatchManager.getInstance().init(getApplication(), "20161107130703172-9340", "e9e8941f33914d21b2d24933651702ca", "http://192.168.2.1:9011/api/patch", new ActualPatchManager() {
            @Override
            public void cleanPatch(Context context) {
                TinkerInstaller.cleanPatch(context);
            }

            @Override
            public void applyPatch(Context context, String patchPath) {
                TinkerInstaller.onReceiveUpgradePatch(context, patchPath);
            }
        });
        PatchManager.getInstance().setTag("your tag");
        PatchManager.getInstance().queryAndApplyPatch(new PatchListener() {
            @Override
            public void onQuerySuccess(String response) {
                Log.d("TEST", "onQuerySuccess response=" + response);
            }

            @Override
            public void onQueryFailure(Throwable e) {
                Log.d("TEST", "onQueryFailure e=" + e.getMessage());
            }

            @Override
            public void onDownloadSuccess(String path) {
                Log.d("TEST", "onDownloadSuccess path=" + path);
            }

            @Override
            public void onDownloadFailure(Throwable e) {
                Log.d("TEST", "onDownloadFailure e=" + e.getMessage());
            }

            @Override
            public void onApplySuccess() {
                Log.d("TEST", "onApplySuccess");
            }

            @Override
            public void onApplyFailure(String msg) {
                Log.d("TEST", "onApplyFailure msg=" + msg);
            }

            @Override
            public void onCompleted() {
                Log.d("TEST", "onCompleted");
            }
        });
        originalApplication.onCreate();
    }

}
