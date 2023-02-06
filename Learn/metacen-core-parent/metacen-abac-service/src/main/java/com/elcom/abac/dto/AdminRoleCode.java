package com.elcom.abac.dto;

import java.io.Serializable;
import java.util.Map;

public class AdminRoleCode implements Serializable {
    Map<String,String> roleCodeAdmin;

    public Map<String, String> getRoleCodeAdmin() {
        return roleCodeAdmin;
    }

    public void setRoleCodeAdmin(Map<String, String> roleCodeAdmin) {
        this.roleCodeAdmin = roleCodeAdmin;
    }
}
