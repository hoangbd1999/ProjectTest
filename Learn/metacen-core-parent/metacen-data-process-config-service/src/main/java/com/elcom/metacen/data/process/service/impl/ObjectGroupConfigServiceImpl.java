/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.service.impl;

import com.elcom.metacen.data.process.model.ObjectGroupConfig;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigRequestDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigResponseDTO;
import com.elcom.metacen.data.process.repository.CustomObjectGroupConfigRepository;
import com.elcom.metacen.data.process.repository.ObjectGroupConfigRepository;
import com.elcom.metacen.data.process.service.ObjectGroupConfigService;
import com.elcom.metacen.enums.DataActiveStatus;
import com.elcom.metacen.utils.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author Admin
 */
@Service
public class ObjectGroupConfigServiceImpl implements ObjectGroupConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupConfigServiceImpl.class);
    @Autowired
    ObjectGroupConfigRepository objectGroupConfigRepository;

    @Autowired
    CustomObjectGroupConfigRepository customObjectGroupConfigRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public ObjectGroupConfig save(ObjectGroupConfigRequestDTO objectGroupConfigRequestDTO, String createBy) {
        try {
            ObjectGroupConfig objectGroupConfig = modelMapper.map(objectGroupConfigRequestDTO, ObjectGroupConfig.class);
            objectGroupConfig.setUuid(UUID.randomUUID().toString());
            objectGroupConfig.setIsActive(DataActiveStatus.ACTIVE.code());
            objectGroupConfig.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupConfig.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupConfig.setCreatedBy(createBy);

            ObjectGroupConfig response = objectGroupConfigRepository.save(objectGroupConfig);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save Object Group failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupConfig findByUuid(String uuid) {
        try {
            return objectGroupConfigRepository.findByUuid(uuid);
        } catch (Exception ex) {
            LOGGER.error("find data process failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupConfig update(ObjectGroupConfig objectGroupConfig, ObjectGroupConfigRequestDTO objectGroupConfigRequestDTO, String modifiedBy) {
        try {
            objectGroupConfig.setName(objectGroupConfigRequestDTO.getName());
            objectGroupConfig.setCoordinates(objectGroupConfigRequestDTO.getCoordinates());
            objectGroupConfig.setAreaUuid(objectGroupConfigRequestDTO.getAreaUuid());
            objectGroupConfig.setStartTime(objectGroupConfigRequestDTO.getStartTime());
            objectGroupConfig.setEndTime(objectGroupConfigRequestDTO.getEndTime());
            objectGroupConfig.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupConfig.setModifiedBy(modifiedBy);
            ObjectGroupConfig response = objectGroupConfigRepository.save(objectGroupConfig);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update object group failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<ObjectGroupConfigResponseDTO> findListObjectGroupConfig(ObjectGroupConfigFilterDTO objectGroupConfigFilterDTO) {
        try {
            Integer page = objectGroupConfigFilterDTO.getPage() > 0 ? objectGroupConfigFilterDTO.getPage() : 0;
            Pageable pageable = PageRequest.of(page, objectGroupConfigFilterDTO.getSize());
            return customObjectGroupConfigRepository.search(objectGroupConfigFilterDTO, pageable);
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }
    @Override
    public ObjectGroupConfig delete(ObjectGroupConfig objectGroupConfig) {
        try {
            objectGroupConfigRepository.delete(objectGroupConfig);
            return null;
        } catch (Exception ex) {
            LOGGER.error("delete object group failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupConfig statusChange(ObjectGroupConfig objectGroupConfig, Integer isActive) {
        try {
            objectGroupConfig.setIsActive(isActive);
            return objectGroupConfigRepository.save(objectGroupConfig);
        } catch (Exception ex) {
            LOGGER.error("change status failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupConfig findByName(String name) {
        try {
            return objectGroupConfigRepository.findByName(name);
        } catch (Exception ex) {
            LOGGER.error("not find >>> {}", ex.toString());
            return null;
        }
    }
}
