/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model.dto;

import com.elcom.metacen.id.model.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class UserPagingDTO implements Serializable{

    private List<User> dataRows;
    private Long totalRows;

    public UserPagingDTO() {
        totalRows = new Long("0");
        dataRows = new ArrayList<>();
    }

    public List<User> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<User> dataRows) {
        this.dataRows = dataRows;
    }

    public Long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Long totalRows) {
        this.totalRows = totalRows;
    }
    
}
