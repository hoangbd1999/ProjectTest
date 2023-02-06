/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;


import com.elcom.metacen.contact.model.GroupObjectMapping;
import com.elcom.metacen.contact.model.dto.GroupDTO.GroupObjectMappingDTO;
import com.elcom.metacen.contact.repository.GroupObjectMappingRepository;
import com.elcom.metacen.contact.service.GroupObjectMappingService;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author hoangbd
 */
@Service
public class GroupObjectMapingServiceImpl implements GroupObjectMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    GroupObjectMappingRepository groupObjectMappingRepository;

    @Override
    public GroupObjectMapping save(GroupObjectMapping groupObjectMapping) {
        return groupObjectMappingRepository.save(groupObjectMapping);
    }

    @Override
    public int updateIsDelete(UUID groupId) {
       return groupObjectMappingRepository.delete(groupId);
    }

    @Override
    public List<GroupObjectMappingDTO> findAllByGroupId(UUID groupId) {
        List<GroupObjectMapping> groupObjectMapping = groupObjectMappingRepository.findAllByGroupIdAndIsDeleted(groupId, DataDeleteStatus.NOT_DELETED.code());
        List<GroupObjectMappingDTO> list = new ArrayList<>();;
        if (groupObjectMapping != null && groupObjectMapping.size() > 0) {
            groupObjectMapping.stream().forEach(loop ->
                    list.add(modelMapper.map(loop, GroupObjectMappingDTO.class)));
        }
        return list;
    }

}
