/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.uploadservice.model.dto;

import com.google.gson.Gson;

/**
 *
 * @author Admin
 */
public class UploadDTO {
    private String path;
    private String folder;
    private String accept;
    private Long maxSize;//byte

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }
    public String toJsonString(){
        return new Gson().toJson(this);
    }
}
