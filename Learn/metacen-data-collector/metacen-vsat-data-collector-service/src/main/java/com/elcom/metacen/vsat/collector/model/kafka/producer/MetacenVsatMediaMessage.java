package com.elcom.metacen.vsat.collector.model.kafka.producer;

import java.io.Serializable;

/**
 *
 * @author anhdv
 * metacen kafka topic: `VSAT_MEDIA_RAW`
 * Nguồn VSAT
 */
// public class MetacenVsatMediaMessage extends Job implements Serializable {
public class MetacenVsatMediaMessage implements Serializable {
    
    private String uuidKey;
    private Long eventTime;
    private Integer mediaTypeId;
    private String mediaTypeName;
    private String sourceIp;
    private String destIp;
    private Long sourceId;
    private Long destId;
    private String sourceName;
    private String destName;
    private Integer sourcePort;
    private Integer destPort;
    private String sourcePhone;
    private String destPhone;
    private String filePath;
    private String fileName;
    private String fileSize;
    private String fileType;
    private Long dataSourceId;
    private String dataSourceName;
    private Integer direction;

    public MetacenVsatMediaMessage() {
    }

    public MetacenVsatMediaMessage(String uuidKey, Long eventTime, Integer mediaTypeId, String mediaTypeName, String sourceIp, String destIp
                        , Long sourceId, Long destId, String sourceName, String destName, Integer sourcePort, Integer destPort, String sourcePhone, String destPhone
                        , String filePath, String fileName, String fileSize, String fileType, Long dataSourceId, String dataSourceName, Integer direction) {
        this.uuidKey = uuidKey;
        this.eventTime = eventTime;
        this.mediaTypeId = mediaTypeId;
        this.mediaTypeName = mediaTypeName;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.sourceId = sourceId;
        this.destId = destId;
        this.sourceName = sourceName;
        this.destName = destName;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
        this.sourcePhone = sourcePhone;
        this.destPhone = destPhone;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.dataSourceId = dataSourceId;
        this.dataSourceName = dataSourceName;
        this.direction = direction;
    }

    /**
     * @return the uuidKey
     */
    public String getUuidKey() {
        return uuidKey;
    }

    /**
     * @param uuidKey the uuidKey to set
     */
    public void setUuidKey(String uuidKey) {
        this.uuidKey = uuidKey;
    }

    /**
     * @return the eventTime
     */
    public Long getEventTime() {
        return eventTime;
    }

    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the mediaTypeId
     */
    public Integer getMediaTypeId() {
        return mediaTypeId;
    }

    /**
     * @param mediaTypeId the mediaTypeId to set
     */
    public void setMediaTypeId(Integer mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    /**
     * @return the mediaTypeName
     */
    public String getMediaTypeName() {
        return mediaTypeName;
    }

    /**
     * @param mediaTypeName the mediaTypeName to set
     */
    public void setMediaTypeName(String mediaTypeName) {
        this.mediaTypeName = mediaTypeName;
    }

    /**
     * @return the sourceIp
     */
    public String getSourceIp() {
        return sourceIp;
    }

    /**
     * @param sourceIp the sourceIp to set
     */
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    /**
     * @return the destIp
     */
    public String getDestIp() {
        return destIp;
    }

    /**
     * @param destIp the destIp to set
     */
    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    /**
     * @return the sourceId
     */
    public Long getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the destId
     */
    public Long getDestId() {
        return destId;
    }

    /**
     * @param destId the destId to set
     */
    public void setDestId(Long destId) {
        this.destId = destId;
    }

    /**
     * @return the sourceName
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * @param sourceName the sourceName to set
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * @return the destName
     */
    public String getDestName() {
        return destName;
    }

    /**
     * @param destName the destName to set
     */
    public void setDestName(String destName) {
        this.destName = destName;
    }

    /**
     * @return the sourcePort
     */
    public Integer getSourcePort() {
        return sourcePort;
    }

    /**
     * @param sourcePort the sourcePort to set
     */
    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    /**
     * @return the destPort
     */
    public Integer getDestPort() {
        return destPort;
    }

    /**
     * @param destPort the destPort to set
     */
    public void setDestPort(Integer destPort) {
        this.destPort = destPort;
    }

    /**
     * @return the sourcePhone
     */
    public String getSourcePhone() {
        return sourcePhone;
    }

    /**
     * @param sourcePhone the sourcePhone to set
     */
    public void setSourcePhone(String sourcePhone) {
        this.sourcePhone = sourcePhone;
    }

    /**
     * @return the destPhone
     */
    public String getDestPhone() {
        return destPhone;
    }

    /**
     * @param destPhone the destPhone to set
     */
    public void setDestPhone(String destPhone) {
        this.destPhone = destPhone;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the fileSize
     */
    public String getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return the fileType
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * @param fileType the fileType to set
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * @return the dataSourceId
     */
    public Long getDataSourceId() {
        return dataSourceId;
    }

    /**
     * @param dataSourceId the dataSourceId to set
     */
    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    /**
     * @return the dataSourceName
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * @param dataSourceName the dataSourceName to set
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    /**
     * @return the direction
     */
    public Integer getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(Integer direction) {
        this.direction = direction;
    }
}
