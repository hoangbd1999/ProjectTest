/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Infrastructure;
import com.elcom.metacen.contact.model.dto.*;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface InfrastructureService {

    Infrastructure save(InfrastructureRequestDTO infrastructureRequestDTO, String createBy);

    Infrastructure findByUuid(String uuid);

    Infrastructure updateInfrastructure(Infrastructure infrastructure, InfrastructureRequestDTO infrastructureRequestDTO, String modifiedBy);

    Page<InfrastructureResponseDTO> findListInfrastructure(InfrastructureFilterDTO infrastructureFilterDTO);

    Infrastructure delete(Infrastructure infrastructure, String modifiedBy);

    InfrastructureResponseDTO findInfrastructureByUuid(String uuid);

}
