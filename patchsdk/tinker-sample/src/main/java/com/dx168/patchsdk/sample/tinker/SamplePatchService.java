package com.dx168.patchsdk.sample.tinker;

import android.content.Context;
import android.content.Intent;

import com.tencent.tinker.lib.service.TinkerPatchService;

/**
 * Created by jianjun.lin on 2017/1/13.
 */

public class SamplePatchService extends TinkerPatchService {

    private static final String        PATCH_PATH_EXTRA      = "patch_path_extra";
    private static final String        PATCH_NEW_EXTRA       = "patch_new_extra";

    public static void runPatchService(Context context, String path, boolean isUpgradePatch) {
        Intent intent = new Intent(context, SamplePatchService.class);
        intent.putExtra(PATCH_PATH_EXTRA, path);
        intent.putExtra(PATCH_NEW_EXTRA, isUpgradePatch);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onHandleIntent(intent);
    }
}
