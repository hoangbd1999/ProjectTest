/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
public class ObjectGeneralInfoDTO implements Serializable {

    private String objectType;
    private String id;
    private String uuid;
    private String name;
    private String countryId;
    private String sideId;
    private String sideName;
    private int isDeleted;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;
}
