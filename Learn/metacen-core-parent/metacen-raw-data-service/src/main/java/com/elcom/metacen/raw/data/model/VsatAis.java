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
@Table(name = "vsat_ais")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VsatAis.findAll", query = "SELECT t FROM VsatAis t")})
public class VsatAis implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private UUID uuidKey;

    @Column(name = "mmsi")
    private BigInteger mmsi;

    @Column(name = "imo")
    private String imo;

    @Column(name = "name")
    private String name;

    @Column(name = "callSign")
    private String callSign;

    @Column(name = "countryId")
    private Integer countryId;

    @Column(name = "typeId")
    private Integer typeId;

    @Column(name = "dimA")
    private Integer dimA;

    @Column(name = "dimB")
    private Integer dimB;

    @Column(name = "dimC")
    private Integer dimC;

    @Column(name = "dimD")
    private Integer dimD;

    @Column(name = "draught")
    private Float draught;

    @Column(name = "rot")
    private Float rot;

    @Column(name = "sog")
    private Float sog;

    @Column(name = "cog")
    private Float cog;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "navStatus")
    private Long navStatus;

    @Column(name = "trueHanding")
    private Long trueHanding;

    @Column(name = "eta")
    private String eta;

    @Column(name = "destination")
    private String destination;

    @Column(name = "mmsiMaster")
    private BigInteger mmsiMaster;

    @Column(name = "sourcePort")
    private Long sourcePort;

    @Column(name = "sourceIp")
    private String sourceIp;

    @Column(name = "destPort")
    private Long destPort;

    @Column(name = "destIp")
    private String destIp;

    @Column(name = "direction")
    private Integer direction;

    @Column(name = "processStatus")
    private Integer processStatus;

    @Column(name = "dataSourceId")
    private Long dataSourceId;

    @Column(name = "dataSourceName")
    private String dataSourceName;

    @Column(name = "timeKey")
    private BigInteger timeKey;

    @Column(name = "dataVendor")
    private String dataVendor;

    @Column(name = "eventTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    public VsatAis() {
    }

    public VsatAis(UUID uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.raw.data.model.VsatAis[ uuidKey=" + uuidKey + " ]";
    }
}
