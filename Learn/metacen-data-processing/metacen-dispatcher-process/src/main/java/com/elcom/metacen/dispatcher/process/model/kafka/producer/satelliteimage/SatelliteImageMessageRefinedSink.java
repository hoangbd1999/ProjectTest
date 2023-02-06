//package com.elcom.metacen.dispatcher.process.model.kafka.producer.satelliteimage;
//
//import java.io.Serializable;
//
///**
// *
// * @author Admin
// * metacen kafka topic: `SATELLITE_IMAGE_RAW_R`
// * Topic chứa dữ liệu bắn sang service xử lý ảnh vệ tinh Satellite Image Process
// */
//public class SatelliteImageMessageRefinedSink implements Serializable {
//
//    private String satelliteImageUuidKey;
//    private String originalFolderName; // Tên folder chứa data ảnh vệ tinh
//    private String rootDataFolderPath; // Full path của folder chứa data ảnh vệ tinh
//    private Double originLongitude; // xMin
//    private Double originLatitude;  // yMin
//    private Double cornerLongitude; // xMax
//    private Double cornerLatitude;  // yMax
//    private int retryNum; // Mặc định khi tạo mới là  = 0, có lỗi thì ++
//
//    public SatelliteImageMessageRefinedSink() {
//    }
//
//    public SatelliteImageMessageRefinedSink(String satelliteImageUuidKey, String originalFolderName, String rootDataFolderPath
//                                        , Double originLongitude, Double originLatitude, Double cornerLongitude, Double cornerLatitude, int retryNum) {
//        this.satelliteImageUuidKey = satelliteImageUuidKey;
//        this.originalFolderName = originalFolderName;
//        this.rootDataFolderPath = rootDataFolderPath;
//        this.originLongitude = originLongitude;
//        this.originLatitude = originLatitude;
//        this.cornerLongitude = cornerLongitude;
//        this.cornerLatitude = cornerLatitude;
//        this.retryNum = retryNum;
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
//     * @return the satelliteImageUuidKey
//     */
//    public String getSatelliteImageUuidKey() {
//        return satelliteImageUuidKey;
//    }
//
//    /**
//     * @param satelliteImageUuidKey the satelliteImageUuidKey to set
//     */
//    public void setSatelliteImageUuidKey(String satelliteImageUuidKey) {
//        this.satelliteImageUuidKey = satelliteImageUuidKey;
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
//
//    /**
//     * @return the rootDataFolderPath
//     */
//    public String getRootDataFolderPath() {
//        return rootDataFolderPath;
//    }
//
//    /**
//     * @param rootDataFolderPath the rootDataFolderPath to set
//     */
//    public void setRootDataFolderPath(String rootDataFolderPath) {
//        this.rootDataFolderPath = rootDataFolderPath;
//    }
//}
