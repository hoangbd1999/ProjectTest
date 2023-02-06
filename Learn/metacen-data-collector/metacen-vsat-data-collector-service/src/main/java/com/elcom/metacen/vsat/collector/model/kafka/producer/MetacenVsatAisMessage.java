package com.elcom.metacen.vsat.collector.model.kafka.producer;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author anhdv
 * metacen kafka topic: `VSAT_AIS_RAW`
 * Nguồn VSAT
 */
// public class MetacenVsatAisMessage extends Job implements Serializable {
public class MetacenVsatAisMessage implements Serializable {
    
    private String uuidKey;
    private String mmsi;
    private String name;
    private String callSign;
    private String imo;
    private Integer countryId;
    private Integer typeId;
    private Integer dimA;
    private Integer dimB;
    private Integer dimC;
    private Integer dimD;
    private Float draught;
    private Float rot;
    private Float sog;
    private Float cog;
    private Float longitude;
    private Float latitude;
    private Integer navstatus;
    private Integer trueHanding;
    private String eta;
    private String destination;
    private Long eventTime;
    private String mmsiMaster;
    private Integer sourcePort;
    private Integer destPort;
    private String sourceIp;
    private String destIp;
    private Integer direction;
    private Long dataSourceId;
    private String dataSourceName;
    private String timeKey;

    public MetacenVsatAisMessage() {
    }

    public MetacenVsatAisMessage(String mmsi, String imo, Integer countryId, Integer typeId, Integer dimA, Integer dimB, Integer dimC, Integer dimD
            , Float draught, Float rot, Float sog, Float cog, Float longitude, Float latitude, Integer navstatus, Integer trueHanding
            , String callSign, String name, String eta, String destination, Long eventTime, String mmsiMaster
            , Integer sourcePort, Integer destPort, String sourceIp, String destIp
            , Integer direction, Long dataSourceId, String dataSourceName, String timeKey) {
        this.uuidKey = UUID.randomUUID().toString();
        this.mmsi = mmsi;
        this.imo = imo;
        this.countryId = countryId;
        this.typeId = typeId;
        this.dimA = dimA;
        this.dimB = dimB;
        this.dimC = dimC;
        this.dimD = dimD;
        this.draught = draught;
        this.rot = rot;
        this.sog = sog;
        this.cog = cog;
        this.longitude = longitude;
        this.latitude = latitude;
        this.navstatus = navstatus;
        this.trueHanding = trueHanding;
        this.callSign = callSign;
        this.name = name;
        this.eta = eta;
        this.destination = destination;
        this.eventTime = eventTime;
        this.mmsiMaster = mmsiMaster;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.direction = direction;
        this.dataSourceId = dataSourceId;
        this.dataSourceName = dataSourceName;
        this.timeKey = timeKey;
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
     * @return the typeId
     */
    public Integer getTypeId() {
        return typeId;
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    /**
     * @return the dimA
     */
    public Integer getDimA() {
        return dimA;
    }

    /**
     * @param dimA the dimA to set
     */
    public void setDimA(Integer dimA) {
        this.dimA = dimA;
    }

    /**
     * @return the dimB
     */
    public Integer getDimB() {
        return dimB;
    }

    /**
     * @param dimB the dimB to set
     */
    public void setDimB(Integer dimB) {
        this.dimB = dimB;
    }

    /**
     * @return the dimC
     */
    public Integer getDimC() {
        return dimC;
    }

    /**
     * @param dimC the dimC to set
     */
    public void setDimC(Integer dimC) {
        this.dimC = dimC;
    }

    /**
     * @return the dimD
     */
    public Integer getDimD() {
        return dimD;
    }

    /**
     * @param dimD the dimD to set
     */
    public void setDimD(Integer dimD) {
        this.dimD = dimD;
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
     * @return the navstatus
     */
    public Integer getNavstatus() {
        return navstatus;
    }

    /**
     * @param navstatus the navstatus to set
     */
    public void setNavstatus(Integer navstatus) {
        this.navstatus = navstatus;
    }

    /**
     * @return the trueHanding
     */
    public Integer getTrueHanding() {
        return trueHanding;
    }

    /**
     * @param trueHanding the trueHanding to set
     */
    public void setTrueHanding(Integer trueHanding) {
        this.trueHanding = trueHanding;
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
     * @return the mmsiMaster
     */
    public String getMmsiMaster() {
        return mmsiMaster;
    }

    /**
     * @param mmsiMaster the mmsiMaster to set
     */
    public void setMmsiMaster(String mmsiMaster) {
        this.mmsiMaster = mmsiMaster;
    }

    /**
     * @return the sourcePort
     */
    public Integer getSourcePort() {
        return sourcePort;
    }

    /**
     * @param sourcePort the sourcePort to set
     */
    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    /**
     * @return the destPort
     */
    public Integer getDestPort() {
        return destPort;
    }

    /**
     * @param destPort the destPort to set
     */
    public void setDestPort(Integer destPort) {
        this.destPort = destPort;
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
     * @return the direction
     */
    public Integer getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    /**
     * @return the dataSourceName
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * @param dataSourceName the dataSourceName to set
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
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
}
