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
public class SatelliteImageChangeResponseDTO {

    private String uuidKey;
    private String tileNumber;
    private Timestamp timeFileOrigin;
    private Timestamp timeFileCompare;
    private String imagePathFileOrigin;
    private String imagePathFileCompare;
    private Timestamp timeReceiveResult;
    private String createdBy;
    private Timestamp ingestTime;
    private Integer processStatus;
    private Integer retryTimes;
    private Integer isDeleted;
}
