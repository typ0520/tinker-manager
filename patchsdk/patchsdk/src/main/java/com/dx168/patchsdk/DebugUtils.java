package com.dx168.patchsdk;

import android.os.Environment;

import com.dx168.patchsdk.bean.AppInfo;

import java.io.File;

/**
 * Created by jianjun.lin on 2016/12/1.
 */

public class DebugUtils {

    private static final String DEBUG_PATCH_DIR_NAME = "com.dx168.patchtool";

    public static File findDebugPatch(AppInfo appInfo) {
         File patchDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator );

//                + File.separator + appInfo.getPackageName() + "_" + appInfo.getVersionName() + "_" + patchVersion + ".apk";
        return null;
    }

}
