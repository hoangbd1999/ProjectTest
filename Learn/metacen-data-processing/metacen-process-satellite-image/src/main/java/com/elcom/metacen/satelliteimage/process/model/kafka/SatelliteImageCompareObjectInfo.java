package com.elcom.metacen.satelliteimage.process.model.kafka;

import java.io.Serializable;

/**
 *
 * @author Admin
 * Use this model for publish to kafka topic `SATELLITE_IMAGE_CHANGES_RESULT`
 */
public class SatelliteImageCompareObjectInfo implements Serializable {
    
    private String satelliteImageChangesUuidKey;
    private Float originLongitude;
    private Float originLatitude;
    private Float cornerLongitude;
    private Float cornerLatitude;
    private Integer width;
    private Integer height;
    private String imageFilePathOrigin;
    private String imageFilePathCompare;

    public SatelliteImageCompareObjectInfo() {
    }

    public SatelliteImageCompareObjectInfo(String satelliteImageChangesUuidKey, Float originLongitude, Float originLatitude, Float cornerLongitude, Float cornerLatitude
                                        , Integer width, Integer height, String imageFilePathOrigin, String imageFilePathCompare) {
        this.satelliteImageChangesUuidKey = satelliteImageChangesUuidKey;
        this.originLongitude = originLongitude;
        this.originLatitude = originLatitude;
        this.cornerLongitude = cornerLongitude;
        this.cornerLatitude = cornerLatitude;
        this.width = width;
        this.height = height;
        this.imageFilePathOrigin = imageFilePathOrigin;
        this.imageFilePathCompare = imageFilePathCompare;
    }
    
    /**
     * @return the satelliteImageChangesUuidKey
     */
    public String getSatelliteImageChangesUuidKey() {
        return satelliteImageChangesUuidKey;
    }

    /**
     * @param satelliteImageChangesUuidKey the satelliteImageChangesUuidKey to set
     */
    public void setSatelliteImageChangesUuidKey(String satelliteImageChangesUuidKey) {
        this.satelliteImageChangesUuidKey = satelliteImageChangesUuidKey;
    }

    /**
     * @return the originLongitude
     */
    public Float getOriginLongitude() {
        return originLongitude;
    }

    /**
     * @param originLongitude the originLongitude to set
     */
    public void setOriginLongitude(Float originLongitude) {
        this.originLongitude = originLongitude;
    }

    /**
     * @return the originLatitude
     */
    public Float getOriginLatitude() {
        return originLatitude;
    }

    /**
     * @param originLatitude the originLatitude to set
     */
    public void setOriginLatitude(Float originLatitude) {
        this.originLatitude = originLatitude;
    }

    /**
     * @return the cornerLongitude
     */
    public Float getCornerLongitude() {
        return cornerLongitude;
    }

    /**
     * @param cornerLongitude the cornerLongitude to set
     */
    public void setCornerLongitude(Float cornerLongitude) {
        this.cornerLongitude = cornerLongitude;
    }

    /**
     * @return the cornerLatitude
     */
    public Float getCornerLatitude() {
        return cornerLatitude;
    }

    /**
     * @param cornerLatitude the cornerLatitude to set
     */
    public void setCornerLatitude(Float cornerLatitude) {
        this.cornerLatitude = cornerLatitude;
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
     * @return the imageFilePathOrigin
     */
    public String getImageFilePathOrigin() {
        return imageFilePathOrigin;
    }

    /**
     * @param imageFilePathOrigin the imageFilePathOrigin to set
     */
    public void setImageFilePathOrigin(String imageFilePathOrigin) {
        this.imageFilePathOrigin = imageFilePathOrigin;
    }

    /**
     * @return the imageFilePathCompare
     */
    public String getImageFilePathCompare() {
        return imageFilePathCompare;
    }

    /**
     * @param imageFilePathCompare the imageFilePathCompare to set
     */
    public void setImageFilePathCompare(String imageFilePathCompare) {
        this.imageFilePathCompare = imageFilePathCompare;
    }
}
