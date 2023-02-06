/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
public class OtherVehicleResponseDTO extends BaseDTO<OtherVehicleResponseDTO> {

    private String uuid;
    private String name;
    private Double dimLength;
    private Double dimWidth;
    private Double dimHeight;
    private Integer countryId;
    private String countryName;
    private String description;
    private Double tonnage;
    private String payroll;
    private String sideId;
    private String sideName;
    private String equipment;
    private Double speedMax;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<ObjectRelationshipDeltailDTO> relationshipLst;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;

}
