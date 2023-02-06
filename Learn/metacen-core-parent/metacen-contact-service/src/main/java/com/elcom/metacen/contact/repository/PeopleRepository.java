/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.People;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hoangbd
 */
@Repository
public interface PeopleRepository extends MongoRepository<People, String> {

    People findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<People> findByUuidIn(List<String> uuidLst);
}
