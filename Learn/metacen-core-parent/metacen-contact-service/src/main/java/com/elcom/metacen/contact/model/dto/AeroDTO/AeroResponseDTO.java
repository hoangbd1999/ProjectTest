package com.elcom.metacen.contact.model.dto.AeroDTO;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import com.elcom.metacen.contact.model.dto.FileDTO;
import com.elcom.metacen.contact.model.dto.KeywordDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDeltailDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AeroResponseDTO extends BaseDTO<AeroResponseDTO> {

    private String uuid;
    private String name;
    private String model;
    private int countryId;
    private String countryName;
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
    private String sideName;
    private String typeId;
    private String typeDesc;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<ObjectRelationshipDeltailDTO> relationshipLst;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;
}
