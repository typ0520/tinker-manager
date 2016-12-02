package com.dx168.patchtool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class AppServer implements ServiceConnection {

    private Context mContext;
    private String mPackageName;
    private Messenger mRemoteMessenger;
    private Callback mApplyPatchCallback;

    public AppServer(Context context, String packageName) {
        mContext = context;
        mPackageName = packageName;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    if (mApplyPatchCallback == null) {
                        return;
                    }
                    if (msg.arg1 == 1) {
                        mApplyPatchCallback.onSuccess();
                    } else {
                        mApplyPatchCallback.onFailure();
                    }
                }
                break;
            }
        }
    };
    private Messenger mMessenger = new Messenger(mHandler);

    public void applyPatch(Callback callback) {
        mApplyPatchCallback = callback;
        if (mRemoteMessenger != null) {
            realApplyPatch();
            return;
        }
        Intent intent = new Intent("com.dx168.patchsdk.DebugService.BIND");
        intent.setPackage(mPackageName);
        mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    private void realApplyPatch() {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.replyTo = mMessenger;
        try {
            mRemoteMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mRemoteMessenger = new Messenger(service);
        realApplyPatch();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mRemoteMessenger = null;
    }

    public void destroy() {
        mContext.unbindService(this);
        mRemoteMessenger = null;
    }

    interface Callback {
        void onSuccess();

        void onFailure();
    }

}
