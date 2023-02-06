package com.elcom.metacen.raw.data.model.dto;

import lombok.*;

/**
 *
 * @author hoangbd
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaDataSatelliteDTO {

    private String filePath;
    private String filePathLocal;
    private String fileName;
    private String fileSize;
}
