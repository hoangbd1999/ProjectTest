/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.ObjectGroupDefine;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineMappingDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineRequestDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineResponseDTO;
import com.elcom.metacen.contact.repository.*;
import com.elcom.metacen.contact.service.ObjectGroupDefineService;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * @author Admin
 */
@Service
public class ObjectGroupDefineServiceImpl implements ObjectGroupDefineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupDefineServiceImpl.class);
    @Autowired
    CustomObjectGroupDefineRepository customObjectGroupDefineRepository;

    @Autowired
    ObjectGroupDefineRepository objectGroupDefineRepository;

    @Autowired
    ModelMapper modelMapper;



    @Override
    public Page<ObjectGroupDefineResponseDTO> findListObjectGroupDefine(ObjectGroupDefineFilterDTO objectGroupDefineFilterDTO) {
        try {
            Integer page = objectGroupDefineFilterDTO.getPage() > 0 ? objectGroupDefineFilterDTO.getPage() : 0;
            Pageable pageable = PageRequest.of(page, objectGroupDefineFilterDTO.getSize());
            return customObjectGroupDefineRepository.search(objectGroupDefineFilterDTO, pageable);
        } catch (Exception ex) {
            LOGGER.error("filter failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupDefine findByUuid(String uuid) {
        try {
            return objectGroupDefineRepository.findByUuidAndIsDeleted(uuid,DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception ex) {
            LOGGER.error("not find >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupDefine findByName(String name) {
        try {
            return objectGroupDefineRepository.findByName(name);
        } catch (Exception ex) {
            LOGGER.error("not find >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupDefine save(ObjectGroupDefineRequestDTO objectGroupDefineRequestDTO, String createBy) {
        try {
            ObjectGroupDefine objectGroupDefine = modelMapper.map(objectGroupDefineRequestDTO, ObjectGroupDefine.class);
            objectGroupDefine.setUuid(UUID.randomUUID().toString());
            objectGroupDefine.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            objectGroupDefine.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupDefine.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupDefine.setCreatedBy(createBy);

            ObjectGroupDefine response = objectGroupDefineRepository.save(objectGroupDefine);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Save object group define failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupDefine delete(ObjectGroupDefine objectGroupDefine) {
        try {
            objectGroupDefine.setIsDeleted(DataDeleteStatus.DELETED.code());
            return objectGroupDefineRepository.save(objectGroupDefine);
        } catch (Exception ex) {
            LOGGER.error("delete failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectGroupDefine statusChange(ObjectGroupDefine objectGroupDefine, Boolean isMainObject, String objectUuid) {
        try {
            List<ObjectGroupDefineMappingDTO> listData = objectGroupDefine.getObjects();
            for (int i = 0; i < listData.size(); i++) {
                if(objectUuid.equalsIgnoreCase(listData.get(i).getUuid())){
                    listData.get(i).setIsMainObject(isMainObject);
                }
            }
            objectGroupDefine.setObjects(listData);
            return objectGroupDefineRepository.save(objectGroupDefine);
        } catch (Exception ex) {
            LOGGER.error("change status failed >>> {}", ex.toString());
            return null;
        }
    }

//    @Override
//    public ObjectGroup delete(ObjectGroup objectGroup) {
//        try {
//            objectGroup.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
//            objectGroup.setIsDeleted(DataDeleteStatus.DELETED.code());
//            return objectGroupRepository.save(objectGroup);
//        } catch (Exception ex) {
//            LOGGER.error("delete object group failed >>> {}", ex.toString());
//            return null;
//        }
//    }

    @Override
    public ObjectGroupDefine update(ObjectGroupDefine objectGroupDefine, ObjectGroupDefineRequestDTO objectGroupDefineRequestDTO, String modifiedBy) {
        try {
            objectGroupDefine.setName(objectGroupDefineRequestDTO.getName());
            objectGroupDefine.setNote(objectGroupDefineRequestDTO.getNote());
            objectGroupDefine.setObjects(objectGroupDefineRequestDTO.getObjects());
            objectGroupDefine.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectGroupDefine.setModifiedBy(modifiedBy);

            ObjectGroupDefine response = objectGroupDefineRepository.save(objectGroupDefine);

            return response;
        } catch (Exception ex) {
            LOGGER.error("Update object group define failed >>> {}", ex.toString());
            return null;
        }
    }
}
