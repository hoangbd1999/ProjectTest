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
public class VsatMediaDataObjectAnalyzedDetailDTO {

    private String objectId;
    private String objectMmsi;
    private String objectUuid;
    private String objectType;
    private String objectName;

}
