/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Infrastructure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 *
 * @author hoangbd
 */
@Repository
public interface InfrastructureRepository extends MongoRepository<Infrastructure, String> {

    Infrastructure findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<Infrastructure> findByUuidIn(List<String> uuidLst);
}
