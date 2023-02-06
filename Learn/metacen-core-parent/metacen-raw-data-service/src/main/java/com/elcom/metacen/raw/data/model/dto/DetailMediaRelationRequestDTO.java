package com.elcom.metacen.raw.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DetailMediaRelationRequestDTO {

    private String uuidKeyFrom;
    private String uuidKeyTo;
    private Integer partName;
}
