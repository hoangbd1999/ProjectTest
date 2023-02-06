package com.elcom.metacen.vsat.media.process.model.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Admin
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VsatMediaDTO implements Serializable {

    private String id;
    private String vsatMediaUuidKey;
    private Long mediaTypeId;
    private String mediaTypeName;
    private Long sourceId;
    private String sourceName;
    private String sourceIp;
    private Long sourcePort;
    private Long destId;
    private String destName;
    private String destIp;
    private Long destPort;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String fileContentUtf8;
    private String fileContentGB18030;
    private String mailFrom;
    private String mailReplyTo;
    private String mailTo;
    private String mailAttachments;
    private String mailContents;
    private String mailSubject;
    private String mailScanVirus;
    private String mailScanResult;
    private String mailUserAgent;
    private String mailContentLanguage;
    private String mailXMail;
    private String mailRaw;
    private Long dataSourceId;
    private String dataSourceName;
    private Integer direction;
    private String dataVendor;
    private String analyzedEngine;
    private String processType;
    private Date eventTime;
    private Date ingestTime;
    private Date processTime;
    private Integer processStatus;
    private Integer retryNum;

}
