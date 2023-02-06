package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.MarineVesselInfo;
import com.elcom.metacen.contact.model.dto.MarineVesselFilterDTO;
import com.elcom.metacen.contact.model.dto.MarineVesselResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 *
 * @author hoangbd
 */
public interface CustomMarineVesselRepository extends BaseCustomRepository<MarineVesselInfo> {

    Page<MarineVesselResponseDTO> search(MarineVesselFilterDTO marineVesselFilterDTO, Pageable pageable);

    MarineVesselResponseDTO findMarineVesselByUuid(String uuid);

    List<MarineVesselInfo> findListMarineVessel(List<Integer> listId);
}
