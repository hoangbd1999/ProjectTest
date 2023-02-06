/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.SatelliteImageChangesResult;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeResultDTO;

import java.util.List;


/**
 *
 * @author Admin
 */
public interface SatelliteImageChangeResultService {

    List<SatelliteImageChangeResultDTO> findByUuid(String uuid);

}
