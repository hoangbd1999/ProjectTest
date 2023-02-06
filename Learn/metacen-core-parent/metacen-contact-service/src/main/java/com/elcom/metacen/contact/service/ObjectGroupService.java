/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.ObjectGroup;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupConfirmedFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupUnconfirmedFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupRequestDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;


/**
 * @author Admin
 */
public interface ObjectGroupService {

    Page<ObjectGroupResponseDTO> findListObjectGroupUnconfirmed(ObjectGroupUnconfirmedFilterDTO objectGroupUnconfirmedFilterDTO);

    Page<ObjectGroupResponseDTO> findListObjectGroupConfirmed(ObjectGroupConfirmedFilterDTO objectGroupConfirmedFilterDTO);

    ObjectGroup findByUuid(String uuid);

    ObjectGroup findByName(String uuid);

    List<ObjectGroup> findByConfigUuid(String configUuid);

    ObjectGroup delete(ObjectGroup objectGroup);

    ObjectGroup update(ObjectGroup objectGroup, ObjectGroupRequestDTO objectGroupRequestDTO, String modifiedBy);

    ObjectGroup updateObjectGroupName(ObjectGroup objectGroup, String modifiedBy, String name);

}
