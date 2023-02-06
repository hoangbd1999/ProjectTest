package com.elcom.metacen.contact.model.dto;

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
public class OtherObjectFilterDTO {

    private Integer page;
    private Integer size;
    private String term;
    private String sort;
    private List<Integer> countryIds;
    private List<String> sideIds;
    private List<String> keywordIds;
}
