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
public class OrganisationRequestDTO implements Serializable {

    private String name;
    private String organisationType;
    private Integer countryId;
    private String headquarters;
    private String sideId;
    private String description;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<String> commentLst;
    private List<ObjectRelationshipDTO> relationshipLst;
    private List<String> keywordLst;
}
