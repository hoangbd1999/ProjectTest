package com.elcom.metacen.raw.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ObjectTripDTO {
    private String uuidKey;
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
    private String sourceIp;
    private String destIp;
    private String dataSourceName;
    private String dataVendor;
    private String sourceType;
    private Timestamp eventTime;
    private Timestamp ingestTime;
    private List<TripCoordinateDTO> coordinates;
    private String mappingId;
}
