package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.AeroAirplaneInfo;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroFilterDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroInsertRequestDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroResponseDTO;
import org.springframework.data.domain.Page;

public interface AeroService {

    AeroAirplaneInfo insert(AeroInsertRequestDTO aeroInsertRequestDTO, String userName);

    AeroAirplaneInfo findByUuid(String uuid);

    AeroAirplaneInfo delete(AeroAirplaneInfo aeroAirplaneInfo, String modifiedBy);

    AeroAirplaneInfo update(AeroAirplaneInfo aeroAirplaneInfo, AeroInsertRequestDTO aeroInsertRequestDTO, String modifiedBy);

    Page<AeroResponseDTO> findListAero(AeroFilterDTO aeroFilterDTO);

    AeroResponseDTO findAeroByUuid(String uuid);
}
