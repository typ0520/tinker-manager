package com.dx168.patchsdk.sample.tinker;

import android.content.Context;
import android.os.Build;

import com.dx168.patchsdk.sample.internal.SampleBsDiffPatchInternal;
import com.dx168.patchsdk.sample.internal.SampleDexDiffPatchInternal;
import com.dx168.patchsdk.sample.internal.SampleResDiffPatchInternal;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.SharePatchInfo;
import com.tencent.tinker.loader.shareutil.ShareSecurityCheck;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by jianjun.lin on 2017/1/13.
 */

public class SampleUpgradePatch extends UpgradePatch {
    private static final String TAG = "Tinker.UpgradePatch";

    @Override
    public boolean tryPatch(Context context, String tempPatchPath, PatchResult patchResult) {
        Tinker manager = Tinker.with(context);

        final File patchFile = new File(tempPatchPath);

        if (!manager.isTinkerEnabled() || !ShareTinkerInternals.isTinkerEnableWithSharedPreferences(context)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:patch is disabled, just return");
            return false;
        }

        if (!SharePatchFileUtil.isLegalFile(patchFile)) {
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

            if (!SharePatchFileUtil.checkIfMd5Valid(patchMd5)) {
                TinkerLog.e(TAG, "UpgradePatch tryPatch:onPatchVersionCheckFail md5 %s is valid", patchMd5);
                manager.getPatchReporter().onPatchVersionCheckFail(patchFile, oldInfo, patchMd5);
                return false;
            }
            // if it is interpret now, use changing flag to wait main process
            final String finalOatDir = oldInfo.oatDir.equals(ShareConstants.INTERPRET_DEX_OPTIMIZE_PATH)
                    ? ShareConstants.CHANING_DEX_OPTIMIZE_PATH : oldInfo.oatDir;
            newInfo = new SharePatchInfo(oldInfo.oldVersion, patchMd5, Build.FINGERPRINT,finalOatDir);
        } else {
            newInfo = new SharePatchInfo("", patchMd5, Build.FINGERPRINT, ShareConstants.DEFAULT_DEX_OPTIMIZE_PATH);
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
        File[] dexFiles = new File(patchFile.getParentFile() + "/dex/").listFiles();
        try {
            if (dexFiles != null) {
                for (File dexFile : dexFiles) {
                    File dest = new File(patchVersionDirectory + "/" + ShareConstants.DEX_PATH + "/" + dexFile.getName());
                    SharePatchFileUtil.copyFileUsingStream(dexFile, dest);
                    extractDexToJar(dest);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File destPatchFile = new File(patchVersionDirectory + "/" + SharePatchFileUtil.getPatchVersionFile(patchMd5));
        try {
            // check md5 first
            if (!patchMd5.equals(SharePatchFileUtil.getMD5(destPatchFile))) {
                SharePatchFileUtil.copyFileUsingStream(patchFile, destPatchFile);
                TinkerLog.w(TAG, "UpgradePatch copy patch file, src file: %s size: %d, dest file: %s size:%d", patchFile.getAbsolutePath(), patchFile.length(),
                        destPatchFile.getAbsolutePath(), destPatchFile.length());
            }
        } catch (IOException e) {
//            e.printStackTrace();
            TinkerLog.e(TAG, "UpgradePatch tryPatch:copy patch file fail from %s to %s", patchFile.getPath(), destPatchFile.getPath());
            manager.getPatchReporter().onPatchTypeExtractFail(patchFile, destPatchFile, patchFile.getName(), ShareConstants.TYPE_PATCH_FILE);
            return false;
        }

        //we use destPatchFile instead of patchFile, because patchFile may be deleted during the patch process
        if (!SampleDexDiffPatchInternal.tryRecoverDexFiles(manager, signatureCheck, context, patchVersionDirectory, destPatchFile)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, try patch dex failed");
            return false;
        }

        if (!SampleBsDiffPatchInternal.tryRecoverLibraryFiles(manager, signatureCheck, context, patchVersionDirectory, destPatchFile)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, try patch library failed");
            return false;
        }

        if (!SampleResDiffPatchInternal.tryRecoverResourceFiles(manager, signatureCheck, context, patchVersionDirectory, destPatchFile)) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, try patch resource failed");
            return false;
        }

        // check dex opt file at last, some phone such as VIVO/OPPO like to change dex2oat to interpreted
        // just warn
        if (!SampleDexDiffPatchInternal.waitDexOptFile()) {
            TinkerLog.e(TAG, "UpgradePatch tryPatch:new patch recover, check dex opt file failed");
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

    private static void extractDexToJar(File dex) throws IOException {
        FileOutputStream fos = new FileOutputStream(dex + ".jar");
        InputStream in = new FileInputStream(dex);

        ZipOutputStream zos = null;
        BufferedInputStream bis = null;

        try {
            zos = new ZipOutputStream(new
                    BufferedOutputStream(fos));
            bis = new BufferedInputStream(in);

            byte[] buffer = new byte[ShareConstants.BUFFER_SIZE];
            ZipEntry entry = new ZipEntry(ShareConstants.DEX_IN_JAR);
            zos.putNextEntry(entry);
            int length = bis.read(buffer);
            while (length != -1) {
                zos.write(buffer, 0, length);
                length = bis.read(buffer);
            }
            zos.closeEntry();
        } finally {
            SharePatchFileUtil.closeQuietly(bis);
            SharePatchFileUtil.closeQuietly(zos);
        }
    }

}
