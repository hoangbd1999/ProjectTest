package com.elcom.metacen.contact.model.dto;

import java.io.Serializable;
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
public class ObjectCriteria implements Serializable {

    private Integer page;
    private Integer size;
    private String term;
    private List<String> keyId;
    private List<String> objectTypeLst;
    private List<Integer> countryIds;
    private List<String> sideIds;
    private List<String> keywordIds;
}
