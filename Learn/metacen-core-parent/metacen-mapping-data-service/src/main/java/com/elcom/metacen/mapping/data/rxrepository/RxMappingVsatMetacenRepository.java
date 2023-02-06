/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.mapping.data.rxrepository;

import com.elcom.metacen.mapping.data.model.MappingVsatMetacen;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface RxMappingVsatMetacenRepository extends ReactiveMongoRepository<MappingVsatMetacen, String> {
}
