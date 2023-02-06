/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service.impl;

import com.elcom.metacen.enrich.data.model.SatelliteImageObjectAnalyzed;
import com.elcom.metacen.enrich.data.repository.SatelliteImageObjectAnalyzedRepository;
import com.elcom.metacen.enrich.data.service.SatelliteImageObjectAnalyzedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author Admin
 */
@Service
public class SatelliteImageObjectAnalyzedServiceImpl implements SatelliteImageObjectAnalyzedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageObjectAnalyzedServiceImpl.class);

    @Autowired
    SatelliteImageObjectAnalyzedRepository satelliteImageObjectAnalyzedRepository;

    @Override
    public List<SatelliteImageObjectAnalyzed> satelliteImageUuidKey(String uuid) {
        return satelliteImageObjectAnalyzedRepository.satelliteImageUuidKey(uuid);
    }

    @Override
    public SatelliteImageObjectAnalyzed findByUuid(String uuid) {
        return satelliteImageObjectAnalyzedRepository.findByUuid(uuid);
    }

    @Override
    public SatelliteImageObjectAnalyzed delete(int isDeleted, String uuid) {
        return satelliteImageObjectAnalyzedRepository.delete(isDeleted, uuid);
    }

}
