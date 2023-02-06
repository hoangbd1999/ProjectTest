package com.elcom.metacen.group.detect.service;

import com.elcom.metacen.group.detect.model.dto.VsatAisDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface IVsatAisService {
    List<VsatAisDTO> getListVsatAisWithCellId(Integer hour, Integer level);

    List<VsatAisDTO> getListVsatAisWithCellIdTest(Integer hour, Integer level, LocalDateTime startTime);
}
