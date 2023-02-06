/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KeywordGrantRequestDTO implements Serializable {

    private String name;
    private Integer type; //1: Đối tượng; 2: VSAT Media; 3: Ảnh vệ tinh
    private String refId;
    private String refType;

}
