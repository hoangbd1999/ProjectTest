package com.elcom.metacen.group.detect.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

public interface IDetectGroupService {
    @SneakyThrows
    void detectBatch(Integer hour, Integer level);

    @SneakyThrows
    void detect(Integer hour, Integer level);
}
