package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.MarineVesselInfo;
import com.elcom.metacen.contact.model.dto.MarineVesselFilterDTO;
import com.elcom.metacen.contact.model.dto.MarineVesselRequestDTO;
import com.elcom.metacen.contact.model.dto.MarineVesselResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hoangbd
 */
@Service
public interface MarineVesselInfoService {

    MarineVesselInfo save(MarineVesselRequestDTO marineVesselRequestDTO, String createBy);

    MarineVesselInfo findByMmsi(Long mmsi);

    MarineVesselInfo findById(String uuid);

    MarineVesselResponseDTO findMarineVesselByUuid(String uuid);

    MarineVesselInfo updateMarineVesselInfo(MarineVesselInfo marineVesselInfo,MarineVesselRequestDTO marineVesselRequestDTO,String modifiedBy);

    MarineVesselInfo delete(MarineVesselInfo marineVesselInfo, String modifiedBy);

    Page<MarineVesselResponseDTO> findListMarineVessel(MarineVesselFilterDTO marineVesselFilterDTO);

    List<MarineVesselInfo> getLstMarineVesselId(List<Integer> mmsiLst);
}
