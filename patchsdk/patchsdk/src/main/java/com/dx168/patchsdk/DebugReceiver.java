package com.dx168.patchsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;

/**
 * Created by jianjun.lin on 2016/12/2.
 */

public class DebugReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle data = intent.getExtras();
        if (data == null) {
            return;
        }
        final String packageName = data.getString("PACKAGE_NAME");
        if (!TextUtils.equals(packageName, context.getPackageName())) {
            return;
        }
        int what = data.getInt("WHAT");
        if (what == 1) {
            PatchManager.getInstance().queryAndApplyPatch(new SimplePatchListener() {
                @Override
                public void onApplySuccess() {
                    Intent intent = new Intent("com.dx168.patchtool.APPLY_PATCH_RESULT");
                    intent.putExtra("PACKAGE_NAME", packageName);
                    intent.putExtra("IS_APPLY_SUCCESS", true);
                    context.sendBroadcast(intent);
                }

                @Override
                public void onApplyFailure(String msg) {
                    Intent intent = new Intent("com.dx168.patchtool.APPLY_PATCH_RESULT");
                    intent.putExtra("PACKAGE_NAME", packageName);
                    intent.putExtra("IS_APPLY_SUCCESS", false);
                    context.sendBroadcast(intent);
                }
            });
        } else if (what == 2) {
            RestartService.start(context);
        }
    }
}
