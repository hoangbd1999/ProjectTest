package com.elcom.metacen.vsat.media.process.model.kafka.consumer;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Admin metacen kafka topic: `VSAT_MEDIA_PROCESSED` Topic chứa dữ liệu
 * kết quả xử lý media content bên metacen-content-process trả về
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VsatMediaProcessedMessage implements Serializable {

    private String uuidKey;
    private String mediaUuidKey;
    private Integer mediaTypeId;
    private String mediaTypeName;
    private Long sourceId;
    private String sourceName;
    private String sourceIp;
    private Integer sourcePort;
    private Long destId;
    private String destName;
    private String destIp;
    private Integer destPort;
    private String filePath;
    private String mediaFileUrl;
    private String fileType;
    private String fileSize;
    private Integer dataSourceId;
    private String dataSourceName;
    private Integer direction;
    private String dataVendor;
    private String analyzedEngine;
    private String processType;
    private Integer processStatus;
    private Long eventTime;
    private Boolean status;
    private Integer type;
    private String err;
    private Object data;
    private int retryNum; // Mặc định khi tạo mới là = 0, có lỗi thì ++
}
