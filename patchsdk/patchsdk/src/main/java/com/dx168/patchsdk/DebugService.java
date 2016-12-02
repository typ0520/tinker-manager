package com.dx168.patchsdk;

import android.app.Service;
import android.content.Intent;
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
                    final Messenger replyTo = msg.replyTo;
                    PatchManager.getInstance().queryAndApplyPatch(new SimplePatchListener() {
                        @Override
                        public void onApplySuccess() {
                            Message reply = new Message();
                            reply.what = 1;
                            reply.arg1 = 1;
                            try {
                                replyTo.send(reply);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onApplyFailure(String msg) {
                            Message reply = new Message();
                            reply.what = 1;
                            reply.arg1 = 0;
                            reply.obj = msg;
                            try {
                                replyTo.send(reply);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                break;
                case 2: {

                }
                break;
            }
        }
    };

    private Messenger mMessenger = new Messenger(mHandler);
}
