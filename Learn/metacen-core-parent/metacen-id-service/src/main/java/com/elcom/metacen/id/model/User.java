package com.elcom.metacen.id.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "user", schema = "metacen_id")
@Proxy(lazy = false)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.cache.annotation.Cacheable
@ApiModel(value = "User entity")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @NotNull
    @Size(min = 1, max = 40)
    @Column(name = "uuid")
    private String uuid;

    @Size(max = 100)
    @Column(name = "user_name")
    private String userName;

    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 20)
    @Column(name = "mobile")
    private String mobile;

    @Size(max = 255)
    @Column(name = "full_name")
    private String fullName;
    
    @Size(max = 255)
    @Column(name = "password")
    @JsonIgnore
    private String password;

    @Column(name = "status")
    private Integer status;

    @Column(name = "email_verify")
    private Integer emailVerify;

    @Column(name = "mobile_verify")
    private Integer mobileVerify;

    @Size(max = 255)
    @Column(name = "skype")
    private String skype;

    @Size(max = 255)
    @Column(name = "facebook")
    private String facebook;

    @Size(max = 1000)
    @Column(name = "avatar")
    private String avatar;

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Size(max = 255)
    @Column(name = "birth_day")
    private String birthDay;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "last_update")
    private Timestamp lastUpdate;

    @Size(max = 255)
    @Column(name = "login_ip")
    private String loginIp;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    @Column(name = "signup_type")
    private int signupType;

    @Size(max = 255)
    @Column(name = "fb_id")
    private String fbId;

    @Size(max = 255)
    @Column(name = "gg_id")
    private String ggId;
    
    @Size(max = 255)
    @Column(name = "apple_id")
    private String appleId;

    @Column(name = "is_delete")
    private Integer isDelete;

    @Column(name = "set_password")
    private Integer setPassword;
    
    @Column(name = "profile_update")
    private Timestamp profileUpdate;
    
    @Column(name = "avatar_update")
    private Timestamp avatarUpdate;
    
    @JsonIgnore
    @Column(name = "otp")
    private String otp;
    
    @JsonIgnore
    @Column(name = "otp_time")
    private Timestamp otpTime;
    
    @JsonIgnore
    @Column(name = "otp_mobile")
    private String otpMobile;
    
    @JsonIgnore
    @Column(name = "otp_password")
    private String otpPassword;
    
    @JsonIgnore
    @Column(name = "opt_password_time")
    private Timestamp otpPasswordTime;
    
    @Column(name = "police_rank")
    private String policeRank;
    
    @Column(name = "position")
    private String position;

    @Column(name = "is_active")
    private Integer isActive;

    @ManyToOne
    @JoinColumn(name = "unit", nullable = true)
    @JsonManagedReference
    private Unit unit;

    @JsonIgnore
    public static final Integer STATUS_ACTIVE = 1;
    @JsonIgnore
    public static final Integer STATUS_LOCK = -1;
    
    @Transient
    @JsonIgnore
    private String matchingPassword;


    public User() {
    }
    
    @PrePersist
    void preInsert() {
       if (this.getCreatedAt()== null)
           this.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    }

    public User(String uuid) {
        this.uuid = uuid;
    }

    public User(String uuid, int signupType) {
        this.uuid = uuid;
        this.signupType = signupType;
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

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
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

    public String getOtpMobile() {
        return otpMobile;
    }

    public void setOtpMobile(String otpMobile) {
        this.otpMobile = otpMobile;
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

    public String getPoliceRank() {
        return policeRank;
    }

    public void setPoliceRank(String policeRank) {
        this.policeRank = policeRank;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uuid != null ? uuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        return !((this.uuid == null && other.uuid != null) || (this.uuid != null && !this.uuid.equals(other.uuid)));
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.id.model.User[ uuid=" + uuid + " ]";
    }

    /**
     * @return the appleId
     */
    public String getAppleId() {
        return appleId;
    }

    /**
     * @param appleId the appleId to set
     */
    public void setAppleId(String appleId) {
        this.appleId = appleId;
    }

    public String toJsonString(){
        return new Gson().toJson(this);
    }
}
