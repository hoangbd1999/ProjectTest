/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.ObjectKeyword;
import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.model.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Admin
 */
public interface KeywordDataService {

    KeywordData insert(KeywordGrantRequestDTO keywordGrantRequestDTO, String keyWordId);

    KeywordData save(KeywordDataRequestDTO keywordDataRequestDTO);

    List<KeywordData> findByRefId(String refId, String keywordIds);

    List<KeywordData> findByRefId(String refId);

    List<KeywordData> findByRefIdAndType(String refId, Integer type);

    List<KeywordData> delete(List<KeywordData> keywordData);

    Page<KeywordDataObjectGeneralInfoDTO> getKeywordDataObject(KeyworDataObject keyworDataObject);

    KeywordData findByRefIdAndType(String refId, List<String> keywordIds, Integer type);

   // Object

    boolean save(KeywordDataDTO keywordDataDTO);

    boolean update(KeywordDataDTO keywordDataDTO);

    List<KeywordData> findByKeywordId(String keywordId);

}
