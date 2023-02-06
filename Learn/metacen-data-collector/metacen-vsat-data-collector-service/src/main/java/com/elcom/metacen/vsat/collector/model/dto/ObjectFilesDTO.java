package com.elcom.metacen.vsat.collector.model.dto;

import lombok.Data;
import java.io.Serializable;

/**
 *
 * @author hoangbd
 */
@Data
public class ObjectFilesDTO implements Serializable {

    private Long Id;
    private String objectId;
    private String fileType;
    private String objectType;
    private String imagePath;
    private int isDelete;

    public ObjectFilesDTO(String objectId, String fileType, String objectType, String imagePath, int isDelete) {
        this.objectId = objectId;
        this.fileType = fileType;
        this.objectType = objectType;
        this.imagePath = imagePath;
        this.isDelete = isDelete;
    }
}
