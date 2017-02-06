package com.dx168.patchsdk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.dx168.patchsdk.utils.PatchUtils;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class RestartService extends Service {

    private static final String TAG = "patchsdk.RestartService";
    public static final int WHAT = 0;
    public static final int DELAY = 200;
    public static final int RETRY_TIMES = 10;

    private int count = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT:
                    Context context = RestartService.this;
                    if (!PatchUtils.isMainProcessRunning(context)) {
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                        Log.e(TAG, "start launchIntent");
                        stopSelf();
                        System.exit(0);
                        Process.killProcess(Process.myPid());
                        return;
                    }
                    if (count++ < RETRY_TIMES) {
                        sendMessageDelayed(Message.obtain(msg), DELAY);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            handler.sendMessage(handler.obtainMessage(WHAT));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, RestartService.class);
        context.startService(intent);
        System.exit(0);
        Process.killProcess(Process.myPid());
    }
}
