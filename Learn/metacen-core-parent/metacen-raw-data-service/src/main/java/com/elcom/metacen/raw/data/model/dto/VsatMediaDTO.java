package com.elcom.metacen.raw.data.model.dto;

import java.math.BigInteger;
import java.sql.Timestamp;
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
public class VsatMediaDTO {

    private String uuidKey;
    private Long mediaTypeId;
    private String mediaTypeName;
    private BigInteger sourceId;
    private String sourceName;
    private String sourceIp;
    private Long sourcePort;
    private String sourcePhone;
    private BigInteger destId;
    private String destName;
    private String destIp;
    private Long destPort;
    private String destPhone;
    private String filePath;
    private String filePathLocal;
    private String fileName;
    private String fileType;
    private BigInteger fileSize;
    private Long dataSourceId;
    private String dataSourceName;
    private Integer direction;
    private Long partName;
    private Timestamp eventTime;
    private Timestamp ingestTime;
    private Timestamp processTime;
    private Integer processStatus;
    private String dataVendor;
}
