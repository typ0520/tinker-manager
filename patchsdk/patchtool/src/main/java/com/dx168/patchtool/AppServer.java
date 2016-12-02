package com.dx168.patchtool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class AppServer {

    private Context mContext;
    private String mPackageName;
    private Messenger mRemoteMessenger;
    private Callback mApplyPatchCallback;
    private ServiceConnection mConnection;

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
        if (mConnection != null) {
            mContext.unbindService(mConnection);
        }
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRemoteMessenger = new Messenger(service);
                realApplyPatch();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRemoteMessenger = null;
            }
        };
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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

    public void restart() {
        if (mRemoteMessenger != null) {
            realRestart();
            return;
        }
        Intent intent = new Intent("com.dx168.patchsdk.DebugService.BIND");
        intent.setPackage(mPackageName);
        if (mConnection != null) {
            mContext.unbindService(mConnection);
        }
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRemoteMessenger = new Messenger(service);
                realRestart();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRemoteMessenger = null;
            }
        };
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void realRestart() {
        Message msg = Message.obtain();
        msg.what = 2;
        msg.replyTo = mMessenger;
        try {
            mRemoteMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
        }
        mRemoteMessenger = null;
    }

    interface Callback {
        void onSuccess();

        void onFailure();
    }

}
