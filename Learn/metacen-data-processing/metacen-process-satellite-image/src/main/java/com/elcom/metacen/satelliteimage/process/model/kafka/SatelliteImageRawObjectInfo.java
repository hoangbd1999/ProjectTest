package com.elcom.metacen.satelliteimage.process.model.kafka;

import java.io.Serializable;

/**
 *
 * @author Admin
 * Use this model for publish to kafka topic `SATELLITE_IMAGE_FINAL`
 */
public class SatelliteImageRawObjectInfo implements Serializable {
    
    private String satelliteImageUuidKey;
    private Integer width;
    private Integer height;
    private Float latitude;
    private Float longitude;
    private String color;
    private String imageFilePath;
    private String analyzedEngine;

    public SatelliteImageRawObjectInfo(){
    }

    public SatelliteImageRawObjectInfo(String satelliteImageUuidKey, Integer width, Integer height, Float latitude, Float longitude, String color, String imageFilePath, String analyzedEngine) {
        this.satelliteImageUuidKey = satelliteImageUuidKey;
        this.width = width;
        this.height = height;
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
        this.imageFilePath = imageFilePath;
        this.analyzedEngine = analyzedEngine;
    }
    
    /**
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * @return the latitude
     */
    public Float getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public Float getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the satelliteImageUuidKey
     */
    public String getSatelliteImageUuidKey() {
        return satelliteImageUuidKey;
    }

    /**
     * @param satelliteImageUuidKey the satelliteImageUuidKey to set
     */
    public void setSatelliteImageUuidKey(String satelliteImageUuidKey) {
        this.satelliteImageUuidKey = satelliteImageUuidKey;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the imageFilePath
     */
    public String getImageFilePath() {
        return imageFilePath;
    }

    /**
     * @param imageFilePath the imageFilePath to set
     */
    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    /**
     * @return the analyzedEngine
     */
    public String getAnalyzedEngine() {
        return analyzedEngine;
    }

    /**
     * @param analyzedEngine the analyzedEngine to set
     */
    public void setAnalyzedEngine(String analyzedEngine) {
        this.analyzedEngine = analyzedEngine;
    }
}
