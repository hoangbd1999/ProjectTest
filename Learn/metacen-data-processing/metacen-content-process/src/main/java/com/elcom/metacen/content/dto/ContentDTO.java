package com.elcom.metacen.content.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ContentDTO {
//    String mediaUuidKey ;
//    String mediaFileUrl;
//    String sourceIp ;
//    String destIp;
//    String dataSourceId ;
//    String dataType;
//    String fileFormat;

    private String uuidKey;
    private String mediaUuidKey;
    private Integer mediaTypeId; // dataType
    private String mediaTypeName;
    private BigInteger sourceId;
    private String sourceName;
    private String sourceIp;
    private Integer sourcePort;
    private BigInteger destId;
    private String destName;
    private String destIp;
    private Integer destPort;
    private String filePath;
    private String mediaFileUrl;
    private String fileType; // fileFormat
    private String fileSize;
    private Integer dataSourceId;
    private String dataSourceName;
    private Integer direction;
    private String dataVendor;
    private String analyzedEngine;
    private String processType;
    private Long eventTime;
    private  Integer processStatus;
    private Integer retryNum;

}
