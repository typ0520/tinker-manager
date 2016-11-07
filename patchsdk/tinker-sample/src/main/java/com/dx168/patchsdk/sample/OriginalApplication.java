package com.dx168.patchsdk.sample;

import android.app.Application;
import android.support.multidex.MultiDex;

/**
 * Created by jianjun.lin on 2016/11/4.
 */

public class OriginalApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
    }


}
