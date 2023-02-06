/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Group;
import com.elcom.metacen.contact.model.dto.GroupDTO.GroupDTO;
import com.elcom.metacen.contact.model.dto.GroupDTO.GroupFilterDTO;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface GroupService {

    Group save(GroupDTO groupDTO,String createBy);

    Group findById(String uuidKey);

    Group updateGroup(Group group, GroupDTO groupDTO, String updateBy);

    Page<Group> findListGroup(GroupFilterDTO groupFilterDTO);

    Group delete(Group group);

}
