/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.ObjectRelationship;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface ObjectRelationshipService {

    void save(List<ObjectRelationshipDTO> objectRelationshipDtoLst);

    void update(String sourceObjectId, List<ObjectRelationshipDTO> objectRelationshipDtoLst);

    void deleteObjectRelationship(String sourceObjectId);

    void deleteObjectRelationshipByDestObjectId(String destObjectId);

    List<ObjectRelationship> getRelationshipsBySourceObjectId(String sourceObjectType, String sourceObjectId);

    List<ObjectRelationship> getRelationshipsBySourceObjectId(String sourceObjectId);

    List<ObjectRelationship> getRelationshipsByDestObjectId(String destObjectType, String destObjectId);
}
