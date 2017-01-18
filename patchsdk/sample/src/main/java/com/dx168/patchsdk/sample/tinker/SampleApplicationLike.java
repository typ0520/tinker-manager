package com.dx168.patchsdk.sample.tinker;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.DefaultApplicationLike;

/**
 * Created by jianjun.lin on 2016/10/25.
 */
public class SampleApplicationLike extends DefaultApplicationLike {

    public static Application application;

    private static final String TAG = SampleApplicationLike.class.getSimpleName();

    public SampleApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    /**
     * install multiDex before install com.dx168.patchsdk.sample.tinker
     * so we don't need to put the com.dx168.patchsdk.sample.tinker lib classes in the main dex
     *
     * @param base
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        //you must install multiDex whatever com.dx168.patchsdk.sample.tinker is installed!
        MultiDex.install(base);

        SampleTinkerManager.setTinkerApplicationLike(this);
        SampleTinkerManager.initFastCrashProtect();
        //should set before com.dx168.patchsdk.sample.tinker is installed
        SampleTinkerManager.setUpgradeRetryEnable(true);

        //optional set logIml, or you can use default debug log
        TinkerInstaller.setLogIml(new SampleTinkerLog());

        //installTinker after load multiDex
        //or you can put com.tencent.com.dx168.patchsdk.sample.tinker.** to main dex
        SampleTinkerManager.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }

}
