/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.OtherVehicle;
import com.elcom.metacen.contact.model.dto.*;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface OtherVehicleService {

    OtherVehicle save(OtherVehicleRequestDTO otherVehicleRequestDTO, String createBy);

    OtherVehicle findByUuid(String uuid);

    OtherVehicle updateOtherVehicle(OtherVehicle otherVehicle, OtherVehicleRequestDTO otherVehicleRequestDTO, String modifiedBy);

    Page<OtherVehicleResponseDTO> findListOtherVehicle(OtherVehicleFilterDTO otherVehicleFilterDTO);

    OtherVehicle delete(OtherVehicle otherVehicle, String modifiedBy);

    OtherVehicleResponseDTO findOtherVehicleByUuid(String uuid);

}
