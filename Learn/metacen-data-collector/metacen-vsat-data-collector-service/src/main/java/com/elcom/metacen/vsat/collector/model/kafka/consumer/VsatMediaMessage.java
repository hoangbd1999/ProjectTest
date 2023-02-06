package com.elcom.metacen.vsat.collector.model.kafka.consumer;

import java.io.Serializable;

/**
 *
 * @author anhdv
 * vsat kafka topic: `VSAT_MEDIA_T`
 */
public class VsatMediaMessage implements Serializable {
    
    private String uuidKey;
    private Long eventTime;
    private Long procTime;
    private Integer mediaTypeId;
    private String mediaTypeName;
    private String sourceIp;
    private Long srcId;
    private String srcObjId;
    private String srcName;
    private String destIp;
    private Long destId;
    private String destObjId;
    private String destName;
    private Integer sourcePort;
    private Integer destPort;
    private String filePath;
    private String sourcePhone;
    private String destPhone;
    private String fileSize;
    private String fileName;
    private String fileType;
    private Long dataSource;
    private String dataSourceName;
    private Integer direction;
    private String partName;

    public VsatMediaMessage() {
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
     * @return the procTime
     */
    public Long getProcTime() {
        return procTime;
    }

    /**
     * @param procTime the procTime to set
     */
    public void setProcTime(Long procTime) {
        this.procTime = procTime;
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
     * @return the srcObjId
     */
    public String getSrcObjId() {
        return srcObjId;
    }

    /**
     * @param srcObjId the srcObjId to set
     */
    public void setSrcObjId(String srcObjId) {
        this.srcObjId = srcObjId;
    }

    /**
     * @return the srcName
     */
    public String getSrcName() {
        return srcName;
    }

    /**
     * @param srcName the srcName to set
     */
    public void setSrcName(String srcName) {
        this.srcName = srcName;
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
     * @return the destObjId
     */
    public String getDestObjId() {
        return destObjId;
    }

    /**
     * @param destObjId the destObjId to set
     */
    public void setDestObjId(String destObjId) {
        this.destObjId = destObjId;
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
     * @return the dataSource
     */
    public Long getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(Long dataSource) {
        this.dataSource = dataSource;
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

    /**
     * @return the partName
     */
    public String getPartName() {
        return partName;
    }

    /**
     * @param partName the partName to set
     */
    public void setPartName(String partName) {
        this.partName = partName;
    }

    /**
     * @return the srcId
     */
    public Long getSrcId() {
        return srcId;
    }

    /**
     * @param srcId the srcId to set
     */
    public void setSrcId(Long srcId) {
        this.srcId = srcId;
    }
}
