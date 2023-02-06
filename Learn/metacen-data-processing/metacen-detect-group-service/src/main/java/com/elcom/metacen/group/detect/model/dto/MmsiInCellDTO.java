package com.elcom.metacen.group.detect.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class MmsiInCellDTO {
    private BigInteger mmsi;
    private List<LocalDateTime> eventTimes;

    public MmsiInCellDTO() {
        eventTimes = new ArrayList<>();
    }

    public MmsiInCellDTO addEventTime(LocalDateTime eventTime) {
        eventTimes.add(eventTime);
        return this;
    }

    public MmsiInCellDTO addEventTimes(List<LocalDateTime> eventTimes) {
        this.eventTimes.addAll(eventTimes);
        return this;
    }
}
