/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.dbm;

import com.elcom.metacen.menumanagement.enums.DataStatus;
import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String catType;
    private String code;
    private String description;
    private String mapper;
    private String name;
    private Integer orderInType;
    private DataStatus status = DataStatus.ENABLE;

    public Category() {
    }

    public Category(Long id) {
        this.id = id;
    }

    public Category(Long id, String catType, String code, String name, Integer orderInType) {
        this.id = id;
        this.catType = catType;
        this.code = code;
        this.name = name;
        this.orderInType = orderInType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCatType() {
        return catType;
    }

    public void setCatType(String catType) {
        this.catType = catType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMapper() {
        return mapper;
    }

    public void setMapper(String mapper) {
        this.mapper = mapper;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderInType() {
        return orderInType;
    }

    public void setOrderInType(Integer orderInType) {
        this.orderInType = orderInType;
    }

    public DataStatus getStatus() {
        return status;
    }

    public void setStatus(DataStatus status) {
        this.status = status;
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
        if (!(object instanceof CategoryDTO)) {
            return false;
        }
        Category other = (Category) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.dto.CategoryDTO[ id=" + id + " ]";
    }
}
