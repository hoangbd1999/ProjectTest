/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.ObjectKeyword;
import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import com.elcom.metacen.contact.repository.CustomObjectKeywordRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elcom.metacen.contact.service.ObjectKeywordService;
import com.elcom.metacen.utils.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Admin
 */
@Service
public class ObjectKeywordServiceImpl implements ObjectKeywordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectKeywordServiceImpl.class);

    @Autowired
    KeywordDataRepository keywordDataRepository;

    @Autowired
    CustomObjectKeywordRepository customObjectKeywordRepository;

    @Override
    public boolean save(KeywordDataDTO keywordDataDTO) {
        try {
            List<String> keywordIds = keywordDataDTO.getKeywordIds();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                List<KeywordData> keywordDataList = new ArrayList<>();
                Date now = new Date();
                for (String keywordId : keywordIds) {
                    KeywordData keywordData = new KeywordData();
                    keywordData.setUuid(UUID.randomUUID().toString());
                    keywordData.setRefId(keywordDataDTO.getRefId());
                    keywordData.setRefType(keywordDataDTO.getRefType());
                    keywordData.setType(1);
                    keywordData.setKeywordId(keywordId);
                    keywordData.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    keywordData.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    keywordDataList.add(keywordData);
                }

                keywordDataRepository.saveAll(keywordDataList);
            }

            return true;
        } catch (Exception ex) {
            LOGGER.error("Save object keyword failed >>> {}", ex.toString());
            return false;
        }
    }

    @Override
    public boolean update(KeywordDataDTO keywordDataDTO) {
        try {
            // delete object keyword
            deleteObjectKeyword(keywordDataDTO.getRefId());

            // insert object keyword
            List<String> keywordIds = keywordDataDTO.getKeywordIds();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                List<KeywordData> keywordDataList = new ArrayList<>();
                Date now = new Date();
                for (String keywordId : keywordIds) {
                    KeywordData keywordData = new KeywordData();
                    keywordData.setUuid(UUID.randomUUID().toString());
                    keywordData.setRefId(keywordDataDTO.getRefId());
                    keywordData.setRefType(keywordDataDTO.getRefType());
                    keywordData.setType(1);
                    keywordData.setKeywordId(keywordId);
                    keywordData.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    keywordData.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    keywordDataList.add(keywordData);
                }

                keywordDataRepository.saveAll(keywordDataList);
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Update object keyword failed >>> {}", ex.toString());
            return false;
        }
    }

    @Override
    public List<KeywordData> findByKeywordId(String keywordId) {
        return keywordDataRepository.findByKeywordId(keywordId);
    }

    private void deleteObjectKeyword(String objectId) {
        keywordDataRepository.deleteByRefId(objectId);
    }

}
