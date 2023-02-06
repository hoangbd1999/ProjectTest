package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Infrastructure;
import com.elcom.metacen.contact.model.dto.InfrastructureFilterDTO;
import com.elcom.metacen.contact.model.dto.InfrastructureResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author Admin
 */
public interface CustomInfrastructureRepository extends BaseCustomRepository<Infrastructure> {

    Page<InfrastructureResponseDTO> search(InfrastructureFilterDTO infrastructureFilterDTO, Pageable pageable);

    InfrastructureResponseDTO findInfrastructureByUuid(String uuid);


}
