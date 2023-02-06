package com.elcom.metacen.group.detect.model.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VsatAisDTO implements Serializable {
    private BigInteger mmsi;
    private String name;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime eventTime;
    private String objId;
    private Long cellId;
    private Integer typeId;
}