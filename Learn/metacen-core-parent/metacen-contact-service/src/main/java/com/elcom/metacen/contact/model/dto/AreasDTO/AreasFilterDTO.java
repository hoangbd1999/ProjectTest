package com.elcom.metacen.contact.model.dto.AreasDTO;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AreasFilterDTO {
    private Integer page;
    private Integer size;
    private String term;
    private String sort;
    private List<String> sideIds;
    private List<String> keywordIds;
}
