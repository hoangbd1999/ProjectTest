/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;


import com.elcom.metacen.contact.model.ObjectGroupMapping;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupMappingRequestDTO;

import java.util.List;

/**
 * @author Admin
 */
public interface ObjectGroupMappingService {

    void delete(String groupId);

    List<ObjectGroupMapping> findByGroupId(String groupId);

    ObjectGroupMapping delete(ObjectGroupMapping objectGroupMapping);

    ObjectGroupMapping findByObjIdAndGroupId(String objId, String groupId);

    ObjectGroupMapping updateObjectMapping(ObjectGroupMapping objectGroupMapping, String objNote);

    ObjectGroupMapping save(ObjectGroupMappingRequestDTO objectGroupMappingRequestDTO);

}
