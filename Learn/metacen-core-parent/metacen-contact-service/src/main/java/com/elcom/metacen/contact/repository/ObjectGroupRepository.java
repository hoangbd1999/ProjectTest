/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectGroup;
import com.elcom.metacen.contact.model.ObjectGroupMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Admin
 */
@Repository
public interface ObjectGroupRepository extends MongoRepository<ObjectGroup, String> {

    ObjectGroup findByUuid(String uuid);

    ObjectGroup findByName(String name);

    List<ObjectGroup> findByConfigUuidInAndIsDeleted(String configUuid, int isDeleted);

}
