package com.elcom.metacen.dispatcher.process.model.kafka.producer.vsatmedia;

import java.io.Serializable;

/**
 *
 * @author Admin
 * metacen kafka topic: `VSAT_MEDIA_RAW_R`
 * Topic chứa dữ liệu bắn sang service xử lý Vsat Media Process
 */
public class VsatMediaMessageRefinedSink implements Serializable {

    private String mediaUuidKey;
    private String mediaFileUrl;
    private String sourceIp;
    private String destIp;
    private Long dataSourceId;
    private String mediaTypeName;
    private String fileType;
    private Integer retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++

    public VsatMediaMessageRefinedSink() {
    }

    public VsatMediaMessageRefinedSink(String mediaUuidKey, String mediaFileUrl, String sourceIp, String destIp, Long dataSourceId
                                    , String mediaTypeName, String fileType, Integer retryNum) {
        this.mediaUuidKey = mediaUuidKey;
        this.mediaFileUrl = mediaFileUrl;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.dataSourceId = dataSourceId;
        this.mediaTypeName = mediaTypeName;
        this.fileType = fileType;
        this.retryNum = retryNum;
    }
    
    /**
     * @return the mediaUuidKey
     */
    public String getMediaUuidKey() {
        return mediaUuidKey;
    }

    /**
     * @param mediaUuidKey the mediaUuidKey to set
     */
    public void setMediaUuidKey(String mediaUuidKey) {
        this.mediaUuidKey = mediaUuidKey;
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
}
