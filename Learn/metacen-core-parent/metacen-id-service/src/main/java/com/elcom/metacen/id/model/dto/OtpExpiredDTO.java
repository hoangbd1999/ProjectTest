/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model.dto;

/**
 *
 * @author Admin
 */
public class OtpExpiredDTO {
    private String uuid;
    private Long otpExpiredTime;
    private Integer countSms;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getOtpExpiredTime() {
        return otpExpiredTime;
    }

    public void setOtpExpiredTime(Long otpExpiredTime) {
        this.otpExpiredTime = otpExpiredTime;
    }

    public Integer getCountSms() {
        return countSms;
    }

    public void setCountSms(Integer countSms) {
        this.countSms = countSms;
    }
    
}
