/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.dto.KeywordGrantRequestDTO;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.dto.KeywordRequestDTO;
import com.elcom.metacen.contact.model.dto.KeywordFilterDTO;
import com.elcom.metacen.contact.repository.KeywordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elcom.metacen.contact.repository.CustomKeywordRepository;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.StringUtil;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author Admin
 */
@Service
public class KeywordServiceImpl implements KeywordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordServiceImpl.class);

    @Autowired
    KeywordRepository keywordRepository;

    @Autowired
    CustomKeywordRepository customKeywordRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<Keyword> findAll(KeywordFilterDTO keywordFilterDTO) {
        Integer page = keywordFilterDTO.getPage() > 0 ? keywordFilterDTO.getPage() : 0;
        Pageable pageable = PageRequest.of(page, keywordFilterDTO.getSize());

        Page<Keyword> keywordList = null;
        if (StringUtil.isNullOrEmpty(keywordFilterDTO.getTerm())) {
            keywordList = keywordRepository.findByIsDeleted(DataDeleteStatus.NOT_DELETED.code(), pageable);
        } else {
            keywordList = customKeywordRepository.search(keywordFilterDTO.getTerm(), pageable);
        }

        return keywordList;
    }

    @Override
    public Keyword findById(String uuid) {
        Keyword keyword = keywordRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        return keyword;
    }

    @Override
    public Keyword findByName(String name) {
        Keyword keyword = keywordRepository.findByNameAndIsDeleted(name, DataDeleteStatus.NOT_DELETED.code());
        return keyword;
    }

    @Override
    public Keyword save(KeywordRequestDTO keywordRequestDTO) {
        try {
            Keyword keyword = modelMapper.map(keywordRequestDTO, Keyword.class);
            keyword.setUuid(UUID.randomUUID().toString());
            keyword.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            keyword.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            keyword.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            Keyword response = keywordRepository.save(keyword);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save keyword failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Keyword insert(KeywordGrantRequestDTO keywordGrantRequestDTO) {
        try {
            Keyword keyword = new Keyword();
            keyword.setUuid(UUID.randomUUID().toString());
            keyword.setName(keywordGrantRequestDTO.getName());
            keyword.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());
            keyword.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            keyword.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            Keyword response = keywordRepository.save(keyword);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save keyword failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Keyword update(Keyword keyword) {
        try {
            keyword.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            Keyword response = keywordRepository.save(keyword);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update keyword failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Keyword delete(Keyword keyword) {
        try {
            keyword.setIsDeleted(DataDeleteStatus.DELETED.code());
            keyword.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            Keyword response = keywordRepository.save(keyword);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Delete keyword failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public List<Keyword> findKeywordsByUuidList(List<String> uuidLst) {
        return keywordRepository.findByUuidInAndIsDeleted(uuidLst, DataDeleteStatus.NOT_DELETED.code());
    }

    @Override
    public List<Keyword> findAll() {
        return keywordRepository.findAllByIsDeleted(DataDeleteStatus.NOT_DELETED.code());
    }
}
