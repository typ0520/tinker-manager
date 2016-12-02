package com.dx168.patchsdk;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.dx168.patchsdk.utils.DebugUtils;

/**
 * Created by jianjun.lin on 2016/12/1.
 */

/**
 * Tinker apply 的回调是处于后台进程，ApplyResultService 的主要目的是让回调回到主进程
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
        try {
            if (intent == null) {
                return super.onStartCommand(intent, flags, startId);
            }
            mHandler.removeCallbacksAndMessages(null);
            String msg = intent.getStringExtra("msg");
            if (!TextUtils.isEmpty(msg)) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                DebugUtils.sendNotify(this, msg);
            }
            PatchListener patchListener = PatchManager.getInstance().getPatchListener();
            if (patchListener != null) {
                boolean success = intent.getBooleanExtra("success", false);
                if (success) {
                    patchListener.onApplySuccess();
                    patchListener.onCompleted();
                } else {
                    patchListener.onApplyFailure(msg);
                }
            }
            return super.onStartCommand(intent, flags, startId);
        } finally {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 5000);
        }
    }

}
