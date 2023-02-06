package com.elcom.metacen.raw.data.model.dto;

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
public class VsatMediaOverallStatisticResponseDTO {

    private Integer audio;
    private Integer video;
    private Integer web;
    private Integer email;
    private Integer transferFile;
    private Integer undefined;
    private Integer rawData;
    private Integer analyzedData;
    private Integer totalData;
}
