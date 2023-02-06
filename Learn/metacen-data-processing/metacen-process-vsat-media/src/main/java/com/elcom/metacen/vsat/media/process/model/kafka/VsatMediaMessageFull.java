package com.elcom.metacen.vsat.media.process.model.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Admin
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VsatMediaMessageFull implements Serializable {

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
    private Long eventTime;
    private Integer processStatus;
    private Integer retryNum; // Mặc định khi tạo mới là = 0, có lỗi thì ++

    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
