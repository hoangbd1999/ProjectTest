package com.elcom.metacen.group.detect.service;

import com.elcom.metacen.group.detect.model.dto.ObjectGroupConfigDTO;
import com.elcom.metacen.group.detect.model.dto.VsatAisDTO;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;

public interface IDetectGroupAsyncService {
    @SneakyThrows
    @Async
    void detectGroup(ObjectGroupConfigDTO objectGroupConfigDTO, List<VsatAisDTO> filterCoordinates, Integer hour, Integer level, LocalDateTime endTime);
}
