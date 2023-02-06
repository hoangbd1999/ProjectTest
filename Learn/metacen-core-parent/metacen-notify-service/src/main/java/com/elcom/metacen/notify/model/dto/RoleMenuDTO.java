/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class RoleMenuDTO implements Serializable {

    private Integer id;

    private String menuName;

    private Integer orderNo;

    private Date createdAt;

    private Integer status;

    private Integer canRead;

    private Integer canCreate;

    private Integer canUpdate;

    private Integer canDelete;

    private String roleCode;

    private Menu menuId;

    public RoleMenuDTO(String roleCode, Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            if (map.containsKey("menuId")) {
                this.menuId = new Menu((Integer) map.get("menuId"));
            }
            if (map.containsKey("menuName")) {
                this.menuName = (String) map.get("menuName");
            }
            if (map.containsKey("orderNo")) {
                this.orderNo = (Integer) map.get("orderNo");
            }
            if (map.containsKey("status")) {
                this.status = (Integer) map.get("status");
            }
        }
    }

    public RoleMenuDTO() {
    }

    public RoleMenuDTO(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Menu getMenuId() {
        return menuId;
    }

    public void setMenuId(Menu menuId) {
        this.menuId = menuId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RoleMenuDTO)) {
            return false;
        }
        RoleMenuDTO other = (RoleMenuDTO) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.notify.model.dto.RoleMenuDTO[ id=" + id + " ]";
    }

}
