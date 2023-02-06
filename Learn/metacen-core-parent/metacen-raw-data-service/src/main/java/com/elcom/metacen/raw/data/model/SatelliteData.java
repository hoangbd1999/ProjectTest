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
@Table(name = "satellite_data")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "SatelliteData.findAll", query = "SELECT sd FROM SatelliteData sd")})
public class SatelliteData implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private UUID uuidKey;

    @Column(name = "satelliteName")
    private String satelliteName;

    @Column(name = "imageFilePath")
    private String imageFilePath;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "coordinates")
    private String coordinates;

    @Column(name = "dataVendor")
    private String dataVendor;

    @Column(name = "processStatus")
    private Integer processStatus;

    @Column(name = "eventTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    public SatelliteData() {
    }

    public SatelliteData(UUID uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.raw.data.model.SatelliteData[ uuidKey=" + uuidKey + " ]";
    }
}
