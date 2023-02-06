package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 *
 * @author Admin
 */
public interface CustomGroupRepository extends BaseCustomRepository<Group> {

    Page<Group> search(String term, Pageable pageable);

    Page<Group> findByCriteria(Criteria criteria, Pageable pageable);

}
