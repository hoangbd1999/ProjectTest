/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Keyword;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface KeywordRepository extends MongoRepository<Keyword, String> {

    Keyword findByUuidAndIsDeleted(String uuid, int isDeleted);

    Keyword findByNameAndIsDeleted(String name, int isDeleted);

    List<Keyword> findByUuidInAndIsDeleted(List<String> uuidLst, int isDeleted);

    Page<Keyword> findByIsDeleted(int isDeleted, Pageable pageable);

    List<Keyword> findAllByIsDeleted(int isDeleted);
}
