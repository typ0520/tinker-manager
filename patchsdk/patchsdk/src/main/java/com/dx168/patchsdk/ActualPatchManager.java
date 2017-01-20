package com.dx168.patchsdk;

import android.content.Context;

/**
 * Created by jianjun.lin on 2017/1/20.
 */

public interface ActualPatchManager {

    void cleanPatch(Context context);

    void patch(Context context, String patchPath);

}
