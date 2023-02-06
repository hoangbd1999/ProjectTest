/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service.impl;


import com.elcom.metacen.enrich.data.model.SatelliteImageChangesResult;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeResultDTO;
import com.elcom.metacen.enrich.data.repository.SatelliteImageChangeResultRepository;
import com.elcom.metacen.enrich.data.service.SatelliteImageChangeResultService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;


/**
 *
 * @author Admin
 */
@Service
public class SatelliteImageChangeResultServiceImpl implements SatelliteImageChangeResultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageChangeResultServiceImpl.class);

    @Autowired
    SatelliteImageChangeResultRepository satelliteImageChangeResultRepository;

    @Override
    public List<SatelliteImageChangeResultDTO> findByUuid(String uuid) {
        return satelliteImageChangeResultRepository.findByUuid(uuid);
    }

}
