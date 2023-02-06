package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.ObjectTypes;
import com.elcom.metacen.contact.model.dto.ObjectTypesRequestDTO;
import com.elcom.metacen.contact.repository.ObjectTypesRepository;
import com.elcom.metacen.contact.service.ObjectTypesService;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.StringUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author hoangbd
 */
@Service
public class ObjectTypesServiceImpl implements ObjectTypesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypesServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ObjectTypesRepository objectTypesRepository;

    @Override
    public ObjectTypes findByTypeNameAndTypeCodeObjectType(String typeName, String typeCode) {
        return this.objectTypesRepository.findByTypeNameAndTypeCodeAndIsDeleted(typeName, typeCode, 0);
    }

    @Override
    public ObjectTypes save(ObjectTypesRequestDTO objectTypesDTO) {
        try {
            ObjectTypes objectTypes = modelMapper.map(objectTypesDTO, ObjectTypes.class);
            objectTypes.setTypeId(UUID.randomUUID().toString());
            objectTypes.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            objectTypes.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            objectTypes.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            ObjectTypes response = objectTypesRepository.save(objectTypes);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save ObjectTypes failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectTypes updateObjectType(ObjectTypes objectTypes, ObjectTypesRequestDTO objectTypesDTO) {
        try {
            objectTypes.setTypeName(objectTypesDTO.getTypeName());
            objectTypes.setTypeCode(objectTypesDTO.getTypeCode());
            objectTypes.setTypeDesc(objectTypesDTO.getTypeDesc());
            objectTypes.setTypeObject(objectTypesDTO.getTypeObject());
            objectTypes.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            ObjectTypes response = objectTypesRepository.save(objectTypes);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update ObjectTypes failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public ObjectTypes findByTypeId(String typeId) {
        ObjectTypes objectTypes = objectTypesRepository.findByTypeIdAndIsDeleted(typeId, DataDeleteStatus.NOT_DELETED.code());
        return objectTypes;
    }

    @Override
    public ObjectTypes delete(ObjectTypes objectTypes) {
        objectTypes.setIsDeleted(DataDeleteStatus.DELETED.code());
        return objectTypesRepository.save(objectTypes);
    }

    @Override
    public String getObjectTypeById(int id) {
       // return objectTypesRepository.getTypeObjectById(id);
        return null;
    }
}
