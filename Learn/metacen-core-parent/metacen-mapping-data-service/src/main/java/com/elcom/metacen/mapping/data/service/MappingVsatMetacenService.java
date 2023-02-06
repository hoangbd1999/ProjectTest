/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.mapping.data.service;

import com.elcom.metacen.mapping.data.model.MappingVsatMetacen;
import com.elcom.metacen.mapping.data.model.dto.MappingVsatFilterDTO;
import com.elcom.metacen.mapping.data.model.dto.MappingVsatRequestDTO;
import com.elcom.metacen.mapping.data.model.dto.MappingVsatResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Admin
 */
public interface MappingVsatMetacenService {

    MappingVsatMetacen save(MappingVsatRequestDTO mappingVsatRequestDTO, String createBy);

    MappingVsatMetacen findByUuid(String uuid);

    List<MappingVsatMetacen> findByObjectUuid(String objectUuid);

    MappingVsatMetacen updateMappingVsat(MappingVsatMetacen mappingVsatMetacen, MappingVsatRequestDTO mappingVsatRequestDTO, String modifiedBy);

    List<MappingVsatMetacen> updateNameObjectInternal(List<MappingVsatMetacen> mappingVsatMetacen,String objectName);

    Page<MappingVsatResponseDTO> findListMappingVsat(MappingVsatFilterDTO mappingVsatFilterDTO);

    MappingVsatMetacen delete(MappingVsatMetacen mappingVsatMetacen);

    MappingVsatMetacen checkExistMapping(MappingVsatRequestDTO mappingVsatRequestDTO);

    MappingVsatMetacen checkExistMappingByObjectUuid(MappingVsatRequestDTO mappingVsatRequestDTO);
}
