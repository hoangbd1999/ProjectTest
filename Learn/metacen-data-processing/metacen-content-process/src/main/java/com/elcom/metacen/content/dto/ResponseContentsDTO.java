package com.elcom.metacen.content.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Data
public class ResponseContentsDTO {

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
    private Integer retryNum;
    private Boolean status;
    private Integer type;
    private String err;
    private Integer processStatus;
    private Object data;

    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
