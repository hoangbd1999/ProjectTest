/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.repository;

import com.elcom.metacen.data.process.model.ObjectGroupConfig;
import com.elcom.metacen.data.process.model.ObjectGroupGeneralConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 *
 * @author Admin
 */
@Repository
public interface ObjectGroupGeneralConfigRepository extends MongoRepository<ObjectGroupGeneralConfig, String> {

    ObjectGroupGeneralConfig findByTogetherTimeIsNotNull();

}
