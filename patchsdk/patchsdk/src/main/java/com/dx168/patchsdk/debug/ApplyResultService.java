package com.dx168.patchsdk.debug;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.dx168.patchsdk.utils.DebugUtils;

/**
 * Created by jianjun.lin on 2016/12/1.
 */

public class ApplyResultService extends Service {

    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.removeCallbacksAndMessages(null);
        String msg = intent.getStringExtra("msg");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        DebugUtils.sendNotify(this, msg);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, 5000);
        return super.onStartCommand(intent, flags, startId);
    }

}
