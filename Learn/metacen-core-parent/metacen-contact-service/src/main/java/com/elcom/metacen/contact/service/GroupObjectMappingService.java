/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.GroupObjectMapping;
import com.elcom.metacen.contact.model.dto.GroupDTO.GroupObjectMappingDTO;

import java.util.List;
import java.util.UUID;


/**
 * @author hoangbd
 */
public interface GroupObjectMappingService {


    GroupObjectMapping save(GroupObjectMapping groupObjectMapping);

    int updateIsDelete(UUID groupId);

    List<GroupObjectMappingDTO> findAllByGroupId(UUID groupId);

    //GroupObjectMapping updateIsDelete();

//    List<GroupObjectMapping> saveAll();
//
//    People findById(UUID id);
//
//    People updatePeople(People people, PeopleDTO peopleDTO, String updateBy);
//
//    Page<People> findListPeople(Integer currentPage, Integer rowsPerPage);
//

}
