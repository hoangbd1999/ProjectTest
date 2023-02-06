/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.contact.model.ObjectRelationship;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import com.elcom.metacen.contact.repository.CustomObjectRelationshipRepository;
import com.elcom.metacen.contact.repository.ObjectRelationshipRepository;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elcom.metacen.contact.service.ObjectRelationshipService;
import com.elcom.metacen.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 *
 * @author Admin
 */
@Service
public class ObjectRelationshipServiceImpl implements ObjectRelationshipService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectRelationshipServiceImpl.class);

    @Autowired
    ObjectRelationshipRepository objectRelationshipRepository;

    @Autowired
    CustomObjectRelationshipRepository customObjectRelationshipRepository;

    @Autowired
    private ObjectRelationshipService objectRelationshipService;

    @Override
    public void save(List<ObjectRelationshipDTO> objectRelationshipDtoLst) {
        Integer index = 1;
        Integer count = 1;
        try {
            if (objectRelationshipDtoLst != null && !objectRelationshipDtoLst.isEmpty()) {
                List<ObjectRelationship> objectRelationshipList = new ArrayList<>();
                Date now = new Date();
                for (ObjectRelationshipDTO objectRelationshipDTO : objectRelationshipDtoLst) {
                    ObjectRelationship objectRelationship = new ObjectRelationship();
                    objectRelationship.setNo(index++);
                    if(objectRelationshipDTO.getFromTime() == null) {
                        objectRelationship.setFromTime(null);
                    } else {
                        objectRelationship.setFromTime(DateUtils.parse(objectRelationshipDTO.getFromTime()));
                    }
                    if(objectRelationshipDTO.getToTime() == null) {
                        objectRelationship.setToTime(null);
                    } else {
                        objectRelationship.setToTime(DateUtils.parse(objectRelationshipDTO.getToTime()));
                    }
                    objectRelationship.setSourceObjectId(objectRelationshipDTO.getSourceObjectId());
                    objectRelationship.setSourceObjectType(objectRelationshipDTO.getSourceObjectType());
                    objectRelationship.setDestObjectId(objectRelationshipDTO.getDestObjectId());
                    objectRelationship.setDestObjectType(objectRelationshipDTO.getDestObjectType());
                    objectRelationship.setRelationshipType(objectRelationshipDTO.getRelationshipType());
                    objectRelationship.setNote(objectRelationshipDTO.getNote());
                    objectRelationship.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
                    objectRelationship.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    objectRelationship.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    objectRelationshipList.add(objectRelationship);
                }

                objectRelationshipRepository.saveAll(objectRelationshipList);
                // phụ thuộc

                List<ObjectRelationship> objectRelationshipDependentList = new ArrayList<>();
                for (ObjectRelationshipDTO objectRelationshipDTO : objectRelationshipDtoLst) {
                    ObjectRelationship objectRelationship = new ObjectRelationship();
                    objectRelationship.setNo(count++);
                    if(objectRelationshipDTO.getFromTime() == null) {
                        objectRelationship.setFromTime(null);
                    } else {
                        objectRelationship.setFromTime(DateUtils.parse(objectRelationshipDTO.getFromTime()));
                    }
                    if(objectRelationshipDTO.getToTime() == null) {
                        objectRelationship.setToTime(null);
                    } else {
                        objectRelationship.setToTime(DateUtils.parse(objectRelationshipDTO.getToTime()));
                    }
                    objectRelationship.setSourceObjectId(objectRelationshipDTO.getDestObjectId());
                    objectRelationship.setSourceObjectType(objectRelationshipDTO.getDestObjectType());
                    objectRelationship.setDestObjectId(objectRelationshipDTO.getSourceObjectId());
                    objectRelationship.setDestObjectType(objectRelationshipDTO.getSourceObjectType());
                    if(objectRelationshipDTO.getRelationshipType() == 0){
                        objectRelationship.setRelationshipType(1);
                    } else if(objectRelationshipDTO.getRelationshipType() == 1) {
                        objectRelationship.setRelationshipType(0);
                    }
                    objectRelationship.setNote("");
                    objectRelationship.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
                    objectRelationship.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    objectRelationship.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    objectRelationshipDependentList.add(objectRelationship);
                }
                objectRelationshipRepository.saveAll(objectRelationshipDependentList);

            }
        } catch (Exception ex) {
            LOGGER.error("Save object relationship failed >>> {}", ex.toString());
        }
    }

    @Override
    public void update(String sourceObjectId, List<ObjectRelationshipDTO> objectRelationshipDtoLst) {
        try {
            // delete object relationship
            List<ObjectRelationship> objectRelationshipSourceObject = objectRelationshipService.getRelationshipsBySourceObjectId(sourceObjectId);
            for (ObjectRelationship objectRelationship : objectRelationshipSourceObject) {
            //    deleteObjectRelationship(objectRelationship.getDestObjectId());
                deleteObjectRelationshipByDestAndSource(objectRelationship.getDestObjectId(),sourceObjectId);
            }
            deleteObjectRelationship(sourceObjectId);
            Integer index = 1;
            Integer count = 1;
            // insert object relationship
            if (objectRelationshipDtoLst != null && !objectRelationshipDtoLst.isEmpty()) {
                List<ObjectRelationship> objectRelationshipList = new ArrayList<>();
                Date now = new Date();
                for (ObjectRelationshipDTO objectRelationshipDTO : objectRelationshipDtoLst) {
                    ObjectRelationship objectRelationship = new ObjectRelationship();
                    objectRelationship.setNo(index++);
                    if(objectRelationshipDTO.getFromTime() == null) {
                        objectRelationship.setFromTime(null);
                    } else {
                        objectRelationship.setFromTime(DateUtils.parse(objectRelationshipDTO.getFromTime()));
                    }
                    if(objectRelationshipDTO.getToTime() == null) {
                        objectRelationship.setToTime(null);
                    } else {
                        objectRelationship.setToTime(DateUtils.parse(objectRelationshipDTO.getToTime()));
                    }
                    objectRelationship.setSourceObjectId(objectRelationshipDTO.getSourceObjectId());
                    objectRelationship.setSourceObjectType(objectRelationshipDTO.getSourceObjectType());
                    objectRelationship.setDestObjectId(objectRelationshipDTO.getDestObjectId());
                    objectRelationship.setDestObjectType(objectRelationshipDTO.getDestObjectType());
                    objectRelationship.setRelationshipType(objectRelationshipDTO.getRelationshipType());
                    objectRelationship.setNote(objectRelationshipDTO.getNote());
                    objectRelationship.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
                    objectRelationship.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    objectRelationship.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    objectRelationshipList.add(objectRelationship);
                }

                objectRelationshipRepository.saveAll(objectRelationshipList);

                // phụ thuộc
                List<ObjectRelationship> objectRelationshipDependentList = new ArrayList<>();
                for (ObjectRelationshipDTO objectRelationshipDTO : objectRelationshipDtoLst) {
               //     deleteObjectRelationship(objectRelationshipDTO.getDestObjectId());
                    deleteObjectRelationshipByDestAndSource(objectRelationshipDTO.getDestObjectId(),sourceObjectId);
                    ObjectRelationship objectRelationship = new ObjectRelationship();
                    objectRelationship.setNo(count++);
                    if(objectRelationshipDTO.getFromTime() == null) {
                        objectRelationship.setFromTime(null);
                    } else {
                        objectRelationship.setFromTime(DateUtils.parse(objectRelationshipDTO.getFromTime()));
                    }
                    if(objectRelationshipDTO.getToTime() == null) {
                        objectRelationship.setToTime(null);
                    } else {
                        objectRelationship.setToTime(DateUtils.parse(objectRelationshipDTO.getToTime()));
                    }
                    objectRelationship.setSourceObjectId(objectRelationshipDTO.getDestObjectId());
                    objectRelationship.setSourceObjectType(objectRelationshipDTO.getDestObjectType());
                    objectRelationship.setDestObjectId(objectRelationshipDTO.getSourceObjectId());
                    objectRelationship.setDestObjectType(objectRelationshipDTO.getSourceObjectType());
                    if(objectRelationshipDTO.getRelationshipType() == 0){
                        objectRelationship.setRelationshipType(1);
                    } else if(objectRelationshipDTO.getRelationshipType() == 1) {
                        objectRelationship.setRelationshipType(0);
                    }
                    objectRelationship.setNote("");
                    objectRelationship.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
                    objectRelationship.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    objectRelationship.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    objectRelationshipDependentList.add(objectRelationship);
                }
                objectRelationshipRepository.saveAll(objectRelationshipDependentList);
            }
        } catch (Exception ex) {
            LOGGER.error("Update object relationship failed >>> {}", ex.toString());
        }
    }

    @Override
    public List<ObjectRelationship> getRelationshipsBySourceObjectId(String sourceObjectType, String sourceObjectId) {
        return objectRelationshipRepository.findBySourceObjectTypeAndSourceObjectIdAndIsDeletedOrderByNoAsc(sourceObjectType, sourceObjectId, DataDeleteStatus.NOT_DELETED.code());
    }

    @Override
    public List<ObjectRelationship> getRelationshipsBySourceObjectId(String sourceObjectId) {
        return objectRelationshipRepository.findBySourceObjectIdAndIsDeletedOrderByNoAsc(sourceObjectId, DataDeleteStatus.NOT_DELETED.code());

    }

    @Override
    public List<ObjectRelationship> getRelationshipsByDestObjectId(String destObjectType, String destObjectId) {
        return objectRelationshipRepository.findByDestObjectTypeAndDestObjectIdAndIsDeleted(destObjectType, destObjectId, DataDeleteStatus.NOT_DELETED.code());
    }

    public void deleteObjectRelationship(String sourceObjectId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("source_object_id").is(sourceObjectId));

        Update update = new Update();
        update.set("is_deleted", DataDeleteStatus.DELETED.code());
        update.set("modified_date", DateUtils.convertToLocalDateTime(new Date()));

        customObjectRelationshipRepository.updateMulti(query, update);
    }

    public void deleteObjectRelationshipByDestAndSource(String sourceObjectId,String destObjectId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("source_object_id").is(sourceObjectId).and("dest_object_id").is(destObjectId));

        Update update = new Update();
        update.set("is_deleted", DataDeleteStatus.DELETED.code());
        update.set("modified_date", DateUtils.convertToLocalDateTime(new Date()));

        customObjectRelationshipRepository.updateMulti(query, update);
    }

    public void deleteObjectRelationshipByDestObjectId(String destObjectId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("dest_object_id").is(destObjectId));

        Update update = new Update();
        update.set("is_deleted", DataDeleteStatus.DELETED.code());
        update.set("modified_date", DateUtils.convertToLocalDateTime(new Date()));

        customObjectRelationshipRepository.updateMulti(query, update);
    }

}
