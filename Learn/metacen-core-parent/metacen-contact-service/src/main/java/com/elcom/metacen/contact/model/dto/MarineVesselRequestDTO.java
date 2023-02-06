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
public class MarineVesselRequestDTO implements Serializable {
    private Long mmsi;
    private String name;
    private String imo;
    private Long countryId;
    private String typeId;
    private Double dimA;
    private Double dimC;
    private String payroll;
    private String description;
    private String equipment;
    private Long draught;
    private Double grossTonnage;
    private Double speedMax;
    private String sideId;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<String> keywordLst;
    private List<ObjectRelationshipDTO> relationshipLst;
}
