package com.dx168.tmsdk.sample;

/**
 * Created by jianjun.lin on 2016/11/4.
 */

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler ueh;


    public MyUncaughtExceptionHandler() {
        ueh = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (ueh != null) {
            ueh.uncaughtException(thread, ex);
        }
        //...
    }


}
