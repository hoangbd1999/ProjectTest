/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

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
public class KeywordDataRequestDTO implements Serializable {

    private Integer type; //1: Đối tượng; 2: VSAT Media; 3: Ảnh vệ tinh
    private String refId;
    private String refType;
    private List<String> keywordIds;

}
