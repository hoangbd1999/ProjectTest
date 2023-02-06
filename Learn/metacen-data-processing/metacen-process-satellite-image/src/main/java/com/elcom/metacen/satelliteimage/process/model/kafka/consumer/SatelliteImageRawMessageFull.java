package com.elcom.metacen.satelliteimage.process.model.kafka.consumer;

import com.elcom.metacen.dto.redis.Job;
import java.io.Serializable;

/**
 *
 * @author Admin
 * metacen kafka topic: `SATELLITE_IMAGE_RAW_R`
 * Topic chứa dữ liệu ảnh vệ tinh cần xử lý, từ bên Dispatcher đẩy sang
 */
public class SatelliteImageRawMessageFull extends Job implements Serializable {
    
    private String uuidKey;
    private String satelliteName;
    private String missionId;
    private String productLevel;
    private Long captureTime;
    private String baseLineNumber;
    private String relativeOrbitNumber;
    private String tileNumber;
    private Long secondTime;
    //private String originalFolderName; // Tên folder chứa data ảnh vệ tinh
    private String rootDataFolderPath; // Full path của folder chứa data ảnh vệ tinh
    private Double originLongitude; // xMin
    private Double originLatitude;  // yMin
    private Double cornerLongitude; // xMax
    private Double cornerLatitude;  // yMax
    private String geoWmsUrl;
    private String geoWorkSpace;
    private String geoLayerName;
    private String dataVendor;
    private Integer processStatus;
    private Integer retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++

    public SatelliteImageRawMessageFull() {
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
     * @return the satelliteName
     */
    public String getSatelliteName() {
        return satelliteName;
    }

    /**
     * @param satelliteName the satelliteName to set
     */
    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    /**
     * @return the missionId
     */
    public String getMissionId() {
        return missionId;
    }

    /**
     * @param missionId the missionId to set
     */
    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    /**
     * @return the productLevel
     */
    public String getProductLevel() {
        return productLevel;
    }

    /**
     * @param productLevel the productLevel to set
     */
    public void setProductLevel(String productLevel) {
        this.productLevel = productLevel;
    }

    /**
     * @return the captureTime
     */
    public Long getCaptureTime() {
        return captureTime;
    }

    /**
     * @param captureTime the captureTime to set
     */
    public void setCaptureTime(Long captureTime) {
        this.captureTime = captureTime;
    }

    /**
     * @return the baseLineNumber
     */
    public String getBaseLineNumber() {
        return baseLineNumber;
    }

    /**
     * @param baseLineNumber the baseLineNumber to set
     */
    public void setBaseLineNumber(String baseLineNumber) {
        this.baseLineNumber = baseLineNumber;
    }

    /**
     * @return the relativeOrbitNumber
     */
    public String getRelativeOrbitNumber() {
        return relativeOrbitNumber;
    }

    /**
     * @param relativeOrbitNumber the relativeOrbitNumber to set
     */
    public void setRelativeOrbitNumber(String relativeOrbitNumber) {
        this.relativeOrbitNumber = relativeOrbitNumber;
    }

    /**
     * @return the tileNumber
     */
    public String getTileNumber() {
        return tileNumber;
    }

    /**
     * @param tileNumber the tileNumber to set
     */
    public void setTileNumber(String tileNumber) {
        this.tileNumber = tileNumber;
    }

    /**
     * @return the secondTime
     */
    public Long getSecondTime() {
        return secondTime;
    }

    /**
     * @param secondTime the secondTime to set
     */
    public void setSecondTime(Long secondTime) {
        this.secondTime = secondTime;
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
     * @return the geoWmsUrl
     */
    public String getGeoWmsUrl() {
        return geoWmsUrl;
    }

    /**
     * @param geoWmsUrl the geoWmsUrl to set
     */
    public void setGeoWmsUrl(String geoWmsUrl) {
        this.geoWmsUrl = geoWmsUrl;
    }

    /**
     * @return the geoWorkSpace
     */
    public String getGeoWorkSpace() {
        return geoWorkSpace;
    }

    /**
     * @param geoWorkSpace the geoWorkSpace to set
     */
    public void setGeoWorkSpace(String geoWorkSpace) {
        this.geoWorkSpace = geoWorkSpace;
    }

    /**
     * @return the geoLayerName
     */
    public String getGeoLayerName() {
        return geoLayerName;
    }

    /**
     * @param geoLayerName the geoLayerName to set
     */
    public void setGeoLayerName(String geoLayerName) {
        this.geoLayerName = geoLayerName;
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

    /**
     * @return the processStatus
     */
    public Integer getProcessStatus() {
        return processStatus;
    }

    /**
     * @param processStatus the processStatus to set
     */
    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }
}
