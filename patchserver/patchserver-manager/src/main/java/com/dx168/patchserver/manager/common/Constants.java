package com.dx168.patchserver.manager.common;

/**
 * Created by tong on 16/10/25.
 */
public interface Constants {
    String SESSION_LOGIN_USER = "loginUser";
    String COOKIE_LOGINNAME = "loginname";
    String COOKIE_LOGINPWD = "loginpwd";

    /**
     * cookie有效期
     */
    int COOKIE_EXPIRY_DATE = 30 * 24 * 60 * 60;
}
