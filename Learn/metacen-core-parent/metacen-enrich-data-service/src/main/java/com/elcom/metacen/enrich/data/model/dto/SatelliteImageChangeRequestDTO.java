package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

/**
 *
 * @author Admin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteImageChangeRequestDTO {

    private String uuidKey;
    private String tileNumber;
    private String timeFileOrigin;
    private String timeFileCompare;
    private String imagePathFileOrigin;
    private String imagePathFileCompare;
    private String timeReceiveResult;
    private String createdBy;
    private String ingestTime;
    private Integer isDeleted;
}
