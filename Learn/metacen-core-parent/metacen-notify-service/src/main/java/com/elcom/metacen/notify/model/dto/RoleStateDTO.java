/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class RoleStateDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private String stateType;
    private String stateCode;
    private String stateName;
    private Date createdAt;
    private Integer status;

    public RoleStateDTO(String roleCode, Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            if (map.containsKey("stateType")) {
                this.stateType = (String) map.get("stateType");
            }
            if (map.containsKey("stateCode")) {
                this.stateCode = (String) map.get("stateCode");
            }
            if (map.containsKey("stateName")) {
                this.stateName = (String) map.get("stateName");
            }
            if (map.containsKey("status")) {
                this.status = (Integer) map.get("status");
            }
        }
    }

    public RoleStateDTO() {
    }

    public RoleStateDTO(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStateType() {
        return stateType;
    }

    public void setStateType(String stateType) {
        this.stateType = stateType;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RoleStateDTO)) {
            return false;
        }
        RoleStateDTO other = (RoleStateDTO) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.notify.model.dto.RoleStateDTO[ id=" + id + " ]";
    }

}
