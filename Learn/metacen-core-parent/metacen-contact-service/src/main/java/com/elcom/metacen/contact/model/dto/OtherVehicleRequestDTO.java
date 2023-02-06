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
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OtherVehicleRequestDTO implements Serializable {

    private String name;
    private Double dimLength;
    private Double dimWidth;
    private Double dimHeight;
    private Integer countryId;
    private String description;
    private Double tonnage;
    private String payroll;
    private String sideId;
    private String equipment;
    private Double speedMax;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<String> keywordLst;
    private List<ObjectRelationshipDTO> relationshipLst;
}
