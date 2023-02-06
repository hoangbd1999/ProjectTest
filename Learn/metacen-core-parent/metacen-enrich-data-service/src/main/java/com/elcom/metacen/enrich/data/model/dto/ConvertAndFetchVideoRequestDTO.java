package com.elcom.metacen.enrich.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertAndFetchVideoRequestDTO {

    private String filePath;
    private String targetExtension;
}
