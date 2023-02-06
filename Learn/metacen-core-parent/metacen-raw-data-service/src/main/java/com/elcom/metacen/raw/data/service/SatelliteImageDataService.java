/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.raw.data.service;

import com.elcom.metacen.raw.data.model.SatelliteImageData;
import com.elcom.metacen.raw.data.model.dto.SatelliteImageDataDTO;
import com.elcom.metacen.raw.data.model.dto.SatelliteImageDataFilterDTO;
import org.springframework.data.domain.Page;

/**
 *
 * @author Admin
 */
public interface SatelliteImageDataService {

    Page<SatelliteImageDataDTO> filterSatelliteImageData(SatelliteImageDataFilterDTO data);

    Page<SatelliteImageDataDTO> filterSatelliteImageDataForMap(SatelliteImageDataFilterDTO data);
    
    SatelliteImageData findByUuid(String uuid);
}
