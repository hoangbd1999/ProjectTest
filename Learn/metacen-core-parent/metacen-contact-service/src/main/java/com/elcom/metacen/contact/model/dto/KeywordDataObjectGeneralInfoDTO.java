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
public class KeywordDataObjectGeneralInfoDTO implements Serializable {

    private String id;
    private String uuid;
    private String name;
    private String objectType;
    private Long mmsi;
    private List<String> keywordIds;
    private List<KeywordDTO> keywordLst;
}
