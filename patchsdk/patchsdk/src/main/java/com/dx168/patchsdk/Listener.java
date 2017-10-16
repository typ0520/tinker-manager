package com.dx168.patchsdk;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public interface Listener {

    void onQuerySuccess(String response);

    void onQueryFailure(Throwable e);

    void onDownloadSuccess(String path);

    void onDownloadFailure(Throwable e);

    void onPatchSuccess();

    void onPatchFailure(String error);

    void onLoadSuccess();

    void onLoadFailure(String error);

}
