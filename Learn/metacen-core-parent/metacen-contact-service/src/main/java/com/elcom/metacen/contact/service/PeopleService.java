/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.model.dto.PeopleRequestDTO;
import com.elcom.metacen.contact.model.dto.PeopleFilterDTO;
import com.elcom.metacen.contact.model.dto.PeopleResponseDTO;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface PeopleService {

    People save(PeopleRequestDTO peopleRequestDTO, String createBy);

    People findByUuid(String uuid);

    People updatePeople(People people, PeopleRequestDTO peopleRequestDTO, String modifiedBy);

    Page<PeopleResponseDTO> findListPeople(PeopleFilterDTO peopleFilterDTO);

    People delete(People people, String modifiedBy);

    PeopleResponseDTO findPeopleByUuid(String uuid);

}
