package com.dx168.patchsdk.debug;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class DebugService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    Message newMsg = Message.obtain();
                    newMsg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("TEST", "from DebugService");
                    newMsg.setData(bundle);
                    try {
                        msg.replyTo.send(newMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
                case 2: {
                    Message newMsg = Message.obtain();
                    newMsg.what = 2;
                    try {
                        msg.replyTo.send(newMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    };

    private Messenger mMessenger = new Messenger(mHandler);
}
