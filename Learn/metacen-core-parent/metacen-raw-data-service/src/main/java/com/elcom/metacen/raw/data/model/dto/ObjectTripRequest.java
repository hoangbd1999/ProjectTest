package com.elcom.metacen.raw.data.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Accessors(chain = true)
public class ObjectTripRequest {
    private String fromTime;
    private String toTime;
    private List<Integer> mmsiLst;
    private Integer limit;
}
