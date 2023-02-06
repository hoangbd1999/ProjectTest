/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 *
 * @author Admin
 */
@Entity
@Getter
@Setter
@Document(indexName = "media_analyzed")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VsatMediaAnalyzed implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "vsatMediaUuidKey")
    private String vsatMediaUuidKey;

    @Field(name = "mediaTypeId")
    private Integer mediaTypeId;

    @Field(name = "mediaTypeName")
    private String mediaTypeName;

    @Field(name = "sourceId")
    private Long sourceId;

    @Field(name = "sourceName")
    private String sourceName;

    @Field(name = "sourceIp")
    private String sourceIp;

    @Field(name = "sourcePort")
    private Integer sourcePort;

    @Field(name = "destId")
    private Long destId;

    @Field(name = "destName")
    private String destName;

    @Field(name = "destIp")
    private String destIp;

    @Field(name = "destPort")
    private Integer destPort;

    @Field(name = "filePath")
    private String filePath;

    @Field(name = "fileType")
    private String fileType;

    @Field(name = "fileSize")
    private Long fileSize;

    @Field(name = "fileContentUtf8")
    private String fileContentUtf8;

    @Field(name = "fileContentGB18030")
    private String fileContentGB18030;

    @Field(name = "mailFrom")
    private String mailFrom;

    @Field(name = "mailReplyTo")
    private String mailReplyTo;

    @Field(name = "mailTo")
    private String mailTo;

    @Field(name = "mailAttachments")
    private String mailAttachments;

    @Field(name = "mailContents")
    private String mailContents;

    @Field(name = "mailSubject")
    private String mailSubject;

    @Field(name = "mailScanVirus")
    private String mailScanVirus;

    @Field(name = "mailScanResult")
    private String mailScanResult;

    @Field(name = "mailUserAgent")
    private String mailUserAgent;

    @Field(name = "mailContentLanguage")
    private String mailContentLanguage;

    @Field(name = "mailXMail")
    private String mailXMail;

    @Field(name = "mailRaw")
    private String mailRaw;

    @Field(name = "dataSourceId")
    private Long dataSourceId;

    @Field(name = "dataSourceName")
    private String dataSourceName;

    @Field(name = "direction")
    private Integer direction;

    @Field(name = "dataVendor")
    private String dataVendor;

    @Field(name = "analyzedEngine")
    private String analyzedEngine;

    @Field(name = "processType")
    private String processType;

    @Field(name = "eventTime")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Field(name = "ingestTime")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Field(name = "processTime")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date processTime;

    @Field(name = "processStatus")
    private Integer processStatus;

}
