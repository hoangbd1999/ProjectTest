package com.elcom.metacen.satelliteimage.process.model.kafka.consumer;

import java.io.Serializable;

/**
 *
 * @author Admin
 * metacen kafka topic: `SATELLITE_IMAGE_CHANGES_PROCESSED`
 * Topic chứa dữ liệu kết quả so sánh ảnh vệ tinh của bên AI trả về
 */
public class SatelliteImageCompareProcessedMessage implements Serializable {
    
    private String uuidKey;
    private String rootDataFolderPathOrigin;
    private String rootDataFolderPathCompare;
    private String resultFolder;  // Thư mục chứa các file kết quả so sánh
    private int retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++
    private Boolean processStatus; // Trạng thái xử lý AI engine
    private long totalProcessedTime; // tổng thời gian Engine xử lý cho bản tin này

    public SatelliteImageCompareProcessedMessage() {
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
     * @return the rootDataFolderPathOrigin
     */
    public String getRootDataFolderPathOrigin() {
        return rootDataFolderPathOrigin;
    }

    /**
     * @param rootDataFolderPathOrigin the rootDataFolderPathOrigin to set
     */
    public void setRootDataFolderPathOrigin(String rootDataFolderPathOrigin) {
        this.rootDataFolderPathOrigin = rootDataFolderPathOrigin;
    }

    /**
     * @return the rootDataFolderPathCompare
     */
    public String getRootDataFolderPathCompare() {
        return rootDataFolderPathCompare;
    }

    /**
     * @param rootDataFolderPathCompare the rootDataFolderPathCompare to set
     */
    public void setRootDataFolderPathCompare(String rootDataFolderPathCompare) {
        this.rootDataFolderPathCompare = rootDataFolderPathCompare;
    }

    /**
     * @return the resultFolder
     */
    public String getResultFolder() {
        return resultFolder;
    }

    /**
     * @param resultFolder the resultFolder to set
     */
    public void setResultFolder(String resultFolder) {
        this.resultFolder = resultFolder;
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
}
