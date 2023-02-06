package com.elcom.metacen.contact.model.dto.AeroDTO;

import com.elcom.metacen.contact.model.dto.FileDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class AeroInsertRequestDTO {

    private String name;
    private String model;
    private Integer countryId;
    private Double dimLength;
    private Double dimWidth;
    private Double dimHeight;
    private Double speedMax;
    private Double grossTonnage;
    private String payrollTime;
    private String equipment;
    private String permanentBase;
    private String description;
    private String sideId;
    private String typeId;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<String> keywordLst;
    private List<ObjectRelationshipDTO> relationshipLst;
}
