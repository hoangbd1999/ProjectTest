package com.elcom.metacen.contact.repository;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Admin
 * @param <E>
 */
public interface BaseCustomRepository<E extends Serializable> {

    UpdateResult updateFirst(Query query, Update update);

    UpdateResult updateMulti(Query query, Update update);

    Criteria getRSQLCriteria(String rsql, Class domain);

    List<E> searchAllByRsql(String rsql);

    Page<E> searchByRsql(String rsql, Pageable pageable);

    Page<E> searchAllByCriteria(Criteria criteria, Pageable pageable);
}
