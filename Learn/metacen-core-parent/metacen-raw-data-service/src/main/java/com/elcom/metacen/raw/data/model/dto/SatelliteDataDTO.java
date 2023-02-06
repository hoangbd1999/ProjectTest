package com.elcom.metacen.raw.data.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteDataDTO {

    private String uuidKey;
    private String satelliteName;
    private String imageFilePath;
    private String imageFilePathLocal;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String coordinates;
    private Timestamp eventTime;
    private Timestamp ingestTime;
    private Integer processStatus;
    private String dataVendor;

}
