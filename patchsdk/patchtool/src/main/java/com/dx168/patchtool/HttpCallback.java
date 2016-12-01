package com.dx168.patchtool;

/**
 * Created by jianjun.lin on 2016/11/30.
 */

public interface HttpCallback {
    void onSuccess(int code, byte[] bytes);

    void onFailure(Exception e);
}
