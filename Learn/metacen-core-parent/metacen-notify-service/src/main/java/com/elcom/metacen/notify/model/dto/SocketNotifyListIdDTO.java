/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model.dto;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class SocketNotifyListIdDTO implements Serializable {

    private String notifyId;
    private String userId;

    public SocketNotifyListIdDTO() {
    }

    public SocketNotifyListIdDTO(String notifyId, String userId) {
        this.notifyId = notifyId;
        this.userId = userId;
    }

    public String getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(String notifyId) {
        this.notifyId = notifyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
