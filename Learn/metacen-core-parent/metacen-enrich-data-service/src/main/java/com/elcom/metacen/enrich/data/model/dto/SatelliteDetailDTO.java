package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

/**
 *
 * @author hoangbd
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteDetailDTO {

    private String uuidKey;
    private String satelliteName;
    private String missionId;
    private String productLevel;
    private String baseLineNumber;
    private String relativeOrbitNumber;
    private String tileNumber;
    private Float originLongitude;
    private Float originLatitude;
    private Float cornerLongitude;
    private Float cornerLatitude;
    private String rootDataFolderPath;
    private String imageFilePath;
    private String imageFilePathLocal;
    private String geoWmsUrl;
    private String geoWorkSpace;
    private String geoLayerName;
    private Timestamp captureTime;
    private Timestamp processTime;
    private Timestamp ingestTime;
    private Integer processStatus;
    private String dataVendor;
    private List<SatelliteImageObjectAnalyzedDTO> listObject;
}
