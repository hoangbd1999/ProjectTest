package com.elcom.metacen.mapping.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class MappingAisRequestDTO implements Serializable {

    private Integer aisMmsi;
    private String aisShipName;
    private String objectType;
    private String objectId;
    private String objectUuid;
    private String objectName;
}
