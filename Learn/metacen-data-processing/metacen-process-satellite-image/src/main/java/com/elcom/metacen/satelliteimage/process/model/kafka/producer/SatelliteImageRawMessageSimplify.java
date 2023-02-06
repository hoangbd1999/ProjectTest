package com.elcom.metacen.satelliteimage.process.model.kafka.producer;

import com.elcom.metacen.dto.redis.Job;
import java.io.Serializable;

/**
 *
 * @author Admin
 * metacen kafka topic: `SATELLITE_IMAGE_RAW_RETRY`
 * Topic chứa dữ liệu thô ảnh vệ tinh cần được xử lý lại
 */
public class SatelliteImageRawMessageSimplify extends Job implements Serializable {
    
    private String uuidKey;
    private String rootDataFolderPath; // Đường dẫn gốc của server cắt ảnh vệ tinh
    private Double originLongitude; // xMin
    private Double originLatitude;  // yMin
    private Double cornerLongitude; // xMax
    private Double cornerLatitude;  // yMax
    private Integer retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++

    public SatelliteImageRawMessageSimplify() {
    }
    
    public SatelliteImageRawMessageSimplify(String uuidKey, String rootDataFolderPath
                                        , Double originLongitude, Double originLatitude, Double cornerLongitude, Double cornerLatitude, Integer retryNum) {
        this.uuidKey = uuidKey;
        this.rootDataFolderPath = rootDataFolderPath;
        this.originLongitude = originLongitude;
        this.originLatitude = originLatitude;
        this.cornerLongitude = cornerLongitude;
        this.cornerLatitude = cornerLatitude;
        this.retryNum = retryNum;
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
}
