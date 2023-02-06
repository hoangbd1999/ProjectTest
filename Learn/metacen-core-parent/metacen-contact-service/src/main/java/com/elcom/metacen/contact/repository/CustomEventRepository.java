package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Event;
import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author hoangbd
 */
public interface CustomEventRepository extends BaseCustomRepository<Event> {

    Page<EventResponseDTO> search(EventFilterDTO eventFilterDTO, Pageable pageable);

    EventResponseDTO findEventByUuid(String uuid);

}
