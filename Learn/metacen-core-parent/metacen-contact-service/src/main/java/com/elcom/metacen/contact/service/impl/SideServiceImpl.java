/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.dto.SideFilterDTO;
import com.elcom.metacen.contact.repository.CustomSideRepository;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.contact.model.Side;
import com.elcom.metacen.contact.model.dto.SideDTO;
import com.elcom.metacen.contact.repository.SideRepository;
import com.elcom.metacen.contact.service.SideService;
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
public class SideServiceImpl implements SideService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SideServiceImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    SideRepository sideRepository;

    @Autowired
    CustomSideRepository customSideRepository;

    @Override
    public Side save(SideDTO sideDTO) {
        try {
            Side side = modelMapper.map(sideDTO, Side.class);
            side.setUuidKey(UUID.randomUUID().toString());
            side.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            side.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            side.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            Side response = sideRepository.save(side);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save side failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Side findById(String uuidKey) {
        Side side = sideRepository.findByUuidKeyAndIsDeleted(uuidKey, DataDeleteStatus.NOT_DELETED.code());
        return side;
    }

    @Override
    public Side updateSide(Side side, SideDTO sideDTO) {
        try {
            side.setName(sideDTO.getName());
            side.setNote(sideDTO.getNote());
            side.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
            Side response = sideRepository.save(side);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update side failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<Side> findListSide(SideFilterDTO sideFilterDTO) {
        Integer page = sideFilterDTO != null && sideFilterDTO.getPage() != null && sideFilterDTO.getPage() > 0 ? sideFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, sideFilterDTO != null && sideFilterDTO.getSize() != null && sideFilterDTO.getSize() > 0 ? sideFilterDTO.getSize(): 0);

        Page<Side> sideList = null;
        if (StringUtil.isNullOrEmpty(sideFilterDTO.getTerm())) {
            sideList = sideRepository.findByIsDeleted(pageable, DataDeleteStatus.NOT_DELETED.code());
        } else {
            sideList = customSideRepository.search(sideFilterDTO.getTerm(), pageable);
        }

        return sideList;
    }

    @Override
    public Side delete(Side side) {
        side.setIsDeleted(DataDeleteStatus.DELETED.code());
        return sideRepository.save(side);
    }
}
