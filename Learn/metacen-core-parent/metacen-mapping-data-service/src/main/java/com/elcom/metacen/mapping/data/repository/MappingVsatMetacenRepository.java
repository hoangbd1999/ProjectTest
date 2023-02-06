/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.mapping.data.repository;

import com.elcom.metacen.mapping.data.model.MappingVsatMetacen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Admin
 */
@Repository
public interface MappingVsatMetacenRepository extends MongoRepository<MappingVsatMetacen, String> {

    MappingVsatMetacen findByUuid(String uuid);

    List<MappingVsatMetacen> findByObjectUuidIn(String objectUuid);

    MappingVsatMetacen findFirstByVsatIpAddressAndVsatDataSourceId(String vsatIpAddress, Integer vsatDataSouceId);

    MappingVsatMetacen findFirstByObjectTypeAndObjectUuid(String objectType, String objectUuid);
}
