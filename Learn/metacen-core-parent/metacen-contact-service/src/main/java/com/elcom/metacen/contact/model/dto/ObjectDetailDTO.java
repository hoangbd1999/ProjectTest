package com.elcom.metacen.contact.model.dto;

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
public class ObjectDetailDTO {

    private String objectId;
    private String objectMmsi;
    private String objectUuid;
    private String objectType;
    private String objectName;

}
