package com.elcom.metacen.enrich.data.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Admin
 */
@Entity
@Getter
@Setter
@Table(name = "satellite_image_changes")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "SatelliteImageChanges.findAll", query = "SELECT sica FROM SatelliteImageChanges sica")})
public class SatelliteImageChanges implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private String uuidKey;

    @Column(name = "tileNumber")
    private String tileNumber;

    @Column(name = "timeFileOrigin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeFileOrigin;

    @Column(name = "timeFileCompare")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeFileCompare;

    @Column(name = "imagePathFileOrigin")
    private String imagePathFileOrigin;

    @Column(name = "imagePathFileCompare")
    private String imagePathFileCompare;

    @Column(name = "timeReceiveResult")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeReceiveResult;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "processStatus")
    private int processStatus;

    @Column(name = "retryTimes")
    private int retryTimes;

    @Column(name = "isDeleted")
    private int isDeleted;

    public SatelliteImageChanges() {
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.enrich.data.model.SatelliteImageChange[ uuidKey=" + uuidKey + " ]";
    }
}
