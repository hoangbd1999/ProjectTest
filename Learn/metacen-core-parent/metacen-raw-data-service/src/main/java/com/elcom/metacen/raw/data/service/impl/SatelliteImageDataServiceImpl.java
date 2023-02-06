/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.raw.data.service.impl;

import com.elcom.metacen.raw.data.model.SatelliteImageData;
import com.elcom.metacen.raw.data.model.dto.SatelliteImageDataDTO;
import com.elcom.metacen.raw.data.model.dto.SatelliteImageDataFilterDTO;
import com.elcom.metacen.raw.data.repository.SatelliteImageDataRepository;
import com.elcom.metacen.raw.data.service.SatelliteImageDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class SatelliteImageDataServiceImpl implements SatelliteImageDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageDataServiceImpl.class);

    @Autowired
    SatelliteImageDataRepository satelliteImageDataRepository;

    @Override
    public Page<SatelliteImageDataDTO> filterSatelliteImageData(SatelliteImageDataFilterDTO data) {
        return satelliteImageDataRepository.filterSatelliteImageData(data);
    }
    
    @Override
    public Page<SatelliteImageDataDTO> filterSatelliteImageDataForMap(SatelliteImageDataFilterDTO data) {
        return satelliteImageDataRepository.filterSatelliteImageDataForMap(data);
    }

    @Override
    public SatelliteImageData findByUuid(String uuid) {
        return satelliteImageDataRepository.findByUuid(uuid);
    }

}
