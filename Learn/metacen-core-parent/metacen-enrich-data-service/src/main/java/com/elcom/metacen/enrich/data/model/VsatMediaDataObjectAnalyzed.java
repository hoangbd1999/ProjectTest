/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.model;

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
@Table(name = "vsat_media_data_object_analyzed")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VsatMediaDataObjectAnalyzed.findAll", query = "SELECT vmdoa FROM VsatMediaDataObjectAnalyzed vmdoa")})
public class VsatMediaDataObjectAnalyzed implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private String uuidKey;

    @Column(name = "vsatMediaDataAnalyzedUuidKey")
    private String vsatMediaDataAnalyzedUuidKey;

    @Column(name = "objectId")
    private String objectId;

    @Column(name = "objectMmsi")
    private String objectMmsi;

    @Column(name = "objectUuid")
    private String objectUuid;

    @Column(name = "objectType")
    private String objectType;

    @Column(name = "objectName")
    private String objectName;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "isDeleted")
    private int isDeleted;

    public VsatMediaDataObjectAnalyzed() {
    }

    public VsatMediaDataObjectAnalyzed(String uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.enrich.data.model.VsatMediaDataObjectAnalyzed[ uuidKey=" + uuidKey + " ]";
    }
}
