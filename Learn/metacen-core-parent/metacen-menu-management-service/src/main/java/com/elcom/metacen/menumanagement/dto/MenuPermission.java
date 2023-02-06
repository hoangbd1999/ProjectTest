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
public class MenuPermission {

    private int menuId;

    private String menuName;

    private int orderNo;

    private int status;

    private int canRead;

    private int canCreate;

    private int canUpdate;

    private int canDelete;

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

  

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCanRead() {
        return canRead;
    }

    public void setCanRead(int canRead) {
        this.canRead = canRead;
    }

    public int getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(int canCreate) {
        this.canCreate = canCreate;
    }

    public int getCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(int canUpdate) {
        this.canUpdate = canUpdate;
    }

    public int getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(int canDelete) {
        this.canDelete = canDelete;
    }

    
    
}
