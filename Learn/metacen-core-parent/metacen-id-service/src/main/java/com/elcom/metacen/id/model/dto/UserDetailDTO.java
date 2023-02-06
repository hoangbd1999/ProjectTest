/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model.dto;

import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.User;

import java.util.Date;

/**
 *
 * @author Admin
 */
public class UserDetailDTO {

    private String uuid;
    private String userName;
    private String email;
    private String mobile;
    private String fullName;
    private String avatar;
    private Integer status;
    private String address;
    private String loginIp;
    private Integer signupType;
    private Date createdAt;
    private Date lastLogin;
    private String birthDay;
    private Integer gender;
    private Unit unit;

    public UserDetailDTO() {
    }

    public UserDetailDTO(User user) {
        this.uuid = user.getUuid();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.mobile = user.getMobile();
        this.fullName = user.getFullName();
        this.avatar = user.getAvatar();
        this.status = user.getStatus();
        this.address = user.getAddress();
        this.signupType = user.getSignupType();
        this.createdAt = user.getCreatedAt();
        this.lastLogin = user.getLastLogin();
        this.birthDay = user.getBirthDay();
        this.gender = user.getGender();
        this.unit = user.getUnit();
    }

    public UserDetailDTO(AuthorizationResponseDTO user) {
        this.uuid = user.getUuid();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.mobile = user.getMobile();
        this.fullName = user.getFullName();
        this.avatar = user.getAvatar();
        this.status = user.getStatus();
        this.address = user.getAddress();
        this.signupType = user.getSignupType();
        this.createdAt = user.getCreatedAt();
        this.lastLogin = user.getLastLogin();
        this.birthDay = user.getBirthDay();
        this.gender = user.getGender();
        this.unit = user.getUnit();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Integer getSignupType() {
        return signupType;
    }

    public void setSignupType(Integer signupType) {
        this.signupType = signupType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}