package com.elcom.metacen.group.detect.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class PairGroupDTO {
    private Integer count;
    private Integer checkCount = 0;
    private List<Long> cellIds;
    private List<LocalDateTime> eventTimes;

    public PairGroupDTO initAndAddEventTimes(LocalDateTime eventTime1, LocalDateTime eventTime2) {
        count = 1;
        eventTimes = new ArrayList<>();
        if (eventTime1.isAfter(eventTime2)) {
            eventTimes.add(eventTime2);
        } else {
            eventTimes.add(eventTime1);
        }
        return this;
    }

    public PairGroupDTO addEventTime(LocalDateTime eventTime1) {
        if (eventTimes == null) {
            eventTimes = new ArrayList<>();
        }
        eventTimes.add(eventTime1);
        return this;
    }

    public PairGroupDTO addEventTime(LocalDateTime eventTime1, LocalDateTime eventTime2) {
        if (eventTime1.isAfter(eventTime2)) {
            eventTimes.add(eventTime1);
        } else {
            eventTimes.add(eventTime2);
        }
        return this;
    }

    public PairGroupDTO addCellId(Long cellId) {
        if (cellIds == null) {
            cellIds = new ArrayList<>();
        }
        cellIds.add(cellId);
        return this;
    }
}
