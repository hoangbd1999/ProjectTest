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
@Table(name = "satellite_image_changes_result")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "SatelliteImageChangesResult.findAll", query = "SELECT sicr FROM SatelliteImageChangesResult sicr")})
public class SatelliteImageChangesResult implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private String uuidKey;

    @Column(name = "satelliteImageChangesUuidKey")
    private String satelliteImageChangesUuidKey;

    @Column(name = "originLatitude")
    private BigDecimal originLatitude;

    @Column(name = "originLongitude")
    private BigDecimal originLongitude;

    @Column(name = "cornerLatitude")
    private BigDecimal cornerLatitude;

    @Column(name = "cornerLongitude")
    private BigDecimal cornerLongitude;

    @Column(name = "width")
    private Long width;

    @Column(name = "height")
    private Long height;

    @Column(name = "imageFilePathOrigin")
    private String imageFilePathOrigin;

    @Column(name = "imageFilePathCompare")
    private String imageFilePathCompare;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    public SatelliteImageChangesResult() {
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.enrich.data.model.SatelliteImageChangesResult[ uuidKey=" + uuidKey + " ]";
    }
}
