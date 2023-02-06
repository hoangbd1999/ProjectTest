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
public class SocketNotifyDataDTO implements Serializable {

    private SocketNotifyMessageDTO message;
    private List<SocketNotifyListIdDTO> listId;

    public SocketNotifyDataDTO() {
    }

    public SocketNotifyMessageDTO getMessage() {
        return message;
    }

    public void setMessage(SocketNotifyMessageDTO message) {
        this.message = message;
    }

    public List<SocketNotifyListIdDTO> getListId() {
        return listId;
    }

    public void setListId(List<SocketNotifyListIdDTO> listId) {
        this.listId = listId;
    }

}
