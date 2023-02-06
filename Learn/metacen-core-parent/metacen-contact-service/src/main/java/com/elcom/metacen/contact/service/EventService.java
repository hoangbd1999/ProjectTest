/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Event;
import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventResponseDTO;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface EventService {

    Event save(EventRequestDTO eventRequestDTO, String createBy);

    Event updateEvent(Event event,EventRequestDTO eventRequestDTO,String modifiedBy);

    Page<EventResponseDTO> findListEvent(EventFilterDTO eventFilterDTO);

    EventResponseDTO findEventByUuid(String uuid);

    Event findById(String uuid);

    Event deleteEvent(Event event, String modifiedBy);

}
