package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.model.dto.PeopleFilterDTO;
import com.elcom.metacen.contact.model.dto.PeopleResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author Admin
 */
public interface CustomPeopleRepository extends BaseCustomRepository<People> {

    Page<PeopleResponseDTO> search(PeopleFilterDTO peopleFilterDTO, Pageable pageable);

    PeopleResponseDTO findPeopleByUuid(String uuid);


}
