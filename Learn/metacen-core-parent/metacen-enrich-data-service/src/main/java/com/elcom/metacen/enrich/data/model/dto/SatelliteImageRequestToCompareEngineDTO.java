package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

/**
 *
 * @author Admin
 * Send to kafka topic `SATELLITE_IMAGE_CHANGES_TO_PROCESS`
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteImageRequestToCompareEngineDTO {

    private String uuidKey;
    private String rootDataFolderPathOrigin;
    private String rootDataFolderPathCompare;
    private String resultFolder;
    private Integer retryNum;
}
