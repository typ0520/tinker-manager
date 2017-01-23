package com.dx168.patchsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class DebugReceiver extends BroadcastReceiver {

    private static final String ACTION_PATCH = "com.dx168.patchsdk.DebugReceiver.PATCH";
    private static final String ACTION_RESTART = "com.dx168.patchsdk.DebugReceiver.RESTART";
    private static final String KEY_PACKAGE_NAME = "package_name";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle data = intent.getExtras();
        if (data == null) {
            return;
        }
        final String packageName = data.getString(KEY_PACKAGE_NAME);
        if (!TextUtils.equals(packageName, context.getPackageName())) {
            return;
        }
        String action = intent.getAction();
        if (ACTION_PATCH.equals(action)) {
            PatchManager.getInstance().queryAndPatch();
        } else if (ACTION_RESTART.equals(action)) {
            RestartService.start(context);
        }
    }

}
