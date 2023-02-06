/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.SatelliteImageChanges;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeFilterDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeRequestDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeResponseDTO;
import org.springframework.data.domain.Page;


/**
 *
 * @author Admin
 */
public interface SatelliteImageChangeService {

    SatelliteImageChanges save (SatelliteImageChangeRequestDTO satelliteImageChangeRequestDTO);

    Page<SatelliteImageChangeResponseDTO> filterSatelliteImageChange(SatelliteImageChangeFilterDTO data);

    SatelliteImageChanges findByUuid(String uuid);

    SatelliteImageChanges delete(int isDeleted, String uuid);
}
