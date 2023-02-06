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
public class MarineVesselResponseDTO extends BaseDTO<MarineVesselResponseDTO> {
    private String uuid;
    private Long mmsi;
    private String name;
    private String imo;
    private Long countryId;
    private String countryName;
    private String typeId;
    private String typeName;
    private Double dimA;
    private Double dimC;
    private String payroll;
    private String description;
    private String equipment;
    private Long draught;
    private Double grossTonnage;
    private Double speedMax;
    private String sideId;
    private String sideName;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<ObjectRelationshipDeltailDTO> relationshipLst;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;
}
