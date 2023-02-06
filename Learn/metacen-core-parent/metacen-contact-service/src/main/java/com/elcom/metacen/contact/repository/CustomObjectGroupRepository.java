package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectGroup;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupConfirmedFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupUnconfirmedFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;

/**
 *
 * @author Admin
 */
public interface CustomObjectGroupRepository extends BaseCustomRepository<ObjectGroup> {

    Page<ObjectGroupResponseDTO> search(ObjectGroupUnconfirmedFilterDTO objectGroupUnconfirmedFilterDTO, Pageable pageable) throws ParseException;

    Page<ObjectGroupResponseDTO> search(ObjectGroupConfirmedFilterDTO objectGroupConfirmedFilterDTO, Pageable pageable) throws ParseException;

}
