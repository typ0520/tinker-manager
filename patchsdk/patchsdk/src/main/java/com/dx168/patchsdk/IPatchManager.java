package com.dx168.patchsdk;

import android.content.Context;

/**
 * Created by jianjun.lin on 2017/2/13.
 */

public interface IPatchManager {

    void patch(Context context, String path);

    void cleanPatch(Context context);
}
