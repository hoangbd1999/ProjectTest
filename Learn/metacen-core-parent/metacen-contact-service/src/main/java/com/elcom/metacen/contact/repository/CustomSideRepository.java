package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Side;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 *
 * @author Admin
 */
public interface CustomSideRepository extends BaseCustomRepository<Side> {

    Page<Side> search(String term, Pageable pageable);

    Page<Side> findByCriteria(Criteria criteria, Pageable pageable);

}
