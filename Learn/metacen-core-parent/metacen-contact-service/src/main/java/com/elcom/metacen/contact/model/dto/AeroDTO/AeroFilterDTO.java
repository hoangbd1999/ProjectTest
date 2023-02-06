package com.elcom.metacen.contact.model.dto.AeroDTO;

import lombok.*;
import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AeroFilterDTO {

    private Integer page;
    private Integer size;
    private String term;
    private String sort;
    private List<Integer> countryIds;
    private List<String> sideIds;
    private List<String> keywordIds;
}
