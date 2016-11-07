package com.dx168.patchserver.manager.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 8/11/15.
 */
public class RestResponse implements Serializable {
    protected int                 code = 200;
    protected String              message;
    protected Map<String, Object> data = new HashMap();

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

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
