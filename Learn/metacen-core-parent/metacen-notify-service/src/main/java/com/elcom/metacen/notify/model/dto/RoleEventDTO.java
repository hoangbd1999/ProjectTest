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
public class RoleEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private String eventCode;
    private String eventName;
    private Date createdAt;
    private Integer status;

    public RoleEventDTO(String roleCode, Map<String, Object> map) {
        if(map != null && map.size() > 0){
            if(map.containsKey("eventCode")) this.eventCode = (String) map.get("eventCode");
            if(map.containsKey("eventName")) this.eventName = (String) map.get("eventName");
            if(map.containsKey("status")) this.status = (Integer) map.get("status");
        }
    }
    
    public RoleEventDTO() {
    }

    public RoleEventDTO(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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
        if (!(object instanceof RoleEventDTO)) {
            return false;
        }
        RoleEventDTO other = (RoleEventDTO) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.notify.model.dto.RoleEvent[ id=" + id + " ]";
    }
    
}
