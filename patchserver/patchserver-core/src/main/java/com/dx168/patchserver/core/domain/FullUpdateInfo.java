package com.dx168.patchserver.core.domain;

import java.util.Date;

/**
 * APP全量更新信息
 * Created by tong on 17/6/29.
 */
public class FullUpdateInfo {
    private Integer id;
    private String appUid;

    /**
     * 最新版本的versionName
     */
    private String latestVersion;
    /**
     * 更新提示的标题
     */
    private String title;
    /**
     * 更新说明
     */
    private String description;
    /**
     * 低于这个版本都强制更新
     */
    private String lowestSupportVersion;
    /**
     * 默认的下载地址(没有传渠道号)
     */
    private String defaultUrl;
    /**
     * 渠道下载地址
     */
    private String channelUrl;
    /**
     * 文件大小
     */
    private String fileSize;
    /**
     * 2G|3G|4G|WIFI
     */
    private String networkType;

    private int status;

    private Date createdAt;
    private Date updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAppUid() {
        return appUid;
    }

    public void setAppUid(String appUid) {
        this.appUid = appUid;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLowestSupportVersion() {
        return lowestSupportVersion;
    }

    public void setLowestSupportVersion(String lowestSupportVersion) {
        this.lowestSupportVersion = lowestSupportVersion;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
