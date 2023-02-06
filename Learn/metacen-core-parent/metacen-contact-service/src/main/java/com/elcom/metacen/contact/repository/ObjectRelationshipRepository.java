/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectRelationship;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface ObjectRelationshipRepository extends MongoRepository<ObjectRelationship, String> {

    List<ObjectRelationship> findBySourceObjectTypeAndSourceObjectIdAndIsDeletedOrderByNoAsc(String sourceObjectType, String sourceObjectId, int isDeleted);

    List<ObjectRelationship> findBySourceObjectIdAndIsDeletedOrderByNoAsc(String sourceObjectId, int isDeleted);

    List<ObjectRelationship> findByDestObjectTypeAndDestObjectIdAndIsDeleted(String destObjectType, String destObjectId, int isDeleted);

}
