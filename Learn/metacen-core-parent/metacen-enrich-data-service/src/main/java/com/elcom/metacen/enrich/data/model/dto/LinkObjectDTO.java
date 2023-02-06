package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkObjectDTO {

    private String vsatMediaDataAnalyzedUuidKey;
    private List<VsatMediaDataObjectAnalyzedDetailDTO> listObject;
}
