package com.elcom.metacen.dispatcher.process.model.kafka.consumer.satelliteimage;

//package com.elcom.metacen.dispatcher.process.model.kafka.consumer;
//
//import com.elcom.metacen.dto.redis.Job;
//import java.io.Serializable;
//
///**
// *
// * @author Admin
// * metacen kafka topic: `SATELLITE_IMAGE_RAW`
// * Topic chứa dữ liệu thô ảnh vệ tinh
// */
//public class SatelliteImageMessageSimplify extends Job implements Serializable {
//    
//    private String uuidKey;
//    private String originalFolderName; // Đường dẫn gốc của server cắt ảnh vệ tinh
//    private Double originLongitude; // xMin
//    private Double originLatitude;  // yMin
//    private Double cornerLongitude; // xMax
//    private Double cornerLatitude;  // yMax
//    private int retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++
//
//    public SatelliteImageMessageSimplify() {
//    }
//    
//    /**
//     * @return the uuidKey
//     */
//    public String getUuidKey() {
//        return uuidKey;
//    }
//
//    /**
//     * @param uuidKey the uuidKey to set
//     */
//    public void setUuidKey(String uuidKey) {
//        this.uuidKey = uuidKey;
//    }
//
//    /**
//     * @return the originalFolderName
//     */
//    public String getOriginalFolderName() {
//        return originalFolderName;
//    }
//
//    /**
//     * @param originalFolderName the originalFolderName to set
//     */
//    public void setOriginalFolderName(String originalFolderName) {
//        this.originalFolderName = originalFolderName;
//    }
//
//    /**
//     * @return the originLongitude
//     */
//    public Double getOriginLongitude() {
//        return originLongitude;
//    }
//
//    /**
//     * @param originLongitude the originLongitude to set
//     */
//    public void setOriginLongitude(Double originLongitude) {
//        this.originLongitude = originLongitude;
//    }
//
//    /**
//     * @return the originLatitude
//     */
//    public Double getOriginLatitude() {
//        return originLatitude;
//    }
//
//    /**
//     * @param originLatitude the originLatitude to set
//     */
//    public void setOriginLatitude(Double originLatitude) {
//        this.originLatitude = originLatitude;
//    }
//
//    /**
//     * @return the cornerLongitude
//     */
//    public Double getCornerLongitude() {
//        return cornerLongitude;
//    }
//
//    /**
//     * @param cornerLongitude the cornerLongitude to set
//     */
//    public void setCornerLongitude(Double cornerLongitude) {
//        this.cornerLongitude = cornerLongitude;
//    }
//
//    /**
//     * @return the cornerLatitude
//     */
//    public Double getCornerLatitude() {
//        return cornerLatitude;
//    }
//
//    /**
//     * @param cornerLatitude the cornerLatitude to set
//     */
//    public void setCornerLatitude(Double cornerLatitude) {
//        this.cornerLatitude = cornerLatitude;
//    }
//
//    /**
//     * @return the retryNum
//     */
//    public int getRetryNum() {
//        return retryNum;
//    }
//
//    /**
//     * @param retryNum the retryNum to set
//     */
//    public void setRetryNum(int retryNum) {
//        this.retryNum = retryNum;
//    }
//}
