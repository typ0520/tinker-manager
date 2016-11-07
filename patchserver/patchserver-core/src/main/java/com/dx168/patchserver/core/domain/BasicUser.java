package com.dx168.patchserver.core.domain;

import java.util.Date;

/**
 * Created by tong on 16/10/25.
 */
public class BasicUser {
    private Integer id;
    private String username;
    private String mobile;
    private String email;
    private String password;
    private String avatar;
    private Date createdAt;
    private Date updatedAt;

    private Integer parentId;
    private BasicUser parentUser;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public BasicUser getParentUser() {
        return parentUser;
    }

    public void setParentUser(BasicUser parentUser) {
        this.parentUser = parentUser;
    }

    //是否是子账号
    public boolean isChildAccount() {
        return parentId != null;
    }
}
