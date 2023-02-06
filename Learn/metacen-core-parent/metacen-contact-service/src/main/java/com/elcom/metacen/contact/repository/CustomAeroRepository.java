package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.AeroAirplaneInfo;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroFilterDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Admin
 */
public interface CustomAeroRepository extends BaseCustomRepository<AeroAirplaneInfo> {

    Page<AeroResponseDTO> search(AeroFilterDTO aeroFilterDTO, Pageable pageable);

    AeroResponseDTO findAeroByUuid(String uuid);
}
