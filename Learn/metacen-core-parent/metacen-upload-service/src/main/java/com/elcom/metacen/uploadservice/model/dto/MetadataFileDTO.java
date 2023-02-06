package com.elcom.metacen.uploadservice.model.dto;

import lombok.*;

/**
 *
 * @author Admin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataFileDTO {

    private String filePathLocal;
    private String fileName;
    private String fileSize;
}
