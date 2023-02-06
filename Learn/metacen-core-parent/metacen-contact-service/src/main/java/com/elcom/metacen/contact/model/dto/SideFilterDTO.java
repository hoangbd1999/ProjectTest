package com.elcom.metacen.contact.model.dto;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SideFilterDTO {
    private Integer page;
    private Integer size;
    private String term;
}
