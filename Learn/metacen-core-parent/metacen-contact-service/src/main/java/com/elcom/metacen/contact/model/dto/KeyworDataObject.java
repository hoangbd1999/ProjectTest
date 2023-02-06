package com.elcom.metacen.contact.model.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KeyworDataObject implements Serializable {

    private Integer page;
    private Integer size;
    private String term;
    private List<String> objectTypeLst;
    private List<String> keywordIds;
    private List<String> objectIds;
}
