package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

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
public class SatelliteCaptureTimeDTO {
    
    private Timestamp captureTime;
    private String rootDataFolderPath;
    private String imageFilePath;
}
