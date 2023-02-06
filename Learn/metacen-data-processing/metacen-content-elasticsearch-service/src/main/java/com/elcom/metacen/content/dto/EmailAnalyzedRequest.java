package com.elcom.metacen.content.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

public class EmailAnalyzedRequest {

    private String id;
    private String vsatMediaUuidKey;
    private Integer mediaTypeId;
    private String mediaTypeName;
    private String sourceName;
    private String sourceIp;
    private Long sourcePort;
    private String sourcePhone;
    private String destName;
    private String destIp;
    private Long destPort;
    private String destPhone;
    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long dataSourceId;
    private String dataSourceName;
    private Integer direction;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;
    private String analyzedEngine;
    private String dataVendor;
    private Object from;
    private Object replyTo;
    private Object to;
    private Object attachments;
    private String contents;
    private String subject;
    private String scanVirus;
    private String scanResult;
    private String userAgent;
    private String contentLanguage;
    private String XMail;
    private Object raw;

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

    public Long getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Long sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSourcePhone() {
        return sourcePhone;
    }

    public void setSourcePhone(String sourcePhone) {
        this.sourcePhone = sourcePhone;
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

    public Long getDestPort() {
        return destPort;
    }

    public void setDestPort(Long destPort) {
        this.destPort = destPort;
    }

    public String getDestPhone() {
        return destPhone;
    }

    public void setDestPhone(String destPhone) {
        this.destPhone = destPhone;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public String getAnalyzedEngine() {
        return analyzedEngine;
    }

    public void setAnalyzedEngine(String analyzedEngine) {
        this.analyzedEngine = analyzedEngine;
    }

    public String getDataVendor() {
        return dataVendor;
    }

    public void setDataVendor(String dataVendor) {
        this.dataVendor = dataVendor;
    }

    public Object getFrom() {
        return from;
    }

    public void setFrom(Object from) {
        this.from = from;
    }

    public Object getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Object replyTo) {
        this.replyTo = replyTo;
    }

    public Object getTo() {
        return to;
    }

    public void setTo(Object to) {
        this.to = to;
    }

    public Object getAttachments() {
        return attachments;
    }

    public void setAttachments(Object attachments) {
        this.attachments = attachments;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getScanVirus() {
        return scanVirus;
    }

    public void setScanVirus(String scanVirus) {
        this.scanVirus = scanVirus;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public String getXMail() {
        return XMail;
    }

    public void setXMail(String XMail) {
        this.XMail = XMail;
    }

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
    }
}
