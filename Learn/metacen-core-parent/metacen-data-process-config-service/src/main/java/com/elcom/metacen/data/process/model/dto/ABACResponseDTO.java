/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;

/**
 *
 * @author Admin
 */
@NoArgsConstructor
@Getter
@Setter
public class ABACResponseDTO {

    private Boolean status;
    private Boolean admin;
    private Boolean systemConfig;
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

    public ABACResponseDTO(Boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ABACResponseDTO{" + "status=" + status + ", type=" + type + ", description=" + description + '}';
    }
}
