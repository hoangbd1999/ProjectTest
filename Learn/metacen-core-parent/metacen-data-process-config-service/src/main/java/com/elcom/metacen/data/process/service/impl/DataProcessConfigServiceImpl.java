/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.service.impl;

import com.elcom.metacen.data.process.model.DataProcessConfig;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigRequestDTO;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigResponseDTO;
import com.elcom.metacen.data.process.repository.CustomDataProcessConfigRepository;
import com.elcom.metacen.data.process.repository.DataProcessConfigRepository;
import com.elcom.metacen.data.process.service.DataProcessConfigService;
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

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

/**
 * @author Admin
 */
@Service
public class DataProcessConfigServiceImpl implements DataProcessConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessConfigServiceImpl.class);
    @Autowired
    DataProcessConfigRepository dataProcessConfigRepository;

    @Autowired
    CustomDataProcessConfigRepository customDataProcessConfigRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public DataProcessConfig save(DataProcessConfigRequestDTO dataProcessConfigRequestDTO, String createBy) {
        try {
            DataProcessConfig dataProcessConfig = modelMapper.map(dataProcessConfigRequestDTO, DataProcessConfig.class);
            dataProcessConfig.setUuid(UUID.randomUUID().toString());
            dataProcessConfig.setStatus(DataActiveStatus.ACTIVE.code());
            dataProcessConfig.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            dataProcessConfig.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            dataProcessConfig.setCreatedBy(createBy);

            DataProcessConfig response = dataProcessConfigRepository.save(dataProcessConfig);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save Data Process failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public DataProcessConfig findByUuid(String uuid) {
        try {
            return dataProcessConfigRepository.findByUuid(uuid);
        } catch (Exception ex) {
            LOGGER.error("find data process failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public DataProcessConfig update(DataProcessConfig dataProcessConfig, DataProcessConfigRequestDTO dataProcessConfigRequestDTO, String modifiedBy) {
        try {
            dataProcessConfig.setName(dataProcessConfigRequestDTO.getName());
            dataProcessConfig.setDataType(dataProcessConfigRequestDTO.getDataType());
            dataProcessConfig.setProcessType(dataProcessConfigRequestDTO.getProcessType());
            dataProcessConfig.setDataVendor(dataProcessConfigRequestDTO.getDataVendor());
            dataProcessConfig.setStartTime(dataProcessConfigRequestDTO.getStartTime());
            dataProcessConfig.setEndTime(dataProcessConfigRequestDTO.getEndTime());
            dataProcessConfig.setDetailConfig(dataProcessConfigRequestDTO.getDetailConfig());
            dataProcessConfig.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            dataProcessConfig.setModifiedBy(modifiedBy);
            DataProcessConfig response = dataProcessConfigRepository.save(dataProcessConfig);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update data process failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<DataProcessConfigResponseDTO> findListDataProcessConfig(DataProcessConfigFilterDTO dataProcessConfigFilterDTO) {
        try {
            Integer page = dataProcessConfigFilterDTO.getPage() > 0 ? dataProcessConfigFilterDTO.getPage() : 0;
            Pageable pageable = PageRequest.of(page, dataProcessConfigFilterDTO.getSize());
            return customDataProcessConfigRepository.search(dataProcessConfigFilterDTO, pageable);
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public List<DataProcessConfig> getList() {
        try {
            //return dataProcessConfigRepository.findAllByStatus(DataActiveStatus.ACTIVE.code());
            return dataProcessConfigRepository.findAll();
        } catch (Exception ex) {
            LOGGER.error("get list failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public DataProcessConfig delete(DataProcessConfig dataProcessConfig) {
        try {
            dataProcessConfigRepository.delete(dataProcessConfig);
            return null;
        } catch (Exception ex) {
            LOGGER.error("delete data process failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public DataProcessConfig statusChange(DataProcessConfig dataProcessConfig, Integer status) {
        try {
            dataProcessConfig.setStatus(status);
            return dataProcessConfigRepository.save(dataProcessConfig);
        } catch (Exception ex) {
            LOGGER.error("change status failed >>> {}", ex.toString());
            return null;
        }
    }
}
