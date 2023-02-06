package com.elcom.metacen.raw.data.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "vsat_media_relation")
public class VsatMediaRelation implements Serializable {

    @Id
    @Column(name = "uuidKey")
    private String uuidKey;

    @Column(name = "uuidKeyFrom")
    private String uuidKeyFrom;

    @Column(name = "eventTimeFrom")
    private Timestamp eventTimeFrom;

    @Column(name = "mediaTypeIdFrom")
    private Integer mediaTypeIdFrom;

    @Column(name = "mediaTypeNameFrom")
    private String mediaTypeNameFrom;

    @Column(name = "directionFrom")
    private Integer directionFrom;

    @Column(name = "fileSizeFrom")
    private BigDecimal fileSizeFrom;

    @Column(name = "fileTypeFrom")
    private String fileTypeFrom;

    @Column(name = "dataSourceFrom")
    private Long dataSourceFrom;

    @Column(name = "sourceIdFrom")
    private Long sourceIdFrom;

    @Column(name = "destIdFrom")
    private Long destIdFrom;

    @Column(name = "sourceIpFrom")
    private String sourceIpFrom;

    @Column(name = "destIpFrom")
    private String destIpFrom;

    @Column(name = "partNameFrom")
    private Long partNameFrom;

    @Column(name = "uuidKeyTo")
    private String uuidKeyTo;

    @Column(name = "eventTimeTo")
    private Timestamp eventTimeTo;

    @Column(name = "mediaTypeIdTo")
    private Integer mediaTypeIdTo;

    @Column(name = "mediaTypeNameTo")
    private String mediaTypeNameTo;

    @Column(name = "directionTo")
    private Integer directionTo;

    @Column(name = "fileSizeTo")
    private BigDecimal fileSizeTo;

    @Column(name = "fileTypeTo")
    private String fileTypeTo;

    @Column(name = "dataSourceTo")
    private Long dataSourceTo;

    @Column(name = "sourceIdTo")
    private Long sourceIdTo;

    @Column(name = "destIdTo")
    private Long destIdTo;

    @Column(name = "sourceIpTo")
    private String sourceIpTo;

    @Column(name = "destIpTo")
    private String destIpTo;

    @Column(name = "partNameTo")
    private Long partNameTo;

    @Column(name = "partName")
    private Long partName;

    @Column(name = "dataSourceNameFrom")
    private String dataSourceNameFrom;

    @Column(name = "dataSourceNameTo")
    private String dataSourceNameTo;

    @Column(name = "sourceNameFrom")
    private String sourceNameFrom;

    @Column(name = "destNameFrom")
    private String destNameFrom;

    @Column(name = "sourceNameTo")
    private String sourceNameTo;

    @Column(name = "destNameTo")
    private String destNameTo;

    @Column(name = "ingestTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ingestTime;

    @Column(name = "processStatus")
    private Integer processStatus;

    @Column(name = "dataVendor")
    private String dataVendor;

    public VsatMediaRelation() {
    }

    public VsatMediaRelation(String uuidKey) {
        this.uuidKey = uuidKey;
    }

    @Override
    public String toString() {
        return "com.elcom.metacen.raw.data.model.VsatMediaRelation[ uuidKey=" + uuidKey + " ]";
    }
}
