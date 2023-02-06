package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 *
 * @author Admin
 */
public interface CustomKeywordRepository extends BaseCustomRepository<Keyword> {

    Page<Keyword> search(String term, Pageable pageable);

    Page<Keyword> findByCriteria(Criteria criteria, Pageable pageable);

}
