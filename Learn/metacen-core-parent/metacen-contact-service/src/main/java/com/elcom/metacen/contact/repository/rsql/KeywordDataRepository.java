/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository.rsql;

import com.elcom.metacen.contact.model.KeywordData;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Admin
 */
@Repository
public interface KeywordDataRepository extends MongoRepository<KeywordData, String> {

    List<KeywordData> findByRefIdAndKeywordId(String refId, String keywordIds);

    List<KeywordData> findByRefId(String refId);

    List<KeywordData> findByRefIdAndType(String refId, Integer type);

    KeywordData findByRefIdAndKeywordIdInAndType(String refId, List<String> keywordIds, Integer type);

    List<KeywordData> findByKeywordId(String keywordId);

    List<KeywordData> findByRefIdIn(List<String> refIds);

   // List<KeywordData> findByRefIdIn(String refIds);

    @DeleteQuery
    void deleteByRefId(String refId);

}
