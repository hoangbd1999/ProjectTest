package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteImageObjectAnalyzedDTO {

    private String uuidKey;
    private String satelliteImageUuidKey;
    private Double width;
    private Double height;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String imageFilePath;
    private String analyzedEngine;
    private String color;
    private Timestamp ingestTime;
    private int isDeleted;

}
