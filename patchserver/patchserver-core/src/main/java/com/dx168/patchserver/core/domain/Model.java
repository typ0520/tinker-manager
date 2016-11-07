package com.dx168.patchserver.core.domain;

import java.util.Date;

/**
 * Created by tong on 16/10/26.
 */
public class Model {
    private Integer id;
    private Integer userId;
    private String regularExp;
    private String description;
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

    public String getRegularExp() {
        return regularExp;
    }

    public void setRegularExp(String regularExp) {
        this.regularExp = regularExp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
