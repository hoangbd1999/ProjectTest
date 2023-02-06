/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.ObjectGroup;
import com.elcom.metacen.contact.model.ObjectGroupMapping;
import com.elcom.metacen.contact.model.dto.ObjectGroup.*;
import com.elcom.metacen.contact.repository.CustomObjectGroupRepository;
import com.elcom.metacen.contact.repository.ObjectGroupMappingRepository;
import com.elcom.metacen.contact.repository.ObjectGroupRepository;
import com.elcom.metacen.contact.service.ObjectGroupMappingService;
import com.elcom.metacen.contact.service.ObjectGroupService;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.utils.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author Admin
 */
@Service
public class ObjectGroupServiceImpl implements ObjectGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupServiceImpl.class);
    @Autowired
    CustomObjectGroupRepository customObjectGroupRepository;

    @Autowired
    ObjectGroupRepository objectGroupRepository;

    @Autowired
    private ObjectGroupMappingService objectGroupMappingService;

    @Autowired
    private ObjectGroupMappingRepository objectGroupMappingRepository;

    @Autowired
    ModelMapper modelMapper;



    @Override
    public Page<ObjectGroupResponseDTO> findListObjectGroupUnconfirmed(ObjectGroupUnconfirmedFilterDTO objectGroupUnconfirmedFilterDTO) {
        try {
            Integer page = objectGroupUnconfirmedFilterDTO.getPage() > 0 ? objectGroupUnconfirmedFilterDTO.getPage() : 0;
            Pageable pageable = PageRequest.of(page, objectGroupUnconfirmedFilterDTO.getSize());
            return customObjectGroupRepository.search(objectGroupUnconfirmedFilterDTO, pageable);
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<ObjectGroupResponseDTO> findListObjectGroupConfirmed(ObjectGroupConfirmedFilterDTO objectGroupConfirmedFilterDTO) {
        try {
            Integer page = objectGroupConfirmedFilterDTO.getPage() > 0 ? objectGroupConfirmedFilterDTO.getPage() : 0;
            Pageable pageable = PageRequest.of(page, objectGroupConfirmedFilterDTO.getSize());
            return customObjectGroupRepository.search(objectGroupConfirmedFilterDTO, pageable);
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroup findByUuid(String uuid) {
        try {
            return objectGroupRepository.findByUuid(uuid);
        } catch (Exception ex) {
            LOGGER.error("not find >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroup findByName(String name) {
        try {
            return objectGroupRepository.findByName(name);
        } catch (Exception ex) {
            LOGGER.error("not find >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public List<ObjectGroup> findByConfigUuid(String configUuid) {
        try {
            return objectGroupRepository.findByConfigUuidInAndIsDeleted(configUuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("not find >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroup delete(ObjectGroup objectGroup) {
        try {
            objectGroup.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroup.setIsDeleted(DataDeleteStatus.DELETED.code());
            return objectGroupRepository.save(objectGroup);
        } catch (Exception ex) {
            LOGGER.error("delete object group failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroup update(ObjectGroup objectGroup, ObjectGroupRequestDTO objectGroupRequestDTO, String modifiedBy) {
        try {
            objectGroup.setName(objectGroupRequestDTO.getName());
            objectGroup.setNote(objectGroupRequestDTO.getNote());
            objectGroup.setConfigName(objectGroupRequestDTO.getConfigName());
            objectGroup.setConfigUuid(objectGroupRequestDTO.getConfigUuid());
            objectGroup.setIsConfirmed(1);
            objectGroup.setConfigDistanceLevel(objectGroupRequestDTO.getConfigDistanceLevel());
            objectGroup.setConfigTogetherTime(objectGroupRequestDTO.getConfigTogetherTime());
           // objectGroup.setEventTimes(objectGroupRequestDTO.getEventTimes());
            objectGroup.setConfirmDate(new Date());
            objectGroup.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroup.setModifiedBy(modifiedBy);
            ObjectGroup response = objectGroupRepository.save(objectGroup);

            // update object group mapping
            objectGroupMappingService.delete(objectGroup.getUuid());
            List<ObjectGroupMappingDTO> objectGroupMappingDtoList = objectGroupRequestDTO.getObjects();
            if (objectGroupMappingDtoList != null && !objectGroupMappingDtoList.isEmpty()) {
                List<ObjectGroupMapping> objectGroupMappingList = new ArrayList<>();
                Date now = new Date();
                for (ObjectGroupMappingDTO objectGroupMappingDTO : objectGroupMappingDtoList) {
                    ObjectGroupMapping objectGroupMapping = new ObjectGroupMapping();
                    objectGroupMapping.setObjId(objectGroupMappingDTO.getObjId());
                    objectGroupMapping.setObjName(objectGroupMappingDTO.getObjName());
                    objectGroupMapping.setObjTypeId(objectGroupMappingDTO.getObjTypeId());
                    objectGroupMapping.setGroupId(objectGroupMappingDTO.getGroupId());
                    objectGroupMapping.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
                    objectGroupMapping.setTakedToSync(DataDeleteStatus.NOT_DELETED.code());
                    objectGroupMapping.setCreatedTime(DateUtils.convertToLocalDateTime(now));
                    objectGroupMapping.setUpdatedTime(DateUtils.convertToLocalDateTime(now));

                    objectGroupMappingList.add(objectGroupMapping);
                }
                objectGroupMappingRepository.saveAll(objectGroupMappingList);
            }

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update object group failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroup updateObjectGroupName(ObjectGroup objectGroup, String modifiedBy, String name) {
        try {
            objectGroup.setName(name);
            objectGroup.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            ObjectGroup response = objectGroupRepository.save(objectGroup);
            return response;
        } catch (Exception ex) {
            LOGGER.error("find object group failed >>> {}", ex.toString());
            return null;
        }
    }
}
