package com.dx168.patchserver.facade.common;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by tong on 8/11/15.
 */
public class RestResponse<T> implements Serializable {
    protected int                 code = 200;
    protected String              message;
    protected T data;
    protected Map<String,Object> extra;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
