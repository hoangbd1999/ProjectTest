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
@Table(name = "vsat_media")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VsatMedia.findAll", query = "SELECT vm FROM VsatMedia vm")})
public class VsatMedia implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private UUID uuidKey;

    @Column(name = "mediaTypeId")
    private Long mediaTypeId;

    @Column(name = "mediaTypeName")
    private String mediaTypeName;

    @Column(name = "sourceId")
    private BigInteger sourceId;

    @Column(name = "sourceName")
    private String sourceName;

    @Column(name = "sourceIp")
    private String sourceIp;

    @Column(name = "sourcePort")
    private Long sourcePort;

    @Column(name = "sourcePhone")
    private String sourcePhone;

    @Column(name = "destId")
    private BigInteger destId;

    @Column(name = "destName")
    private String destName;

    @Column(name = "destIp")
    private String destIp;

    @Column(name = "destPort")
    private Long destPort;

    @Column(name = "destPhone")
    private String destPhone;

    @Column(name = "filePath")
    private String filePath;

    @Column(name = "fileName")
    private String fileName;

    @Column(name = "fileType")
    private String fileType;

    @Column(name = "fileSize")
    private BigInteger fileSize;

    @Column(name = "dataSourceId")
    private Long dataSourceId;

    @Column(name = "dataSourceName")
    private String dataSourceName;

    @Column(name = "direction")
    private Integer direction;

    @Column(name = "partName")
    private Long partName;

    @Column(name = "eventTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "processStatus")
    private Integer processStatus;

    @Column(name = "dataVendor")
    private String dataVendor;

    public VsatMedia() {
    }

    public VsatMedia(UUID uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.raw.data.model.VsatMedia[ uuidKey=" + uuidKey + " ]";
    }
}
