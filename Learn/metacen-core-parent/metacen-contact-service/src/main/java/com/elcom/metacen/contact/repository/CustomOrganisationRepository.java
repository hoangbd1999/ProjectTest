package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Organisation;
import com.elcom.metacen.contact.model.dto.OrganisationDTO;
import com.elcom.metacen.contact.model.dto.OrganisationFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Admin
 */
public interface CustomOrganisationRepository extends BaseCustomRepository<Organisation> {

    Page<OrganisationDTO> search(OrganisationFilterDTO organisationFilterDTO, Pageable pageable);

    OrganisationDTO findByUuid(String uuid);
}
