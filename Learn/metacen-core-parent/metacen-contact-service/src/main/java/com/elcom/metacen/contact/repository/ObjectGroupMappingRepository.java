/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.ObjectGroupMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Admin
 */
@Repository
public interface ObjectGroupMappingRepository extends MongoRepository<ObjectGroupMapping, String> {

    List<ObjectGroupMapping> findByGroupIdInAndIsDeleted(String groupId, int isDeleted);

    ObjectGroupMapping findByObjIdAndGroupIdAndIsDeleted(String objId, String groupId, int isDeleted);

    List<ObjectGroupMapping> findByObjIdInAndIsDeleted(String objId, int isDeleted);

    void deleteAllByGroupId(String groupId);

}
