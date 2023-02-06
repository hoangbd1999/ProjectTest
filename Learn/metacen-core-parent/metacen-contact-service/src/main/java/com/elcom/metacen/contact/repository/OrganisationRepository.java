/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Organisation;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface OrganisationRepository extends MongoRepository<Organisation, String> {

    Organisation findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<Organisation> findByUuidIn(List<String> uuidLst);
}
