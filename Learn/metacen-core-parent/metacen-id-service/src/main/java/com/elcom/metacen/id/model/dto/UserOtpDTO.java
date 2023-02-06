/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model.dto;

import com.elcom.metacen.id.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Admin
 */
public class UserOtpDTO implements Serializable {

    private String uuid;
    private String userName;
    private String email;
    private String mobile;
    private String fullName;
    @JsonIgnore
    private String password;
    private Integer status;
    private Integer emailVerify;
    private Integer mobileVerify;
    private String skype;
    private String facebook;
    private String avatar;
    private String address;
    private String birthDay;
    private Integer gender;
    private Date createdAt;
    private Date lastUpdate;
    private String loginIp;
    private Date lastLogin;
    private int signupType;
    private String fbId;
    private String ggId;
    private String appleId;
    private Integer isDelete;
    private Integer setPassword;
    private Timestamp profileUpdate;
    private Timestamp avatarUpdate;
    @JsonIgnore
    private String otp;
    private Timestamp otpTime;
    @JsonIgnore
    private String otpPassword;
    private Timestamp otpPasswordTime;

    public UserOtpDTO() {

    }

    public UserOtpDTO(User user) {
        this.uuid = user.getUuid();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.mobile = user.getMobile();
        this.fullName = user.getFullName();
        this.password = user.getPassword();
        this.status = user.getStatus();
        this.emailVerify = user.getEmailVerify();
        this.mobileVerify = user.getMobileVerify();
        this.skype = user.getSkype();
        this.facebook = user.getFacebook();
        this.avatar = user.getAvatar();
        this.address = user.getAddress();
        this.birthDay = user.getBirthDay();
        this.gender = user.getGender();
        this.createdAt = user.getCreatedAt();
        this.lastUpdate = user.getLastUpdate();
        this.loginIp = user.getLoginIp();
        this.lastLogin = user.getLastLogin();
        this.signupType = user.getSignupType();
        this.fbId = user.getFbId();
        this.ggId = user.getGgId();
        this.appleId = user.getAppleId();
        this.isDelete = user.getIsDelete();
        this.setPassword = user.getSetPassword();
        this.profileUpdate = user.getProfileUpdate();
        this.avatarUpdate = user.getAvatarUpdate();
        this.otp = user.getOtp();
        this.otpTime = user.getOtpTime();
        this.otpPassword = user.getOtpPassword();
        this.otpPasswordTime = user.getOtpPasswordTime();
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getEmailVerify() {
        return emailVerify;
    }

    public void setEmailVerify(Integer emailVerify) {
        this.emailVerify = emailVerify;
    }

    public Integer getMobileVerify() {
        return mobileVerify;
    }

    public void setMobileVerify(Integer mobileVerify) {
        this.mobileVerify = mobileVerify;
    }

    public String getSkype() {
        return skype;
    }

    public void setSkype(String skype) {
        this.skype = skype;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getSignupType() {
        return signupType;
    }

    public void setSignupType(int signupType) {
        this.signupType = signupType;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getGgId() {
        return ggId;
    }

    public void setGgId(String ggId) {
        this.ggId = ggId;
    }

    public String getAppleId() {
        return appleId;
    }

    public void setAppleId(String appleId) {
        this.appleId = appleId;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getSetPassword() {
        return setPassword;
    }

    public void setSetPassword(Integer setPassword) {
        this.setPassword = setPassword;
    }

    public Timestamp getProfileUpdate() {
        return profileUpdate;
    }

    public void setProfileUpdate(Timestamp profileUpdate) {
        this.profileUpdate = profileUpdate;
    }

    public Timestamp getAvatarUpdate() {
        return avatarUpdate;
    }

    public void setAvatarUpdate(Timestamp avatarUpdate) {
        this.avatarUpdate = avatarUpdate;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Timestamp getOtpTime() {
        return otpTime;
    }

    public void setOtpTime(Timestamp otpTime) {
        this.otpTime = otpTime;
    }

    public String getOtpPassword() {
        return otpPassword;
    }

    public void setOtpPassword(String otpPassword) {
        this.otpPassword = otpPassword;
    }

    public Timestamp getOtpPasswordTime() {
        return otpPasswordTime;
    }

    public void setOtpPasswordTime(Timestamp otpPasswordTime) {
        this.otpPasswordTime = otpPasswordTime;
    }

}
