package com.elcom.metacen.raw.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TripCoordinateDTO {
    private String sourceType;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Float sog;
    private Float cog;
    private Float rot;
    private String dataVendor;
    private Timestamp eventTime;
    private Float draught;
}
