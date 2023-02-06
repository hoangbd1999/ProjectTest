/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.ObjectKeyword;
import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface ObjectKeywordService {

    boolean save(KeywordDataDTO keywordDataDTO);

    boolean update(KeywordDataDTO keywordDataDTO);

    List<KeywordData> findByKeywordId(String keywordId);
}
