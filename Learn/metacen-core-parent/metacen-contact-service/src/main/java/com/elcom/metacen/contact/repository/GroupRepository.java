/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 *
 * @author hoangbd
 */
@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

    Group findByUuidKeyAndIsDeleted(String uuidKey , int isDeleted);

    Page<Group> findByIsDeleted(Pageable pageable, int isDeleted);

}
