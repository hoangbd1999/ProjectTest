package com.elcom.metacen.group.detect.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class MappingPairInfo {
//    private String key; // mmsi1:mmsi2
    private List<LocalDateTime> eventTimes;

    public LocalDateTime getMeetTime() {
        return eventTimes.get(0);
    }

    public LocalDateTime getSeparateTime() {
        return eventTimes.get(eventTimes.size() - 1);
    }
}
