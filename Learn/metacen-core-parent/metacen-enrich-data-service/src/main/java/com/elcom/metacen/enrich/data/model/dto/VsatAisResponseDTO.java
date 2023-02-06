package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VsatAisResponseDTO implements Serializable {

//    private String uuidKey;
    private BigInteger mmsi;
    private String name;
    private String callSign;
//    private String imo;
    private Integer countryId;
    private String countryName;
    private Integer typeId;
    private String typeName;
    private Long length;
    private Long width;
    private Integer dimA;
    private Integer dimB;
    private Integer dimC;
    private Integer dimD;
    private Float draught;
    private Float rot;
    private Float sog;
    private Float cog;
    private BigDecimal longitude;
    private BigDecimal latitude;
//    private String eta;
//    private String destination;
//    private BigInteger mmsiMaster;
//    private Long sourcePort;
    private String sourceIp;
//    private Long destPort;
    private String destIp;
//    private Integer direction;
//    private Long dataSourceId;
    private String dataSourceName;
//    private String dataVendor;
//    private Integer processStatus;
    private Timestamp eventTime;
//    private Timestamp ingestTime;
}
