/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.dto;

import com.elcom.metacen.menumanagement.model.RoleMenu;
import java.util.List;

/**
 *
 * @author Admin
 */
public class UpsertRoleMenuRequest {
    private String roleCode;
    private List<RoleMenu> menuList;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public List<RoleMenu> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<RoleMenu> menuList) {
        this.menuList = menuList;
    }

   
   

  

   
    
}
