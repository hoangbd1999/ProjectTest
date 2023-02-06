/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model.dto;

import com.elcom.metacen.id.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class UserAvatarDTO {

    private String uuid;
    private String avatar;

    public UserAvatarDTO() {
    }

    public UserAvatarDTO(String uuid, String avatar) {
        this.uuid = uuid;
        this.avatar = avatar;
    }

    public UserAvatarDTO(User user) {
        if (user != null) {
            this.uuid = user.getUuid();
            this.avatar = user.getAvatar();
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public static List<UserAvatarDTO> getAvatarList(List<User> userList) {
        if (userList != null && !userList.isEmpty()) {
            List<UserAvatarDTO> avatarList = new ArrayList<>();
            UserAvatarDTO tmpAvatar = null;
            for (User tmpUser : userList) {
                tmpAvatar = new UserAvatarDTO(tmpUser);
                avatarList.add(tmpAvatar);
            }
            return avatarList;
        }
        return null;
    }
}
