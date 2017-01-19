package com.dx168.patchsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class DebugReceiver extends BroadcastReceiver {

    private static final String DEBUG_ACTION = "com.dx168.patchtool.APPLY_PATCH_RESULT";
    private static final String KEY_WHAT = "WHAT";
    private static final String KEY_PACKAGE_NAME = "PACKAGE_NAME";
    private static final String KEY_IS_PATCH_SUCCESS = "IS_APPLY_SUCCESS";

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
        int what = data.getInt(KEY_WHAT);
        if (what == 1) {
            PatchManager.getInstance().register(new SimpleListener() {
                @Override
                public void onPatchSuccess() {
                    Intent intent = new Intent(DEBUG_ACTION);
                    intent.putExtra(KEY_PACKAGE_NAME, packageName);
                    intent.putExtra(KEY_IS_PATCH_SUCCESS, true);
                    context.sendBroadcast(intent);
                }

                @Override
                public void onPatchFailure() {
                    Intent intent = new Intent(DEBUG_ACTION);
                    intent.putExtra(KEY_PACKAGE_NAME, packageName);
                    intent.putExtra(KEY_IS_PATCH_SUCCESS, false);
                    context.sendBroadcast(intent);
                }
            });
            PatchManager.getInstance().queryAndPatch();
        } else if (what == 2) {
            RestartService.start(context);
        }
    }
}
