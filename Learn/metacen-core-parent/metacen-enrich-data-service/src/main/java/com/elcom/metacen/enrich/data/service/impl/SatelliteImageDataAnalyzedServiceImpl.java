/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service.impl;

import com.elcom.metacen.enrich.data.model.SatelliteImageDataAnalyzed;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageDataAnalyzedDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageDataAnalyzedFilterDTO;
import com.elcom.metacen.enrich.data.repository.SatelliteImageDataAnalyzedRepository;
import com.elcom.metacen.enrich.data.service.SatelliteImageDataAnalyzedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author Admin
 */
@Service
public class SatelliteImageDataAnalyzedServiceImpl implements SatelliteImageDataAnalyzedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageDataAnalyzedServiceImpl.class);

    @Autowired
    SatelliteImageDataAnalyzedRepository satelliteImageDataAnalyzedRepository;

    @Override
    public Page<SatelliteImageDataAnalyzedDTO> filterSatelliteImageData(SatelliteImageDataAnalyzedFilterDTO data) {
        return satelliteImageDataAnalyzedRepository.filterSatelliteImageData(data);
    }
    
    @Override
    public List<SatelliteImageDataAnalyzedDTO> filterSatelliteImageDataForMap(SatelliteImageDataAnalyzedFilterDTO data) {
        return satelliteImageDataAnalyzedRepository.filterSatelliteImageDataForMap(data);
    }

    @Override
    public SatelliteImageDataAnalyzed findByUuid(String uuid) {
        return satelliteImageDataAnalyzedRepository.findByUuid(uuid);
    }

    @Override
    public SatelliteImageDataAnalyzed noteChange(int isNoted, String uuid) {
        try {
            return satelliteImageDataAnalyzedRepository.noteChange(isNoted, uuid);
        } catch (Exception e) {
            LOGGER.error("Change failed >>> {}", e.toString());
            return null;
        }
    }

}
