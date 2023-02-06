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
@Table(name = "satellite_image_data")
@XmlRootElement
@NamedQueries({
@NamedQuery(name = "SatelliteImageData.findAll", query = "SELECT sid FROM SatelliteImageData sid")})
public class SatelliteImageData implements Serializable {

    @Id
    @Column(name = "uuidKey", length = 36, nullable = false)
    private String uuidKey;

    @Column(name = "satelliteName")
    private String satelliteName;

    @Column(name = "missionId")
    private String missionId;

    @Column(name = "productLevel")
    private String productLevel;

    @Column(name = "baseLineNumber")
    private String baseLineNumber;

    @Column(name = "relativeOrbitNumber")
    private String relativeOrbitNumber;

    @Column(name = "tileNumber")
    private String tileNumber;

    @Column(name = "originLongitude")
    private Float originLongitude;

    @Column(name = "originLatitude")
    private Float originLatitude;

    @Column(name = "cornerLongitude")
    private Float cornerLongitude;

    @Column(name = "cornerLatitude")
    private Float cornerLatitude;

    @Column(name = "rootDataFolderPath")
    private String rootDataFolderPath;

    @Column(name = "geoWmsUrl")
    private String geoWmsUrl;

    @Column(name = "geoWorkSpace")
    private String geoWorkSpace;

    @Column(name = "geoLayerName")
    private String geoLayerName;

    @Column(name = "captureTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date captureTime;

    @Column(name = "secondTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date secondTime;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "processStatus")
    private Integer processStatus;

    @Column(name = "dataVendor")
    private String dataVendor;
    
    public SatelliteImageData() {
    }

    public SatelliteImageData(String uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.enrich.data.model.SatelliteImageData[ uuidKey=" + uuidKey + " ]";
    }
}
