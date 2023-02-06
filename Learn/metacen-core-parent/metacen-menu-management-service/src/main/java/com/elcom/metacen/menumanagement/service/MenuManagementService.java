/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.service;

import com.elcom.metacen.menumanagement.dto.MenuDTO;
import com.elcom.metacen.menumanagement.model.Menu;
import com.elcom.metacen.menumanagement.model.RelationResources;
import com.elcom.metacen.menumanagement.model.RoleMenu;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface MenuManagementService {

    List<MenuDTO> findAllMenu();

    Menu findMenuById(int menuId);

    public List<RoleMenu> findByRoleCode(String roleCode);
    
    public void deleteAllRoleMenu(List<RoleMenu> listRoleMenu);
    
    public void saveAllRoleMenu(List<RoleMenu> listRoleMenu);
    
    public List<RelationResources> findAllRelationResourceses();
}
