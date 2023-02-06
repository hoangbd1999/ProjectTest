/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.repository;

import com.elcom.metacen.data.process.model.DataProcessConfig;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigResponseDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Admin
 */
@Repository
public interface DataProcessConfigRepository extends MongoRepository<DataProcessConfig, String> {

    DataProcessConfig findByUuid(String uuid);

    @Override
    List<DataProcessConfig> findAll();
    
    List<DataProcessConfig> findAllByStatus(Integer status);
}
