package com.elcom.metacen.group.detect.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class CellDTO {
    private Long cellId;
    private BigInteger mmsi;
    private LocalDateTime inTime;
}
