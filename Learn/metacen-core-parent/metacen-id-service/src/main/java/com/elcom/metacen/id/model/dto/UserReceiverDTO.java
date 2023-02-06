/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model.dto;

import com.elcom.metacen.id.model.User;
import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class UserReceiverDTO implements Serializable {

    private String id;
    private String name;
    private String email;
    private String type = "USER";

    public UserReceiverDTO() {
    }

    public UserReceiverDTO(User user) {
        if (user != null) {
            this.id = user.getUuid();
            this.name = user.getFullName();
            this.email = user.getEmail();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
