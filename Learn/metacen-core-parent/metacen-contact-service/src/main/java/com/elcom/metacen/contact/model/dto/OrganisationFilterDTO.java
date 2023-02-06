package com.elcom.metacen.contact.model.dto;

import java.util.List;
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
public class OrganisationFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private String term;
    private List<String> organisationTypeLst;
    private List<Integer> countryIds;
    private List<String> sideIds;
    private List<String> keywordIds;
}
