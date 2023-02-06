/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

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
@Table(name = "satellite_image_object_analyzed")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "SatelliteImageObjectAnalyzed.findAll", query = "SELECT sa FROM SatelliteImageObjectAnalyzed sa")})
public class SatelliteImageObjectAnalyzed implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private String uuidKey;

    @Column(name = "satelliteImageUuidKey")
    private String satelliteImageUuidKey;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "color")
    private String color;

    @Column(name = "imageFilePath")
    private String imageFilePath;

    @Column(name = "analyzedEngine")
    private String analyzedEngine;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "isDeleted")
    private int isDeleted;

    public SatelliteImageObjectAnalyzed() {
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.enrich.data.model.SatelliteImageObjectAnalyzed[ uuidKey=" + uuidKey + " ]";
    }
}
