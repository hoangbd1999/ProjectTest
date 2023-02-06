/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.dto;

import java.util.List;

/**
 *
 * @author Admin
 */
public class ViolationStatusDTO {
    private int status;
    private String message;
    private List<ViolationStatus> data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ViolationStatus> getData() {
        return data;
    }

    public void setData(List<ViolationStatus> data) {
        this.data = data;
    }
}
