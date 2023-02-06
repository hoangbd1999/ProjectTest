package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.ObjectTypes;
import com.elcom.metacen.contact.model.dto.ObjectTypesRequestDTO;

/**
 * @author hoangbd
 */
public interface ObjectTypesService {

    ObjectTypes findByTypeNameAndTypeCodeObjectType(String typeName, String typeCode);

    ObjectTypes findByTypeId(String typeId);

    ObjectTypes save(ObjectTypesRequestDTO objectTypesDTO);

    ObjectTypes updateObjectType(ObjectTypes objectTypes, ObjectTypesRequestDTO objectTypesDTO);

    ObjectTypes delete(ObjectTypes objectTypes);

    String getObjectTypeById(int id);

}
