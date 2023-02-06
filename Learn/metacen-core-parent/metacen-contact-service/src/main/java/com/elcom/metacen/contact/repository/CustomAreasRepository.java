package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Areas;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasFilterDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author hoangbd
 */
public interface CustomAreasRepository extends BaseCustomRepository<Areas> {

    Page<AreasResponseDTO> search(AreasFilterDTO areasFilterDTO, Pageable pageable);

    AreasResponseDTO findAreasByUuid(String uuid);


}
