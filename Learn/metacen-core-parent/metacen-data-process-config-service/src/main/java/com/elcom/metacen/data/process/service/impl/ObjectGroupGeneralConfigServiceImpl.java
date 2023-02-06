/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.service.impl;

import com.elcom.metacen.data.process.model.ObjectGroupConfig;
import com.elcom.metacen.data.process.model.ObjectGroupGeneralConfig;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigRequestDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigResponseDTO;
import com.elcom.metacen.data.process.repository.CustomObjectGroupConfigRepository;
import com.elcom.metacen.data.process.repository.CustomObjectGroupGeneralConfigRepository;
import com.elcom.metacen.data.process.repository.ObjectGroupConfigRepository;
import com.elcom.metacen.data.process.repository.ObjectGroupGeneralConfigRepository;
import com.elcom.metacen.data.process.service.ObjectGroupConfigService;
import com.elcom.metacen.data.process.service.ObjectGroupGeneralConfigService;
import com.elcom.metacen.enums.DataActiveStatus;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.utils.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author Admin
 */
@Service
public class ObjectGroupGeneralConfigServiceImpl implements ObjectGroupGeneralConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupGeneralConfigServiceImpl.class);
    @Autowired
    ObjectGroupConfigRepository objectGroupConfigRepository;

    @Autowired
    CustomObjectGroupGeneralConfigRepository customObjectGroupGeneralConfigRepository;

    @Autowired
    ObjectGroupGeneralConfigRepository objectGroupGeneralConfigRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public void update(int togetherTime,int distanceLevel,String modifiedBy) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("distance_level").ne(null));

            Update update = new Update();
            update.set("together_time", togetherTime);
            update.set("distance_level", distanceLevel);
            update.set("modified_by", modifiedBy);
            update.set("modified_date", DateUtils.convertToLocalDateTime(new Date()));

            customObjectGroupGeneralConfigRepository.updateMulti(query, update);
        } catch (Exception ex) {
            LOGGER.error("update failed >>> {}", ex.toString());
        }
    }

    @Override
    public ObjectGroupGeneralConfig findByTogetherTime() {
        try {
            return objectGroupGeneralConfigRepository.findByTogetherTimeIsNotNull();
        } catch (Exception ex) {
            LOGGER.error("update failed >>> {}", ex.toString());
            return null;
        }
    }

}
