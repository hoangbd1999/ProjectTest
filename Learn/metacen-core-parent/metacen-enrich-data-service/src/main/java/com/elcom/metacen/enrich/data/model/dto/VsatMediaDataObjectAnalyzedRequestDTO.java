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
public class VsatMediaDataObjectAnalyzedRequestDTO {

    private String uuidKey;
    private String vsatMediaDataAnalyzedUuidKey;
    private String objectId;
    private String objectMmsi;
    private String objectUuid;
    private String objectType;
    private String objectName;
    private String ingestTime;
    private Integer isDeleted;

}
