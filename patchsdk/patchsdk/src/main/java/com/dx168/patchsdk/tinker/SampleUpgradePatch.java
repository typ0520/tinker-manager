package com.dx168.patchsdk.tinker;

import android.content.Context;
import android.os.Build;

import com.dx168.patchsdk.tinker.internal.BsDiffPatchInternal;
import com.dx168.patchsdk.tinker.internal.DexDiffPatchInternal;
import com.dx168.patchsdk.tinker.internal.ResDiffPatchInternal;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.SharePatchInfo;
import com.tencent.tinker.loader.shareutil.ShareSecurityCheck;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;
import java.io.IOException;

/**
 * Created by jianjun.lin on 2017/1/13.
 */

public class SampleUpgradePatch extends UpgradePatch {
    private static final String TAG = "Tinker.SampleUpgradePatch";

    @Override
    public boolean tryPatch(Context context, String tempPatchPath, PatchResult patchResult) {
        Tinker manager = Tinker.with(context);

        final File patchFile = new File(tempPatchPath);

        if (!manager.isTinkerEnabled() || !ShareTinkerInternals.isTinkerEnableWithSharedPreferences(context)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:patch is disabled, just return");
            return false;
        }

        if (!patchFile.isFile() || !patchFile.exists()) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:patch file is not found, just return");
            return false;
        }
        //check the signature, we should create a new checker
        ShareSecurityCheck signatureCheck = new ShareSecurityCheck(context);

        int returnCode = ShareTinkerInternals.checkTinkerPackage(context, manager.getTinkerFlags(), patchFile, signatureCheck);
        if (returnCode != ShareConstants.ERROR_PACKAGE_CHECK_OK) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:onPatchPackageCheckFail");
            manager.getPatchReporter().onPatchPackageCheckFail(patchFile, returnCode);
            return false;
        }

        //it is a new patch, so we should not find a exist
        SharePatchInfo oldInfo = manager.getTinkerLoadResultIfPresent().patchInfo;
        String patchMd5 = SharePatchFileUtil.getMD5(patchFile);

        if (patchMd5 == null) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:patch md5 is null, just return");
            return false;
        }

        //use md5 as version
        patchResult.patchVersion = patchMd5;

        SharePatchInfo newInfo;

        //already have patch
        if (oldInfo != null) {
            if (oldInfo.oldVersion == null || oldInfo.newVersion == null) {
                TinkerLog.e(TAG, "UpgradePatch tryPatch:onPatchInfoCorrupted");
                manager.getPatchReporter().onPatchInfoCorrupted(patchFile, oldInfo.oldVersion, oldInfo.newVersion);
                return false;
            }

            if (oldInfo.oldVersion.equals(patchMd5) || oldInfo.newVersion.equals(patchMd5)) {
                TinkerLog.e(TAG, "UpgradePatch tryPatch:onPatchVersionCheckFail");
                manager.getPatchReporter().onPatchVersionCheckFail(patchFile, oldInfo, patchMd5);
                return false;
            }
            newInfo = new SharePatchInfo(oldInfo.oldVersion, patchMd5, Build.FINGERPRINT);
        } else {
            newInfo = new SharePatchInfo("", patchMd5, Build.FINGERPRINT);
        }

        //check ok, we can real recover a new patch
        final String patchDirectory = manager.getPatchDirectory().getAbsolutePath();

        TinkerLog.i(TAG, "UpgradePatch tryPatch:patchMd5:%s", patchMd5);

        final String patchName = SharePatchFileUtil.getPatchVersionDirectory(patchMd5);

        final String patchVersionDirectory = patchDirectory + "/" + patchName;

        TinkerLog.i(TAG, "UpgradePatch tryPatch:patchVersionDirectory:%s", patchVersionDirectory);

        //it is a new patch, we first delete if there is any files
        //don't delete dir for faster retry
//        SharePatchFileUtil.deleteDir(patchVersionDirectory);

        //copy file
        File destPatchFile = new File(patchVersionDirectory + "/" + SharePatchFileUtil.getPatchVersionFile(patchMd5));
        File[] dexFiles = new File(patchFile.getParentFile() + "/dex/").listFiles();
        try {
            if (dexFiles != null) {
                for (File dexFile : dexFiles) {
                    File dest = new File(patchVersionDirectory + "/" + ShareConstants.DEX_PATH + "/" + dexFile.getName());
                    SharePatchFileUtil.copyFileUsingStream(dexFile, dest);
                }
            }
            SharePatchFileUtil.copyFileUsingStream(patchFile, destPatchFile);
            TinkerLog.w(TAG, "UpgradePatch after %s size:%d, %s size:%d", patchFile.getAbsolutePath(), patchFile.length(),
                    destPatchFile.getAbsolutePath(), destPatchFile.length());
        } catch (IOException e) {
//            e.printStackTrace();
            TinkerLog.e(TAG, "UpgradePatch tryPatch:copy patch file fail from %s to %s", patchFile.getPath(), destPatchFile.getPath());
            manager.getPatchReporter().onPatchTypeExtractFail(patchFile, destPatchFile, patchFile.getName(), ShareConstants.TYPE_PATCH_FILE);
            return false;
        }

        //we use destPatchFile instead of patchFile, because patchFile may be deleted during the patch process
        if (!DexDiffPatchInternal.tryRecoverDexFiles(manager, signatureCheck, context, patchVersionDirectory, destPatchFile)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, try patch dex failed");
            return false;
        }

        if (!BsDiffPatchInternal.tryRecoverLibraryFiles(manager, signatureCheck, context, patchVersionDirectory, destPatchFile)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, try patch library failed");
            return false;
        }

        if (!ResDiffPatchInternal.tryRecoverResourceFiles(manager, signatureCheck, context, patchVersionDirectory, destPatchFile)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, try patch resource failed");
            return false;
        }

        final File patchInfoFile = manager.getPatchInfoFile();

        if (!SharePatchInfo.rewritePatchInfoFileWithLock(patchInfoFile, newInfo, SharePatchFileUtil.getPatchInfoLockFile(patchDirectory))) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, rewrite patch info failed");
            manager.getPatchReporter().onPatchInfoCorrupted(patchFile, newInfo.oldVersion, newInfo.newVersion);
            return false;
        }


        TinkerLog.w(TAG, "UpgradePatch tryPatch: done, it is ok");
        return true;
    }
}
