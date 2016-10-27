package com.ytx.hotfix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tencent.tinker.loader.shareutil.SharePatchInfo;
import com.ytx.hotfix.tinker.SamplePatchReporter;

import java.io.File;


/**
 * Created by jianjun.lin on 2016/10/26.
 */
public class HotFixPatchReporter extends SamplePatchReporter {
    public HotFixPatchReporter(Context context) {
        super(context);
    }

    @Override
    public void onPatchResult(File patchFile, boolean success, long cost, boolean isUpgradePatch) {
        super.onPatchResult(patchFile, success, cost, isUpgradePatch);
        if (patchListener != null) {
            if (success) {
                SharedPreferences sp = context.getSharedPreferences(HotFixManager.SP_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(HotFixManager.SP_KEY_USING_PATCH, patchFile.getAbsolutePath());
                editor.apply();
                //TODO report to server
                patchListener.onApplySuccess();
            } else {
                patchListener.onApplyFailure("");
            }
        }
    }

    @Override
    public void onPatchDexOptFail(File patchFile, File dexFile, String optDirectory, String dexName, Throwable t, boolean isUpgradePatch) {
        super.onPatchDexOptFail(patchFile, dexFile, optDirectory, dexName, t, isUpgradePatch);
        if (patchListener != null) {
            patchListener.onApplyFailure("");
        }
    }

    @Override
    public void onPatchException(File patchFile, Throwable e, boolean isUpgradePatch) {
        super.onPatchException(patchFile, e, isUpgradePatch);
        if (patchListener != null) {
            patchListener.onApplyFailure("");
        }
    }

    @Override
    public void onPatchInfoCorrupted(File patchFile, String oldVersion, String newVersion, boolean isUpgradePatch) {
        super.onPatchInfoCorrupted(patchFile, oldVersion, newVersion, isUpgradePatch);
        if (patchListener != null) {
            patchListener.onApplyFailure("");
        }
    }

    @Override
    public void onPatchPackageCheckFail(File patchFile, boolean isUpgradePatch, int errorCode) {
        super.onPatchPackageCheckFail(patchFile, isUpgradePatch, errorCode);
        if (patchListener != null) {
            patchListener.onApplyFailure("");
        }
    }

    @Override
    public void onPatchServiceStart(Intent intent) {
        super.onPatchServiceStart(intent);
        if (patchListener != null) {
            patchListener.onApplyFailure("");
        }
    }

    @Override
    public void onPatchTypeExtractFail(File patchFile, File extractTo, String filename, int fileType, boolean isUpgradePatch) {
        super.onPatchTypeExtractFail(patchFile, extractTo, filename, fileType, isUpgradePatch);
        if (patchListener != null) {
            patchListener.onApplyFailure("");
        }
    }

    @Override
    public void onPatchVersionCheckFail(File patchFile, SharePatchInfo oldPatchInfo, String patchFileVersion, boolean isUpgradePatch) {
        super.onPatchVersionCheckFail(patchFile, oldPatchInfo, patchFileVersion, isUpgradePatch);
        if (patchListener != null) {
            patchListener.onApplyFailure("");
        }
    }

    private PatchListener patchListener;

    public void setPatchListener(PatchListener patchListener) {
        this.patchListener = patchListener;
    }

}
