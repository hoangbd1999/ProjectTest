package com.elcom.abac.dto;

import java.io.Serializable;
import java.util.List;

public class RoleCodeUuidRedis implements Serializable {
    private String uuid;
    private List<String> roleCode;

    public List<String> getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(List<String> roleCode) {
        this.roleCode = roleCode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
