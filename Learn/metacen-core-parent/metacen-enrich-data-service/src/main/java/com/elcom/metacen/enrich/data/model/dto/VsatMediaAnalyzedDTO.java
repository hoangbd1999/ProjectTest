package com.elcom.metacen.enrich.data.model.dto;

import java.math.BigInteger;
import java.util.Date;
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
public class VsatMediaAnalyzedDTO {

    private String id;
    private String vsatMediaUuidKey;
    private Long mediaTypeId;
    private String mediaTypeName;
    private BigInteger sourceId;
    private String sourceName;
    private String sourceIp;
    private Long sourcePort;
    private BigInteger destId;
    private String destName;
    private String destIp;
    private Long destPort;
    private String filePath;
    private String filePathLocal;
    private String fileType;
    private BigInteger fileSize;
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
    private Date eventTime;
    private Date ingestTime;
    private Date processTime;
    private Integer processStatus;
}
