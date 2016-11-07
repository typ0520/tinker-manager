package com.dx168.patchsdk;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public interface PatchListener {

    void onQuerySuccess(String response);

    void onQueryFailure(Throwable e);

    void onDownloadSuccess(String path);

    void onDownloadFailure(Throwable e);

    void onApplySuccess();

    void onApplyFailure(String msg);

    void onCompleted();
}
