/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model.dto;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Admin
 */
public class RBACResponseDTO implements Serializable {
    private String requestMethod;
    private String uuid;
    private String apiPath;
    private Integer isAdmin;
    private Boolean permission;
    private List<RoleStateDTO> roleStateSet;
    private List<RoleEventDTO> roleEventSet;
    private List<RoleMenuDTO> roleMenuSet;

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public Integer getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Integer isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }

    public List<RoleStateDTO> getRoleStateSet() {
        return roleStateSet;
    }

    public void setRoleStateSet(List<RoleStateDTO> roleStateSet) {
        this.roleStateSet = roleStateSet;
    }

    public List<RoleEventDTO> getRoleEventSet() {
        return roleEventSet;
    }

    public void setRoleEventSet(List<RoleEventDTO> roleEventSet) {
        this.roleEventSet = roleEventSet;
    }

    public List<RoleMenuDTO> getRoleMenuSet() {
        return roleMenuSet;
    }

    public void setRoleMenuSet(List<RoleMenuDTO> roleMenuSet) {
        this.roleMenuSet = roleMenuSet;
    }
}
