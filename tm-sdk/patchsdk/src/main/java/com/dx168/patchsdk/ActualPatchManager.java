package com.dx168.patchsdk;

import android.content.Context;

/**
 * Created by jianjun.lin on 2016/11/7.
 */

public interface ActualPatchManager {

    void cleanPatch(Context context);

    void applyPatch(Context context, String patchPath);

}
