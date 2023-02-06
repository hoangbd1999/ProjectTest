package com.elcom.metacen.dispatcher.process.model.kafka.consumer.vsatmedia;

import com.elcom.metacen.dto.redis.Job;
import java.io.Serializable;

/**
 *
 * @author Admin metacen kafka topic: `VSAT_MEDIA_RAW` Topic chứa dữ liệu thô
 * Media nguồn VSAT
 */
public class VsatMediaMessageFull extends Job implements Serializable {

    private String uuidKey;
    private long eventTime;
    private String mediaFileUrl;
    private int mediaTypeId;
    private String mediaTypeName;
    private long sourceId;
    private String sourceName;
    private String sourceIp;
    private int sourcePort;
    private long destId;
    private String destName;
    private String destIp;
    private int destPort;
    private String filePath;
    private String fileSize;
    private String fileType;
    private long dataSourceId;
    private String dataSourceName;
    private String dataVendor;
    private Integer direction;
    private Integer retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++

    public VsatMediaMessageFull() {
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
    public long getEventTime() {
        return eventTime;
    }

    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the mediaTypeId
     */
    public int getMediaTypeId() {
        return mediaTypeId;
    }

    /**
     * @param mediaTypeId the mediaTypeId to set
     */
    public void setMediaTypeId(int mediaTypeId) {
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
    public long getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the destId
     */
    public long getDestId() {
        return destId;
    }

    /**
     * @param destId the destId to set
     */
    public void setDestId(long destId) {
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
    public int getSourcePort() {
        return sourcePort;
    }

    /**
     * @param sourcePort the sourcePort to set
     */
    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    /**
     * @return the destPort
     */
    public int getDestPort() {
        return destPort;
    }

    /**
     * @param destPort the destPort to set
     */
    public void setDestPort(int destPort) {
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
    public long getDataSourceId() {
        return dataSourceId;
    }

    /**
     * @param dataSourceId the dataSourceId to set
     */
    public void setDataSourceId(long dataSourceId) {
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
     * @return the dataVendor
     */
    public String getDataVendor() {
        return dataVendor;
    }

    /**
     * @param dataVendor the dataVendor to set
     */
    public void setDataVendor(String dataVendor) {
        this.dataVendor = dataVendor;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    /**
     * @return the retryNum
     */
    public Integer getRetryNum() {
        return retryNum;
    }

    /**
     * @param retryNum the retryNum to set
     */
    public void setRetryNum(Integer retryNum) {
        this.retryNum = retryNum;
    }

    /**
     * @return the mediaFileUrl
     */
    public String getMediaFileUrl() {
        return mediaFileUrl;
    }

    /**
     * @param mediaFileUrl the mediaFileUrl to set
     */
    public void setMediaFileUrl(String mediaFileUrl) {
        this.mediaFileUrl = mediaFileUrl;
    }
}
