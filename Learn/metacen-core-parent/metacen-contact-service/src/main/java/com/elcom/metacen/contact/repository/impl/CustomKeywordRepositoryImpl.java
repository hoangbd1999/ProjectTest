package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import java.util.List;
import com.elcom.metacen.contact.repository.CustomKeywordRepository;

/**
 *
 * @author Admin
 */
@Component
public class CustomKeywordRepositoryImpl extends BaseCustomRepositoryImpl<Keyword> implements CustomKeywordRepository {

    @Override
    public Page<Keyword> search(String term, Pageable pageable) {
        term = ".*" + term.trim() + ".*";

        Criteria criteria;
        criteria = Criteria.where("is_deleted").is(DataDeleteStatus.NOT_DELETED.code());
        criteria.orOperator(
                Criteria.where("name").regex(term, "i"),
                Criteria.where("description").regex(term, "i")
        );

        Query query = new Query().with(pageable).addCriteria(criteria);

        List<Keyword> list = mongoOps.find(query, domain);
        Page<Keyword> page = PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoOps.count(Query.query(criteria).limit(-1).skip(-1), domain)
        );

        return page;
    }

    @Override
    public Page<Keyword> findByCriteria(Criteria criteria, Pageable pageable) {
        Query query = new Query().with(pageable).addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

        List<Keyword> list = mongoOps.find(query, domain);
        Page<Keyword> page = PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoOps.count(Query.query(criteria).limit(-1).skip(-1), domain)
        );

        return page;
    }
}
