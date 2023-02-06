package com.elcom.metacen.mapping.data.repository;

import com.elcom.metacen.mapping.data.model.MappingAisMetacen;
import com.elcom.metacen.mapping.data.model.dto.MappingAisFilterDTO;
import com.elcom.metacen.mapping.data.model.dto.MappingAisResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Admin
 */
public interface CustomMappingAisMetacenRepository extends BaseCustomRepository<MappingAisMetacen> {
    Page<MappingAisResponseDTO> search(MappingAisFilterDTO mappingAisFilterDTO, Pageable pageable);
}
