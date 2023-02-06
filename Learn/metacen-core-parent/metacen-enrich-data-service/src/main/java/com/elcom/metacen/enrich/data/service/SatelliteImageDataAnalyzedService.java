/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.SatelliteImageDataAnalyzed;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageDataAnalyzedDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageDataAnalyzedFilterDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 *
 * @author Admin
 */
public interface SatelliteImageDataAnalyzedService {

    Page<SatelliteImageDataAnalyzedDTO> filterSatelliteImageData(SatelliteImageDataAnalyzedFilterDTO data);

    List<SatelliteImageDataAnalyzedDTO> filterSatelliteImageDataForMap(SatelliteImageDataAnalyzedFilterDTO data);
    
    SatelliteImageDataAnalyzed findByUuid(String uuid);

    SatelliteImageDataAnalyzed noteChange(int isNoted, String uuid);
}
