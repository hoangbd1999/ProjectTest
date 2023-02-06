package com.elcom.metacen.raw.data.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VsatAisDTO implements Serializable {

//    private String uuidKey;
    private BigInteger mmsi;
    private String name;
    private String callSign;
    private String imo;
    private Integer countryId;
    private String countryName;
    private Integer typeId;
    private String typeName;
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
    private String sourceIp;
    private String destIp;
//    private Long sourcePort;
//    private Long destPort;
    private String dataSourceName;
    private String dataVendor;
    private Timestamp eventTime;
}
