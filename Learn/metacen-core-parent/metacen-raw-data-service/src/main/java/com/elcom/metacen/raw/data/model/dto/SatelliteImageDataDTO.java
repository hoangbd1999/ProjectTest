package com.elcom.metacen.raw.data.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *
 * @author Admin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteImageDataDTO {

    private String uuidKey;
    private String satelliteName;
    private String missionId;
    private String productLevel;
    private String baseLineNumber;
    private String relativeOrbitNumber;
    private String tileNumber;
    private BigDecimal originLongitude;
    private BigDecimal originLatitude;
    private BigDecimal cornerLongitude;
    private BigDecimal cornerLatitude;
    private String rootDataFolderPath;
    private String imageFilePath;
    private String imageFilePathLocal;
    private String geoWmsUrl;
    private String geoWorkSpace;
    private String geoLayerName;
    private Timestamp captureTime;
    private Timestamp secondTime;
    private Timestamp ingestTime;
    private Integer processStatus;
    private String dataVendor;

}
