package com.elcom.metacen.group.detect.service.impl;

import com.elcom.metacen.group.detect.model.dto.ObjectGroupConfigDTO;
import com.elcom.metacen.group.detect.model.dto.VsatAisDTO;
import com.elcom.metacen.group.detect.service.IDetectGroupAsyncService;
import com.elcom.metacen.group.detect.service.IDetectGroupService;
import com.elcom.metacen.group.detect.service.IObjectGroupConfigService;
import com.elcom.metacen.group.detect.service.IVsatAisService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.geom.Path2D;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetectGroupServiceImpl implements IDetectGroupService {

    private static final Logger log = LoggerFactory.getLogger(DetectGroupServiceImpl.class);

    @Autowired
    private IVsatAisService vsatAisService;
    @Autowired
    private IObjectGroupConfigService objectGroupConfigService;
    @Autowired
    private IDetectGroupAsyncService detectGroupAsyncService;

    @Override
    @SneakyThrows
    public void detectBatch(Integer hour, Integer level) {
        //2022-12-02T19:00
        LocalDateTime startTime = LocalDateTime.of(2022, 12, 31, 00, 00);
//        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.of(2023, 1, 1, 00, 00);
        for (; startTime.isBefore(endTime); startTime = startTime.plusMinutes(30)) {
            final List<VsatAisDTO> vsatAisWithCellId = vsatAisService.getListVsatAisWithCellIdTest(hour, level, startTime);
            List<ObjectGroupConfigDTO> activeConfigs = objectGroupConfigService.getConfigAndFilter();

            for (int i = 0; i < activeConfigs.size(); i++) {
                List<VsatAisDTO> filterCoordinates = filterListCoordinateInConfigArea(activeConfigs.get(i), vsatAisWithCellId);
                if (filterCoordinates.size() > 0) {
                    log.info("START DETECT GROUP - from: {} to {}", startTime, startTime.plusHours(hour + 12));
                    detectGroupAsyncService.detectGroup(activeConfigs.get(i), filterCoordinates, hour, level, startTime.plusHours(hour + 12));
                }
            }
        }
    }

    @Override
    @SneakyThrows
    public void detect(Integer hour, Integer level) {
        final List<VsatAisDTO> vsatAisWithCellId = vsatAisService.getListVsatAisWithCellId(hour, level);
        List<ObjectGroupConfigDTO> activeConfigs = objectGroupConfigService.getConfigAndFilter();

        for (int i = 0; i < activeConfigs.size(); i++) {
            List<VsatAisDTO> filterCoordinates = filterListCoordinateInConfigArea(activeConfigs.get(i), vsatAisWithCellId);
            if (filterCoordinates.size() > 0) {
                log.info("START DETECT GROUP - configId: {}", activeConfigs.get(i).getUuid());
                detectGroupAsyncService.detectGroup(activeConfigs.get(i), filterCoordinates, hour, level, LocalDateTime.now());
            }
        }
    }

    private List<VsatAisDTO> filterListCoordinateInConfigArea(ObjectGroupConfigDTO objectGroupConfigDTO, List<VsatAisDTO> vsatAisWithCellId) {
        Path2D prettyPolyPath = new Path2D.Double();
        prettyPolyPath.moveTo(objectGroupConfigDTO.getCoordinates().get(0).getX(), objectGroupConfigDTO.getCoordinates().get(0).getY());
        for (int i = 1; i < objectGroupConfigDTO.getCoordinates().size(); i++) {
            prettyPolyPath.lineTo(objectGroupConfigDTO.getCoordinates().get(i).getX(), objectGroupConfigDTO.getCoordinates().get(i).getY());
        }
        prettyPolyPath.lineTo(objectGroupConfigDTO.getCoordinates().get(0).getX(), objectGroupConfigDTO.getCoordinates().get(0).getY());
        prettyPolyPath.closePath();
        return vsatAisWithCellId.stream().filter(coordinate ->
                        prettyPolyPath.contains(
                                coordinate.getLongitude().doubleValue(),
                                coordinate.getLatitude().doubleValue()))
                .collect(Collectors.toList());
    }
}
