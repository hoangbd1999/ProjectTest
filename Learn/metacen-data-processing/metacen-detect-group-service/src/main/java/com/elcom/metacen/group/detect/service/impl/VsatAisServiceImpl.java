package com.elcom.metacen.group.detect.service.impl;

import com.elcom.metacen.group.detect.model.dto.VsatAisDTO;
import com.elcom.metacen.group.detect.repository.clickhouse.VsatAisDataRepository;
import com.elcom.metacen.group.detect.service.IVsatAisService;
import org.bson.codecs.jsr310.LocalDateTimeCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VsatAisServiceImpl implements IVsatAisService {
    @Autowired
    private VsatAisDataRepository vsatAisDataRepository;

    @Override
    public List<VsatAisDTO> getListVsatAisWithCellId(Integer hour, Integer level) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hour + 12);

        List<VsatAisDTO> result = new ArrayList<>();
        LocalDateTime endPartitionTime = startTime.plusHours(0);
        for (; endPartitionTime.isBefore(endTime); endPartitionTime = endPartitionTime.plusHours(4)) {
            long start = endPartitionTime.getYear() * 100000000L + endPartitionTime.getMonthValue() * 1000000L + endPartitionTime.getDayOfMonth() * 10000L + endPartitionTime.getHour() * 100L + endPartitionTime.getMinute() * 1L;
            long end = endPartitionTime.plusHours(4).getYear() * 100000000L + endPartitionTime.plusHours(4).getMonthValue() * 1000000L + endPartitionTime.plusHours(4).getDayOfMonth() * 10000L + endPartitionTime.plusHours(4).getHour() * 100L + endPartitionTime.plusHours(4).getMinute() * 1L;

            result.addAll(vsatAisDataRepository.getVsatAisWithCellId(start, end, level));
        }
        return result;
    }

    @Override
    public List<VsatAisDTO> getListVsatAisWithCellIdTest(Integer hour, Integer level, LocalDateTime startTime) {
        LocalDateTime endTime = startTime.plusHours(hour + 12);

        List<VsatAisDTO> result = new ArrayList<>();
        LocalDateTime endPartitionTime = startTime.plusHours(0);
        for (; endPartitionTime.isBefore(endTime); endPartitionTime = endPartitionTime.plusHours(4)) {
            long start = endPartitionTime.getYear() * 100000000L + endPartitionTime.getMonthValue() * 1000000L + endPartitionTime.getDayOfMonth() * 10000L + endPartitionTime.getHour() * 100L + endPartitionTime.getMinute() * 1L;
            long end = endPartitionTime.plusHours(4).getYear() * 100000000L + endPartitionTime.plusHours(4).getMonthValue() * 1000000L + endPartitionTime.plusHours(4).getDayOfMonth() * 10000L + endPartitionTime.plusHours(4).getHour() * 100L + endPartitionTime.plusHours(4).getMinute() * 1L;

            result.addAll(vsatAisDataRepository.getVsatAisWithCellId(start, end, level));
        }
        return result;
    }
}
