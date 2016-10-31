package com.dx168.tmserver.core.dto;

/**
 * Created by tong on 16/10/27.
 */
public class PatchInfoDto {
    private String appUid;
    private String versionName;
    private int patchVersion;
    private int publishType;//0 灰度发布 1 正常发布
    private long patchSize;
    private String hash;
    private String description;
    private String createdTime;
    private String downloadUrl;

    public String getAppUid() {
        return appUid;
    }

    public void setAppUid(String appUid) {
        this.appUid = appUid;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(int patchVersion) {
        this.patchVersion = patchVersion;
    }

    public int getPublishType() {
        return publishType;
    }

    public void setPublishType(int publishType) {
        this.publishType = publishType;
    }

    public long getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(long patchSize) {
        this.patchSize = patchSize;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
