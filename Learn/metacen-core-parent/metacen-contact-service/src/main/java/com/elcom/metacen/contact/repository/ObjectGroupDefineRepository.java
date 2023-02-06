/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectGroupDefine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 *
 * @author Admin
 */
@Repository
public interface ObjectGroupDefineRepository extends MongoRepository<ObjectGroupDefine, String> {

    ObjectGroupDefine findByUuidAndIsDeleted(String uuid, int isDeleted);

    ObjectGroupDefine findByName(String name);

}
