package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.OtherVehicle;
import com.elcom.metacen.contact.model.dto.OtherVehicleFilterDTO;
import com.elcom.metacen.contact.model.dto.OtherVehicleResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author Admin
 */
public interface CustomOtherVehicleRepository extends BaseCustomRepository<OtherVehicle> {

    Page<OtherVehicleResponseDTO> search(OtherVehicleFilterDTO otherVehicleFilterDTO, Pageable pageable);

    OtherVehicleResponseDTO findOtherVehicleByUuid(String uuid);


}
