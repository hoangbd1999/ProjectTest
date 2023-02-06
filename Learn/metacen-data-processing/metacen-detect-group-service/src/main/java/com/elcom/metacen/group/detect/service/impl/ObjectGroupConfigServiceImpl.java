package com.elcom.metacen.group.detect.service.impl;

import com.elcom.metacen.group.detect.model.ObjectGroupConfig;
import com.elcom.metacen.group.detect.model.dto.ObjectGroupConfigDTO;
import com.elcom.metacen.group.detect.repository.mongo.CustomObjectGroupConfigRepository;
import com.elcom.metacen.group.detect.service.IObjectGroupConfigService;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ObjectGroupConfigServiceImpl implements IObjectGroupConfigService {
    @Autowired
    private CustomObjectGroupConfigRepository customObjectGroupConfigRepository;
    @Autowired
    private TypeMap<ObjectGroupConfig, ObjectGroupConfigDTO> typeMap;

    @Override
    public List<ObjectGroupConfigDTO> getConfigAndFilter() {
        return customObjectGroupConfigRepository.getActiveConfig()
                .stream()
//                .filter(objectGroupConfig -> Objects.nonNull(objectGroupConfig.getStartTime()) || Objects.nonNull(objectGroupConfig.getEndTime()))
                .filter(objectGroupConfig -> {
                    if (Objects.isNull(objectGroupConfig.getStartTime()) && Objects.isNull(objectGroupConfig.getEndTime())) return true;
                    return objectGroupConfig.getStartTime().isBefore(LocalDateTime.now()) && objectGroupConfig.getEndTime().isAfter(LocalDateTime.now());
                })
                .map(config -> typeMap.map(config))
                .collect(Collectors.toList());
    }
}