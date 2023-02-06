/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Areas;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasFilterDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasRequestDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasResponseDTO;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface AreasService {

    Areas save(AreasRequestDTO areasRequestDTO, String createBy);

    Areas updateAreas(Areas areas,AreasRequestDTO areasRequestDTO,String modifiedBy);

    Areas findById(String uuid);

    Areas deleteAreas(Areas areas,String modifiedBy);

    AreasResponseDTO findAreasByUuid(String uuid);

    Page<AreasResponseDTO> findListAreas(AreasFilterDTO areasFilterDTO);

}
