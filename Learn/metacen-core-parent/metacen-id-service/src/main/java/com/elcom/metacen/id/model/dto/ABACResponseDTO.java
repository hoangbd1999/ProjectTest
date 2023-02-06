package com.elcom.metacen.id.model.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ABACResponseDTO implements Serializable {
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
    @Override
    public String toString() {
        return "ABACResponseDTO{" + "status=" + status + ", type=" + type + ", description=" + description + '}';
    }
}