package com.dx168.patchsdk.sample;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dx168.patchsdk.ActualPatchManager;
import com.dx168.patchsdk.Listener;
import com.dx168.patchsdk.PatchManager;
import com.dx168.patchsdk.sample.tinker.SampleApplicationLike;
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
public class MyApplicationLike extends SampleApplicationLike {

    private static final String TAG = "patchsdk";

    private OriginalApplication originalApplication;

    public MyApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
        originalApplication = new OriginalApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String appId = "20170112162040035-6936";
        String appSecret = "d978d00c0c1344959afa9d0a39d7dab3";
        PatchManager.getInstance().init(getApplication(), "http://xxx.xxx.xxx/hotfix-apis/", appId, appSecret, new ActualPatchManager() {
            @Override
            public void cleanPatch(Context context) {
                TinkerInstaller.cleanPatch(context);
            }

            @Override
            public void patch(Context context, String patchPath) {
                TinkerInstaller.onReceiveUpgradePatch(context, patchPath);
            }
        });
        PatchManager.getInstance().register(new Listener() {
            @Override
            public void onQuerySuccess(String response) {
                Log.d(TAG, "onQuerySuccess response=" + response);
            }

            @Override
            public void onQueryFailure(Throwable e) {
                Log.d(TAG, "onQueryFailure e=" + e);
            }

            @Override
            public void onDownloadSuccess(String path) {
                Log.d(TAG, "onDownloadSuccess path=" + path);
            }

            @Override
            public void onDownloadFailure(Throwable e) {
                Log.d(TAG, "onDownloadFailure e=" + e);
            }

            @Override
            public void onPatchSuccess() {
                Log.d(TAG, "onPatchSuccess");
            }

            @Override
            public void onPatchFailure() {
                Log.d(TAG, "onPatchFailure");
            }

            @Override
            public void onLoadSuccess() {
                Log.d(TAG, "onLoadSuccess");
            }

            @Override
            public void onLoadFailure() {
                Log.d(TAG, "onLoadFailure");
            }
        });
        PatchManager.getInstance().setTag("your tag");
        PatchManager.getInstance().setChannel("");
        PatchManager.getInstance().queryAndPatch();
        originalApplication.onCreate();
    }

}
