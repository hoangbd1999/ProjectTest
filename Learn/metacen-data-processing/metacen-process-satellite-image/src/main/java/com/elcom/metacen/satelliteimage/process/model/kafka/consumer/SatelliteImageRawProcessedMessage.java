package com.elcom.metacen.satelliteimage.process.model.kafka.consumer;

import java.io.Serializable;

/**
 *
 * @author Admin
 * metacen kafka topic: `SATELLITE_IMAGE_PROCESSED`
 * Topic chứa dữ liệu kết quả xử lý ảnh vệ tinh của bên AI trả về
 */
public class SatelliteImageRawProcessedMessage implements Serializable {
    
//    private String satelliteImageUuidKey;
    private String uuidKey;
    //private String originalFolderName; // Tên folder chứa data ảnh vệ tinh
    private String rootDataFolderPath; // Full path của folder chứa data ảnh vệ tinh
    private Double originLongitude; // xMin
    private Double originLatitude;  // yMin
    private Double cornerLongitude; // xMax
    private Double cornerLatitude;  // yMax
    private long captureTime;
    private int retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++
    private Boolean processStatus;
    private long totalProcessedTime; // tổng thời gian Engine xử lý cho bản tin này

    public SatelliteImageRawProcessedMessage() {
    }

//    /**
//     * @return the errorMessage
//     */
//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    /**
//     * @param errorMessage the errorMessage to set
//     */
//    public void setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//    }

    /**
     * @return the originLongitude
     */
    public Double getOriginLongitude() {
        return originLongitude;
    }

    /**
     * @param originLongitude the originLongitude to set
     */
    public void setOriginLongitude(Double originLongitude) {
        this.originLongitude = originLongitude;
    }

    /**
     * @return the originLatitude
     */
    public Double getOriginLatitude() {
        return originLatitude;
    }

    /**
     * @param originLatitude the originLatitude to set
     */
    public void setOriginLatitude(Double originLatitude) {
        this.originLatitude = originLatitude;
    }

    /**
     * @return the cornerLongitude
     */
    public Double getCornerLongitude() {
        return cornerLongitude;
    }

    /**
     * @param cornerLongitude the cornerLongitude to set
     */
    public void setCornerLongitude(Double cornerLongitude) {
        this.cornerLongitude = cornerLongitude;
    }

    /**
     * @return the cornerLatitude
     */
    public Double getCornerLatitude() {
        return cornerLatitude;
    }

    /**
     * @param cornerLatitude the cornerLatitude to set
     */
    public void setCornerLatitude(Double cornerLatitude) {
        this.cornerLatitude = cornerLatitude;
    }

    /**
     * @return the processStatus
     */
    public Boolean getProcessStatus() {
        return processStatus;
    }

    /**
     * @param processStatus the processStatus to set
     */
    public void setProcessStatus(Boolean processStatus) {
        this.processStatus = processStatus;
    }

    /**
     * @return the retryNum
     */
    public int getRetryNum() {
        return retryNum;
    }

    /**
     * @param retryNum the retryNum to set
     */
    public void setRetryNum(int retryNum) {
        this.retryNum = retryNum;
    }

    /**
     * @return the totalProcessedTime
     */
    public long getTotalProcessedTime() {
        return totalProcessedTime;
    }

    /**
     * @param totalProcessedTime the totalProcessedTime to set
     */
    public void setTotalProcessedTime(long totalProcessedTime) {
        this.totalProcessedTime = totalProcessedTime;
    }

    /**
     * @return the rootDataFolderPath
     */
    public String getRootDataFolderPath() {
        return rootDataFolderPath;
    }

    /**
     * @param rootDataFolderPath the rootDataFolderPath to set
     */
    public void setRootDataFolderPath(String rootDataFolderPath) {
        this.rootDataFolderPath = rootDataFolderPath;
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
     * @return the captureTime
     */
    public long getCaptureTime() {
        return captureTime;
    }

    /**
     * @param captureTime the captureTime to set
     */
    public void setCaptureTime(long captureTime) {
        this.captureTime = captureTime;
    }
}
