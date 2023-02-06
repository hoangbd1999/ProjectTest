package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MarineVesselFilterDTO {
    private Integer page;
    private Integer size;
    private String term;
    private String name;
    private Long mmsi;
    private String sort;
    private List<Integer> countryIds;
    private List<String> sideIds;
    private List<String> keywordIds;
}
