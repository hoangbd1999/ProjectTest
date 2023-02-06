package com.elcom.abac.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PolicyDto implements Serializable {
    private Map<String,Object> allow;
    private Map<String,Object> disallow;
    private Boolean admin;

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Map<String, Object> getAllow() {
        return allow;
    }

    public void setAllow(Map<String, Object> allow) {
        this.allow = allow;
    }

    public Map<String, Object> getDisallow() {
        return disallow;
    }

    public void setDisallow(Map<String, Object> disallow) {
        this.disallow = disallow;
    }
}
