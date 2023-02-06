/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

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
public class OrganisationDTO extends BaseDTO<OrganisationDTO> {

    private String uuid;
    private String name;
    private String organisationType;
    private String organisationTypeName;
    private Integer countryId;
    private String countryName;
    private String headquarters;
    private String sideId;
    private String sideName;
    private String description;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    // private List<CommentDTO> commentLst;
    private List<ObjectRelationshipDeltailDTO> relationshipLst;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;
}
