/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.OtherObject;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hoangbd
 */
@Repository
public interface OtherObjectRepository extends MongoRepository<OtherObject, String> {

    OtherObject findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<OtherObject> findByUuidInAndIsDeleted(List<String> uuidLst, int isDeleted);
}
