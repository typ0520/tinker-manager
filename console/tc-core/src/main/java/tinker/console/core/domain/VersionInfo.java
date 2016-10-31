package tinker.console.core.domain;

import java.util.Date;

/**
 * Created by tong on 16/10/26.
 */
public class VersionInfo {
    private Integer id;
    private Integer userId;
    private String appUid;
    private String versionName;
    private Date createdAt;
    private Date updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

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

    @Override
    public String toString() {
        return "VersionInfo{" +
                "id=" + id +
                ", userId=" + userId +
                ", appUid='" + appUid + '\'' +
                ", versionName='" + versionName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
