/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.dto;

/**
 *
 * @author Admin
 */
import java.util.Map;

/**
 *
 * @author thainguyen
 */
public class ABACResponseDTO {

    private Boolean status;
    private Boolean admin;
    private String type; // ALLOW DENY
    private String description;

    public ABACResponseDTO(Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            if (map.containsKey("status")) {
                this.status = (Boolean) map.get("status");
            }
            if (map.containsKey("type")) {
                this.type = (String) map.get("type");
            }
            if (map.containsKey("description")) {
                this.description = (String) map.get("description");
            }
        }
    }

    public ABACResponseDTO() {
    }

    public ABACResponseDTO(Boolean status) {
        this.status = status;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "ABACResponseDTO{" + "status=" + status + ", type=" + type + ", description=" + description + '}';
    }

}
