package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.Group;
import com.elcom.metacen.contact.repository.CustomGroupRepository;
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

/**
 *
 * @author Admin
 */
@Component
public class CustomGroupRepositoryImpl extends BaseCustomRepositoryImpl<Group> implements CustomGroupRepository {

    @Override
    public Page<Group> search(String term, Pageable pageable) {
        if (StringUtils.isBlank(term)) {
            return Page.empty();
        }

        term = ".*" + term.trim() + ".*";

        Criteria criteria;
        criteria = Criteria.where("is_deleted").is(DataDeleteStatus.NOT_DELETED.code());
        criteria.orOperator(
                Criteria.where("name").regex(term, "i"),
                Criteria.where("note").regex(term, "i"),
                Criteria.where("side_id").regex(term, "i"),
                Criteria.where("group_object").regex(term, "i")
        );

        Query query = new Query().with(pageable).addCriteria(criteria);

        List<Group> list = mongoOps.find(query, domain);
        Page<Group> page = PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoOps.count(Query.query(criteria).limit(-1).skip(-1), domain)
        );

        return page;
    }

    @Override
    public Page<Group> findByCriteria(Criteria criteria, Pageable pageable) {
        Query query = new Query().with(pageable).addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

        List<Group> list = mongoOps.find(query, domain);
        Page<Group> page = PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoOps.count(Query.query(criteria).limit(-1).skip(-1), domain)
        );

        return page;
    }
}
