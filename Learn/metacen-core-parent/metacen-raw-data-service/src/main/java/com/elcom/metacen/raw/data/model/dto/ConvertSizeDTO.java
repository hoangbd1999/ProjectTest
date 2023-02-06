package com.elcom.metacen.raw.data.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertSizeDTO {

    private Double fileSizeFromValue;
    private Double fileSizeToValue;
}
