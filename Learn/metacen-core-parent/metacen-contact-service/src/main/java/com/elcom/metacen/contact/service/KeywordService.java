/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.dto.KeywordGrantRequestDTO;
import com.elcom.metacen.contact.model.dto.KeywordRequestDTO;
import com.elcom.metacen.contact.model.dto.KeywordFilterDTO;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface KeywordService {

    Page<Keyword> findAll(KeywordFilterDTO keywordFilterDTO);

    Keyword findById(String uuid);

    Keyword findByName(String uuid);

    Keyword save(KeywordRequestDTO keywordRequestDTO);

    Keyword insert(KeywordGrantRequestDTO keywordGrantRequestDTO);

    Keyword update(Keyword keyword);

    Keyword delete(Keyword keyword);

    List<Keyword> findKeywordsByUuidList(List<String> uuidLst);

    List<Keyword> findAll();
}
