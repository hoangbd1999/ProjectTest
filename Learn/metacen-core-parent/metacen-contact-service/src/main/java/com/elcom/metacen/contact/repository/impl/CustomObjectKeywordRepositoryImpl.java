package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.ObjectKeyword;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.List;
import com.elcom.metacen.contact.repository.CustomObjectKeywordRepository;

/**
 *
 * @author Admin
 */
@Component
public class CustomObjectKeywordRepositoryImpl extends BaseCustomRepositoryImpl<ObjectKeyword> implements CustomObjectKeywordRepository {

    @Override
    public List<ObjectKeyword> search(String objectId) {
        Criteria criteria;
        criteria = Criteria.where("is_deleted").is(DataDeleteStatus.NOT_DELETED.code());
        criteria.andOperator(Criteria.where("object_id").is(objectId));

        Query query = new Query().addCriteria(criteria);
        List<ObjectKeyword> results = mongoOps.find(query, domain);
        return results;
    }
}
