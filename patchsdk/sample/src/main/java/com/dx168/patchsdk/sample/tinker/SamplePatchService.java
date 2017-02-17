package com.dx168.patchsdk.sample.tinker;

import android.content.Context;
import android.content.Intent;

import com.tencent.tinker.lib.service.TinkerPatchService;
import com.tencent.tinker.lib.util.TinkerLog;

/**
 * Created by jianjun.lin on 2017/1/13.
 */

public class SamplePatchService extends TinkerPatchService {

    private static final String TAG = "Tinker.TinkerPatchService";

    private static final String PATCH_PATH_EXTRA = "patch_path_extra";
    private static final String RESULT_CLASS_EXTRA = "patch_result_class";

    public static void runPatchService(Context context, String path) {
        try {
            Intent intent = new Intent(context, SamplePatchService.class);
            intent.putExtra(PATCH_PATH_EXTRA, path);
            intent.putExtra(RESULT_CLASS_EXTRA, SampleResultService.class.getName());
            context.startService(intent);
        } catch (Throwable throwable) {
            TinkerLog.e(TAG, "start patch service fail, exception:" + throwable);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //TODO just for debug
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onHandleIntent(intent);
    }

}
