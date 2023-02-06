package com.elcom.metacen.vsat.collector.model.dto.AeroDTO;

import com.elcom.metacen.vsat.collector.model.dto.BaseDTO;
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
    private List<String> imageLst;
    private List<String> fileAttachmentLst;
    private List<String> keywordUuidLst;
}
