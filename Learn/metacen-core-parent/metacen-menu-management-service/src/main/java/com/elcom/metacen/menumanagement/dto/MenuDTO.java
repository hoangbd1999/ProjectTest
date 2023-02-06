/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.dto;

import com.elcom.metacen.menumanagement.model.Menu;
import java.util.Date;
import lombok.Data;

/**
 *
 * @author Admin
 */
@Data
public class MenuDTO {
        private Integer id;

    private String name;
    private String description;
    private String url;
    private Integer orderNo;
    private Integer parentId;
    private Date createdAt;
    private String resourceCode ;

    public MenuDTO(Menu menu) {
        this.id = menu.getId();
        this.name = menu.getName();
        this.description = menu.getDescription();
        this.url = menu.getUrl();
        this.orderNo = menu.getOrderNo();
        this.parentId = menu.getParentId();
        this.createdAt = menu.getCreatedAt();
    }
    
    
    
    
}
