package com.elcom.metacen.mapping.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MappingAisFilterDTO {

    private Integer page;
    private Integer size;
    private String term;
    private String sort;
    private Integer aisMmsi;
    private List<String> objectTypes;
    private String objectId;
}
