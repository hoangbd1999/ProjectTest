package com.elcom.metacen.mapping.data.model.dto;

import lombok.*;

import java.util.List;

/**
 *
 * @author hoangbd
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MappingVsatFilterDTO {

    private Integer page;
    private Integer size;
    private String term;
    private String sort;
    private List<Integer> vsatDataSourceIds;
    private String vsatIpAddress;
    private List<String> objectTypes;
    private String objectId;
}
