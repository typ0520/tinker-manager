package com.ytx.hotfix.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jianjun.lin on 2016/10/27.
 */
public class AppInfo {

    private String versionName;
    private int versionCode;

    @SerializedName("appUid")
    private String appId;
    private String appSecret;
    private String tag;

    private String osVersion;
    private boolean isX86CPU;
    private String phoneNumber;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public boolean isX86CPU() {
        return isX86CPU;
    }

    public void setX86CPU(boolean x86CPU) {
        isX86CPU = x86CPU;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
