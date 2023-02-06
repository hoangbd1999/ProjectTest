package com.elcom.metacen.content.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Document(indexName = "media_analyzed")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VsatMediaAnalyzed {

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVsatMediaUuidKey() {
        return vsatMediaUuidKey;
    }

    public void setVsatMediaUuidKey(String vsatMediaUuidKey) {
        this.vsatMediaUuidKey = vsatMediaUuidKey;
    }

    public Integer getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Integer mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public String getMediaTypeName() {
        return mediaTypeName;
    }

    public void setMediaTypeName(String mediaTypeName) {
        this.mediaTypeName = mediaTypeName;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public Long getDestId() {
        return destId;
    }

    public void setDestId(Long destId) {
        this.destId = destId;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public Integer getDestPort() {
        return destPort;
    }

    public void setDestPort(Integer destPort) {
        this.destPort = destPort;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileContentUtf8() {
        return fileContentUtf8;
    }

    public void setFileContentUtf8(String fileContentUtf8) {
        this.fileContentUtf8 = fileContentUtf8;
    }

    public String getFileContentGB18030() {
        return fileContentGB18030;
    }

    public void setFileContentGB18030(String fileContentGB18030) {
        this.fileContentGB18030 = fileContentGB18030;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public String getDataVendor() {
        return dataVendor;
    }

    public void setDataVendor(String dataVendor) {
        this.dataVendor = dataVendor;
    }

    public String getAnalyzedEngine() {
        return analyzedEngine;
    }

    public void setAnalyzedEngine(String analyzedEngine) {
        this.analyzedEngine = analyzedEngine;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public Date getIngestTime() {
        return ingestTime;
    }

    public void setIngestTime(Date ingestTime) {
        this.ingestTime = ingestTime;
    }

    public Date getProcessTime() {
        return processTime;
    }

    public void setProcessTime(Date processTime) {
        this.processTime = processTime;
    }

    public Integer getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailReplyTo() {
        return mailReplyTo;
    }

    public void setMailReplyTo(String mailReplyTo) {
        this.mailReplyTo = mailReplyTo;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailAttachments() {
        return mailAttachments;
    }

    public void setMailAttachments(String mailAttachments) {
        this.mailAttachments = mailAttachments;
    }

    public String getMailContents() {
        return mailContents;
    }

    public void setMailContents(String mailContents) {
        this.mailContents = mailContents;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailScanVirus() {
        return mailScanVirus;
    }

    public void setMailScanVirus(String mailScanVirus) {
        this.mailScanVirus = mailScanVirus;
    }

    public String getMailScanResult() {
        return mailScanResult;
    }

    public void setMailScanResult(String mailScanResult) {
        this.mailScanResult = mailScanResult;
    }

    public String getMailUserAgent() {
        return mailUserAgent;
    }

    public void setMailUserAgent(String mailUserAgent) {
        this.mailUserAgent = mailUserAgent;
    }

    public String getMailContentLanguage() {
        return mailContentLanguage;
    }

    public void setMailContentLanguage(String mailContentLanguage) {
        this.mailContentLanguage = mailContentLanguage;
    }

    public String getMailXMail() {
        return mailXMail;
    }

    public void setMailXMail(String mailXMail) {
        this.mailXMail = mailXMail;
    }

    public String getMailRaw() {
        return mailRaw;
    }

    public void setMailRaw(String mailRaw) {
        this.mailRaw = mailRaw;
    }

}
