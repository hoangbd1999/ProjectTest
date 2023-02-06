package com.elcom.metacen.vsat.collector.model.kafka.producer;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author anhdv
 * metacen kafka topic: `AIS_RAW`
 * Nguồn AIS
 */
// public class MetacenAisMessage extends Job implements Serializable {
public class MetacenAisMessage implements Serializable {
    
    private String uuidKey;
    private String mmsi;
    private String imo;
    private String callSign;
    private String name;
    private String shipType; // Lấy từ việc parse AIS MESSAGE ( !AIVDM.................. )
    private Integer countryId;
    private Float rot;
    private Float sog;
    private Float cog;
    private Float draught;
    private Float longitude;
    private Float latitude;
    private String messageType;
    private String eta;
    private String destination;
    private Integer second;
    private String communicationStateSyncState;
    private String specialManeuverIndicator;
    private Integer toStern;
    private Integer toPort;
    private boolean dataTerminalReady;
    private String positionFixingDevice;
    private Integer toStarboard;
    private Integer toBow;
    private Integer rateOfTurn;
    private Integer repeatIndicator;
    private String transponderClass;
    private Integer navigationStatus;
    private Integer trueHeading;
    private boolean positionAccuracy;
    private boolean raimFlag;
    private boolean valid;
    private Long eventTime;
    private String timeKey;

    public MetacenAisMessage() {
    }

    public MetacenAisMessage(String mmsi, String imo, String callSign, String name, String shipType, Integer countryId
                        , Float rot, Float sog, Float cog, Float draught, Float longitude, Float latitude, String messageType, String eta, String destination
                        , Integer second, String communicationStateSyncState, String specialManeuverIndicator, Integer toStern, Integer toPort
                        , boolean dataTerminalReady, String positionFixingDevice, Integer toStarboard, Integer toBow, Integer rateOfTurn
                        , Integer repeatIndicator, String transponderClass, Integer navigationStatus, Integer trueHeading
                        , boolean positionAccuracy, boolean raimFlag, boolean valid, Long eventTime, String timeKey) {
        this.uuidKey = UUID.randomUUID().toString();
        this.mmsi = mmsi;
        this.imo = imo;
        this.callSign = callSign;
        this.name = name;
        this.shipType = shipType;
        this.countryId = countryId;
        this.rot = rot;
        this.sog = sog;
        this.cog = cog;
        this.draught = draught;
        this.longitude = longitude;
        this.latitude = latitude;
        this.messageType = messageType;
        this.eta = eta;
        this.destination = destination;
        this.second = second;
        this.communicationStateSyncState = communicationStateSyncState;
        this.specialManeuverIndicator = specialManeuverIndicator;
        this.toStern = toStern;
        this.toPort = toPort;
        this.dataTerminalReady = dataTerminalReady;
        this.positionFixingDevice = positionFixingDevice;
        this.toStarboard = toStarboard;
        this.toBow = toBow;
        this.rateOfTurn = rateOfTurn;
        this.repeatIndicator = repeatIndicator;
        this.transponderClass = transponderClass;
        this.navigationStatus = navigationStatus;
        this.trueHeading = trueHeading;
        this.positionAccuracy = positionAccuracy;
        this.raimFlag = raimFlag;
        this.valid = valid;
        this.eventTime = eventTime;
        this.timeKey = timeKey;
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
     * @return the mmsi
     */
    public String getMmsi() {
        return mmsi;
    }

    /**
     * @param mmsi the mmsi to set
     */
    public void setMmsi(String mmsi) {
        this.mmsi = mmsi;
    }

    /**
     * @return the imo
     */
    public String getImo() {
        return imo;
    }

    /**
     * @param imo the imo to set
     */
    public void setImo(String imo) {
        this.imo = imo;
    }

    /**
     * @return the callSign
     */
    public String getCallSign() {
        return callSign;
    }

    /**
     * @param callSign the callSign to set
     */
    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }
    
    /**
     * @return the shipType
     */
    public String getShipType() {
        return shipType;
    }

    /**
     * @param shipType the shipType to set
     */
    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    /**
     * @return the countryId
     */
    public Integer getCountryId() {
        return countryId;
    }

    /**
     * @param countryId the countryId to set
     */
    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    /**
     * @return the rot
     */
    public Float getRot() {
        return rot;
    }

    /**
     * @param rot the rot to set
     */
    public void setRot(Float rot) {
        this.rot = rot;
    }

    /**
     * @return the sog
     */
    public Float getSog() {
        return sog;
    }

    /**
     * @param sog the sog to set
     */
    public void setSog(Float sog) {
        this.sog = sog;
    }

    /**
     * @return the cog
     */
    public Float getCog() {
        return cog;
    }

    /**
     * @param cog the cog to set
     */
    public void setCog(Float cog) {
        this.cog = cog;
    }

    /**
     * @return the draught
     */
    public Float getDraught() {
        return draught;
    }

    /**
     * @param draught the draught to set
     */
    public void setDraught(Float draught) {
        this.draught = draught;
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
     * @return the messageType
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * @param messageType the messageType to set
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the eta
     */
    public String getEta() {
        return eta;
    }

    /**
     * @param eta the eta to set
     */
    public void setEta(String eta) {
        this.eta = eta;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return the second
     */
    public Integer getSecond() {
        return second;
    }

    /**
     * @param second the second to set
     */
    public void setSecond(Integer second) {
        this.second = second;
    }

    /**
     * @return the communicationStateSyncState
     */
    public String getCommunicationStateSyncState() {
        return communicationStateSyncState;
    }

    /**
     * @param communicationStateSyncState the communicationStateSyncState to set
     */
    public void setCommunicationStateSyncState(String communicationStateSyncState) {
        this.communicationStateSyncState = communicationStateSyncState;
    }

    /**
     * @return the specialManeuverIndicator
     */
    public String getSpecialManeuverIndicator() {
        return specialManeuverIndicator;
    }

    /**
     * @param specialManeuverIndicator the specialManeuverIndicator to set
     */
    public void setSpecialManeuverIndicator(String specialManeuverIndicator) {
        this.specialManeuverIndicator = specialManeuverIndicator;
    }

    /**
     * @return the toStern
     */
    public Integer getToStern() {
        return toStern;
    }

    /**
     * @param toStern the toStern to set
     */
    public void setToStern(Integer toStern) {
        this.toStern = toStern;
    }

    /**
     * @return the toPort
     */
    public Integer getToPort() {
        return toPort;
    }

    /**
     * @param toPort the toPort to set
     */
    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * @return the dataTerminalReady
     */
    public boolean isDataTerminalReady() {
        return dataTerminalReady;
    }

    /**
     * @param dataTerminalReady the dataTerminalReady to set
     */
    public void setDataTerminalReady(boolean dataTerminalReady) {
        this.dataTerminalReady = dataTerminalReady;
    }

    /**
     * @return the positionFixingDevice
     */
    public String getPositionFixingDevice() {
        return positionFixingDevice;
    }

    /**
     * @param positionFixingDevice the positionFixingDevice to set
     */
    public void setPositionFixingDevice(String positionFixingDevice) {
        this.positionFixingDevice = positionFixingDevice;
    }

    /**
     * @return the toStarboard
     */
    public Integer getToStarboard() {
        return toStarboard;
    }

    /**
     * @param toStarboard the toStarboard to set
     */
    public void setToStarboard(Integer toStarboard) {
        this.toStarboard = toStarboard;
    }

    /**
     * @return the toBow
     */
    public Integer getToBow() {
        return toBow;
    }

    /**
     * @param toBow the toBow to set
     */
    public void setToBow(Integer toBow) {
        this.toBow = toBow;
    }

    /**
     * @return the rateOfTurn
     */
    public Integer getRateOfTurn() {
        return rateOfTurn;
    }

    /**
     * @param rateOfTurn the rateOfTurn to set
     */
    public void setRateOfTurn(Integer rateOfTurn) {
        this.rateOfTurn = rateOfTurn;
    }

    /**
     * @return the repeatIndicator
     */
    public Integer getRepeatIndicator() {
        return repeatIndicator;
    }

    /**
     * @param repeatIndicator the repeatIndicator to set
     */
    public void setRepeatIndicator(Integer repeatIndicator) {
        this.repeatIndicator = repeatIndicator;
    }

    /**
     * @return the transponderClass
     */
    public String getTransponderClass() {
        return transponderClass;
    }

    /**
     * @param transponderClass the transponderClass to set
     */
    public void setTransponderClass(String transponderClass) {
        this.transponderClass = transponderClass;
    }

    /**
     * @return the navigationStatus
     */
    public Integer getNavigationStatus() {
        return navigationStatus;
    }

    /**
     * @param navigationStatus the navigationStatus to set
     */
    public void setNavigationStatus(Integer navigationStatus) {
        this.navigationStatus = navigationStatus;
    }

    /**
     * @return the trueHeading
     */
    public Integer getTrueHeading() {
        return trueHeading;
    }

    /**
     * @param trueHeading the trueHeading to set
     */
    public void setTrueHeading(Integer trueHeading) {
        this.trueHeading = trueHeading;
    }

    /**
     * @return the positionAccuracy
     */
    public boolean isPositionAccuracy() {
        return positionAccuracy;
    }

    /**
     * @param positionAccuracy the positionAccuracy to set
     */
    public void setPositionAccuracy(boolean positionAccuracy) {
        this.positionAccuracy = positionAccuracy;
    }

    /**
     * @return the raimFlag
     */
    public boolean isRaimFlag() {
        return raimFlag;
    }

    /**
     * @param raimFlag the raimFlag to set
     */
    public void setRaimFlag(boolean raimFlag) {
        this.raimFlag = raimFlag;
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @param valid the valid to set
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return the eventTime
     */
    public Long getEventTime() {
        return eventTime;
    }

    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the timeKey
     */
    public String getTimeKey() {
        return timeKey;
    }

    /**
     * @param timeKey the timeKey to set
     */
    public void setTimeKey(String timeKey) {
        this.timeKey = timeKey;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}