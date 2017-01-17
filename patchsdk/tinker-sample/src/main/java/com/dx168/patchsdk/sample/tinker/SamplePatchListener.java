/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dx168.patchsdk.sample.tinker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.dx168.patchsdk.utils.FileUtils;
import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zhangshaowen on 16/4/30.
 * optional, you can just use DefaultPatchListener
 * we can check whatever you want whether we actually send a patch request
 * such as we can check rom space or apk channel
 */
public class SamplePatchListener extends DefaultPatchListener {
    private static final String TAG = "Tinker.SamplePatchListener";

    protected static final long NEW_PATCH_RESTRICTION_SPACE_SIZE_MIN = 60 * 1024 * 1024;
    protected static final long OLD_PATCH_RESTRICTION_SPACE_SIZE_MIN = 30 * 1024 * 1024;

    private final int maxMemory;

    public SamplePatchListener(Context context) {
        super(context);
        maxMemory = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        TinkerLog.i(TAG, "application maxMemory:" + maxMemory);
    }

    /**
     * because we use the defaultCheckPatchReceived method
     * the error code define by myself should after {@code ShareConstants.ERROR_RECOVER_INSERVICE
     *
     * @param path
     * @param newPatch
     * @return
     */
    @Override
    public int patchCheck(String path, boolean isUpgrade) {
        File patchFile = new File(path);
        TinkerLog.i(TAG, "receive a patch file: %s, isUpgrade:%b, file size:%d", path, isUpgrade, SharePatchFileUtil.getFileOrDirectorySize(patchFile));
        int returnCode = super.patchCheck(path, isUpgrade);

        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            if (isUpgrade) {
                returnCode = SampleUtils.checkForPatchRecover(NEW_PATCH_RESTRICTION_SPACE_SIZE_MIN, maxMemory);
            } else {
                returnCode = SampleUtils.checkForPatchRecover(OLD_PATCH_RESTRICTION_SPACE_SIZE_MIN, maxMemory);
            }
        }

        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            String patchMd5 = SharePatchFileUtil.getMD5(patchFile);
            SharedPreferences sp = context.getSharedPreferences(ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS);
            //optional, only disable this patch file with md5
            int fastCrashCount = sp.getInt(patchMd5, 0);
            if (fastCrashCount >= SampleUncaughtExceptionHandler.MAX_CRASH_COUNT) {
                returnCode = SampleUtils.ERROR_PATCH_CRASH_LIMIT;
            } else {
                //for upgrade patch, version must be not the same
                //for repair patch, we won't has the com.dx168.patchsdk.com.dx168.patchsdk.sample.tinker load flag
                Tinker tinker = Tinker.with(context);

                if (tinker.isTinkerLoaded()) {
                    TinkerLoadResult tinkerLoadResult = tinker.getTinkerLoadResultIfPresent();
                    if (tinkerLoadResult != null) {
                        String currentVersion = tinkerLoadResult.currentVersion;
                        if (patchMd5.equals(currentVersion)) {
                            returnCode = SampleUtils.ERROR_PATCH_ALREADY_APPLY;
                        }
                    }
                }
            }
        }
        // Warning, it is just a sample case, you don't need to copy all of these
        // Interception some of the request
        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            Properties properties = ShareTinkerInternals.fastGetPatchPackageMeta(patchFile);
            if (properties == null) {
                returnCode = SampleUtils.ERROR_PATCH_CONDITION_NOT_SATISFIED;
            } else {
                String platform = properties.getProperty(SampleUtils.PLATFORM);
                TinkerLog.i(TAG, "get platform:" + platform);
                // check patch platform require
                if (platform == null) {// || !platform.equals(BuildInfo.PLATFORM)
                    returnCode = SampleUtils.ERROR_PATCH_CONDITION_NOT_SATISFIED;
                }
            }
        }

        SampleTinkerReport.onTryApply(isUpgrade, returnCode == ShareConstants.ERROR_PATCH_OK);
        return returnCode;
    }

    @Override
    public int onPatchReceived(String path, boolean isUpgrade) {
        try {
            ZipFile zipFile = new ZipFile(path);
            boolean isFullPatch = zipFile.getEntry("FULL_PATCH") != null;
            if (isFullPatch) {
                Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
                File dexDir = new File(path.substring(0, path.length() - 4));
                if (!dexDir.exists()) {
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String fileName = zipEntry.getName();
                        if (fileName.startsWith("classes") && fileName.endsWith(".dex")) {
                            FileUtils.copyFile(zipFile.getInputStream(zipEntry), dexDir + "/" + fileName);
                        }
                    }
                }
                FileUtils.copyFile(zipFile.getInputStream(zipFile.getEntry("patch.apk")), path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int returnCode = patchCheck(path, isUpgrade);

        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            SamplePatchService.runPatchService(context, path, isUpgrade);
        } else {
            Tinker.with(context).getLoadReporter().onLoadPatchListenerReceiveFail(new File(path), returnCode, isUpgrade);
        }
        return returnCode;

    }

    private void releaseFullPatch() {

    }

}