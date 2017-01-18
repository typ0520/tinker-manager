package com.dx168.patchsdk.sample;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.dx168.patchsdk.PatchListener;
import com.dx168.patchsdk.PatchManager;
import com.dx168.patchsdk.tinker.SampleApplicationLike;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by jianjun.lin on 2016/10/31.
 */
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.dx168.patchsdk.sample.MyApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class MyApplicationLike extends SampleApplicationLike {

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
        PatchManager.getInstance().init(getApplication(), "http://hotfix.dx168.com/hotfix-apis/", appId, appSecret);
        PatchManager.getInstance().setTag("your tag");
        PatchManager.getInstance().setChannel("");
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
