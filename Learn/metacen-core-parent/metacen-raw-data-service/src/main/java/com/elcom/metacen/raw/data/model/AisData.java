/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.raw.data.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author Admin
 */
@Entity
@Getter
@Setter
@Table(name = "ais_data")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AisData.findAll", query = "SELECT ad FROM AisData ad")})
public class AisData implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private UUID uuidKey;

    @Column(name = "mmsi")
    private BigInteger mmsi;

    @Column(name = "imo")
    private String imo;

    @Column(name = "callSign")
    private String callSign;

    @Column(name = "name")
    private String name;

    @Column(name = "shipType")
    private String shipType;

    @Column(name = "countryId")
    private Integer countryId;

    @Column(name = "rot")
    private Float rot;

    @Column(name = "sog")
    private Float sog;

    @Column(name = "cog")
    private Float cog;

    @Column(name = "draught")
    private Float draught;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "messageType")
    private String messageType;

    @Column(name = "eta")
    private String eta;

    @Column(name = "destination")
    private String destination;

    @Column(name = "second")
    private Integer second;

    @Column(name = "communicationStateSyncState")
    private String communicationStateSyncState;

    @Column(name = "specialManeuverIndicator")
    private String specialManeuverIndicator;

    @Column(name = "toStern")
    private Integer toStern;

    @Column(name = "toPort")
    private Integer toPort;

    @Column(name = "dataTerminalReady")
    private Boolean dataTerminalReady;

    @Column(name = "positionFixingDevice")
    private String positionFixingDevice;

    @Column(name = "toStarboard")
    private Integer toStarboard;

    @Column(name = "toBow")
    private Integer toBow;

    @Column(name = "rateOfTurn")
    private Integer rateOfTurn;

    @Column(name = "repeatIndicator")
    private Integer repeatIndicator;

    @Column(name = "transponderClass")
    private String transponderClass;

    @Column(name = "navigationStatus")
    private String navigationStatus;

    @Column(name = "positionAccuracy")
    private Boolean positionAccuracy;

    @Column(name = "trueHanding")
    private Integer trueHanding;

    @Column(name = "raimFlag")
    private Boolean raimFlag;

    @Column(name = "valid")
    private Boolean valid;

    @Column(name = "processStatus")
    private Integer processStatus;

    @Column(name = "dataVendor")
    private String dataVendor;

    @Column(name = "timeKey")
    private BigInteger timeKey;

    @Column(name = "eventTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    public AisData() {
    }

    public AisData(UUID uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.raw.data.model.AisData[ uuidKey=" + uuidKey + " ]";
    }
}
