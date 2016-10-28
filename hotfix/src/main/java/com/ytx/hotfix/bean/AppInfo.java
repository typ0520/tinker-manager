package com.ytx.hotfix.bean;

import android.os.Build;

import com.google.gson.annotations.SerializedName;
import com.ytx.hotfix.BuildConfig;

/**
 * Created by jianjun.lin on 2016/10/27.
 */
public class AppInfo {

    @SerializedName("appUid")
    private String appId;
    private String token;
    private String tag;
    private String versionName;
    private int versionCode;
    private String platform = "Android";
    private final String osVersion = Build.VERSION.RELEASE;
    private final String model = Build.MODEL;
    private String sdkVersion = BuildConfig.SDK_VERSION;

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getPlatform() {
        return platform;
    }

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getModel() {
        return model;
    }

}
