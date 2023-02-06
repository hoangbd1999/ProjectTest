/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Organisation;
import com.elcom.metacen.contact.model.dto.OrganisationDTO;
import com.elcom.metacen.contact.model.dto.OrganisationRequestDTO;
import com.elcom.metacen.contact.model.dto.OrganisationFilterDTO;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface OrganisationService {

    Page<OrganisationDTO> findOrganisations(OrganisationFilterDTO organisationFilterDTO);

    OrganisationDTO findOrganisationByUuid(String uuid);

    Organisation findById(String uuid);

    Organisation save(OrganisationRequestDTO organisationRequestDTO, String username);

    Organisation update(Organisation organisation, OrganisationRequestDTO organisationRequestDTO, String username);

    Organisation delete(Organisation organisation, String username);

}
