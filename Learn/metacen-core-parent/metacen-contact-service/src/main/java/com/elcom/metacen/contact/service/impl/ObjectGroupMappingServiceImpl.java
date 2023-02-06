/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;


import com.elcom.metacen.contact.model.ObjectGroupMapping;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupMappingRequestDTO;
import com.elcom.metacen.contact.repository.CustomObjectGroupMappingRepository;
import com.elcom.metacen.contact.repository.ObjectGroupMappingRepository;
import com.elcom.metacen.contact.service.ObjectGroupMappingService;
import com.elcom.metacen.enums.DataActiveStatus;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.utils.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * @author Admin
 */
@Service
public class ObjectGroupMappingServiceImpl implements ObjectGroupMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupMappingServiceImpl.class);

    @Autowired
    ObjectGroupMappingRepository objectGroupMappingRepository;

    @Autowired
    CustomObjectGroupMappingRepository customObjectGroupMappingRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public void delete(String groupId) {
        try {
//            Query query = new Query();
//            query.addCriteria(Criteria.where("group_id").is(groupId));
//
//            Update update = new Update();
//            update.set("is_deleted", DataDeleteStatus.DELETED.code());
//            update.set("taked_to_sync", DataDeleteStatus.NOT_DELETED.code());
//            update.set("updated_time", DateUtils.convertToLocalDateTime(new Date()));
//
//            customObjectGroupMappingRepository.updateMulti(query, update);
            objectGroupMappingRepository.deleteAllByGroupId(groupId);
        } catch (Exception ex) {
            LOGGER.error("delete object group failed >>> {}", ex.toString());
        }
    }

    @Override
    public List<ObjectGroupMapping> findByGroupId(String groupId) {
        try {
            return objectGroupMappingRepository.findByGroupIdInAndIsDeleted(groupId,DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("find group failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupMapping delete(ObjectGroupMapping objectGroupMapping) {
        try {
            objectGroupMapping.setUpdatedTime(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupMapping.setIsDeleted(DataDeleteStatus.DELETED.code());
            objectGroupMapping.setTakedToSync(DataDeleteStatus.NOT_DELETED.code());
            return objectGroupMappingRepository.save(objectGroupMapping);
        } catch (Exception ex) {
            LOGGER.error("delete object group mapping failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupMapping findByObjIdAndGroupId(String objId, String groupId) {
        try {
            return objectGroupMappingRepository.findByObjIdAndGroupIdAndIsDeleted(objId,groupId,DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("find object group mapping failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupMapping updateObjectMapping(ObjectGroupMapping objectGroupMapping, String objNote) {
        try {
            objectGroupMapping.setObjNote(objNote);
            objectGroupMapping.setUpdatedTime(DateUtils.convertToLocalDateTime(new Date()));
            ObjectGroupMapping response = objectGroupMappingRepository.save(objectGroupMapping);
            return response;
        } catch (Exception ex) {
            LOGGER.error("find object group mapping failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupMapping save(ObjectGroupMappingRequestDTO objectGroupMappingRequestDTO) {
        try {
            ObjectGroupMapping objectGroupMapping = modelMapper.map(objectGroupMappingRequestDTO, ObjectGroupMapping.class);
            objectGroupMapping.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            objectGroupMapping.setTakedToSync(DataDeleteStatus.NOT_DELETED.code());
            objectGroupMapping.setCreatedTime(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupMapping.setUpdatedTime(DateUtils.convertToLocalDateTime(new Date()));
            ObjectGroupMapping response = objectGroupMappingRepository.save(objectGroupMapping);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save Object Group Mapping failed >>> {}", ex.toString());
            return null;
        }
    }
}
