package com.dx168.patchtool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class DebugReceiver extends BroadcastReceiver {

    public interface OnReceiveListener {
        void onReceive(Intent intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        for (OnReceiveListener listener : sListeners) {
            listener.onReceive(intent);
        }
    }

    private static List<OnReceiveListener> sListeners = new ArrayList<>();

    public static void register(OnReceiveListener onReceiveListener) {
        sListeners.add(onReceiveListener);
    }

    public static void unregister(OnReceiveListener onReceiveListener) {
        sListeners.remove(onReceiveListener);
    }
}
