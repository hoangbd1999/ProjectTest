/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InfrastructureResponseDTO extends BaseDTO<InfrastructureResponseDTO> {

    private String uuid;
    private String name;
    private String location;
    private Integer countryId;
    private String countryName;
    private Integer infrastructureType;
    private String infrastructureTypeName;
    private String area;
    private String description;
    private String sideId;
    private String sideName;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<ObjectRelationshipDeltailDTO> relationshipLst;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;
}
