/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;


import com.elcom.metacen.contact.model.dto.GroupDTO.GroupFilterDTO;
import com.elcom.metacen.contact.repository.CustomGroupRepository;
import com.elcom.metacen.contact.repository.GroupCustomRepository;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.contact.model.Group;
import com.elcom.metacen.contact.model.dto.GroupDTO.GroupDTO;
import com.elcom.metacen.contact.repository.GroupRepository;
import com.elcom.metacen.contact.service.GroupService;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.StringUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author hoangbd
 */
@Service
public class GroupServiceImpl implements GroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupCustomRepository groupCustomRepository;

    @Autowired
    CustomGroupRepository customGroupRepository;


    @Override
    public Group save(GroupDTO groupDTO,String createBy) {
        try {
            Group group = modelMapper.map(groupDTO, Group.class);
            group.setUuidKey(UUID.randomUUID().toString());
            group.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            group.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            group.setCreatedBy(createBy);
            group.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());

            Group response = groupRepository.save(group);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save group failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Group findById(String uuidKey) {
        Group group = groupRepository.findByUuidKeyAndIsDeleted(uuidKey, DataDeleteStatus.NOT_DELETED.code());
        return group;
    }

    @Override
    public Group updateGroup(Group group, GroupDTO groupDTO, String updateBy) {
        try {
            if (!StringUtil.isNullOrEmpty(groupDTO.getName())) {
                group.setName(groupDTO.getName());
            }
            group.setNote(groupDTO.getNote());
            group.setSideId(groupDTO.getSideId());
            if (!groupDTO.getGroupObject().isEmpty()) {
                group.setGroupObject(groupDTO.getGroupObject());
            }
            group.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            group.setModifiedBy(updateBy);
            Group response = groupRepository.save(group);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update group failed >>> {}", ex.toString());
            return null;
        }
    }


    @Override
    public Page<Group> findListGroup(GroupFilterDTO groupFilterDTO) {
        Integer page = groupFilterDTO.getPage() > 0 ? groupFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, groupFilterDTO.getSize());

        Page<Group> groupList = null;
        if (StringUtil.isNullOrEmpty(groupFilterDTO.getTerm())) {
            groupList = groupRepository.findByIsDeleted(pageable, DataDeleteStatus.NOT_DELETED.code());
        } else {
            groupList = customGroupRepository.search(groupFilterDTO.getTerm(), pageable);
        }

        return groupList;
    }

    @Override
    public Group delete(Group group) {
        group.setIsDeleted(DataDeleteStatus.DELETED.code());
        return groupRepository.save(group);
    }
}
