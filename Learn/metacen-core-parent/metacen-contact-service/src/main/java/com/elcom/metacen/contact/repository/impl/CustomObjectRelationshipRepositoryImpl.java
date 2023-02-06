package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.ObjectRelationship;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.List;
import com.elcom.metacen.contact.repository.CustomObjectRelationshipRepository;

/**
 *
 * @author Admin
 */
@Component
class CustomObjectRelationshipRepositoryImpl extends BaseCustomRepositoryImpl<ObjectRelationship> implements CustomObjectRelationshipRepository {

    @Override
    public List<ObjectRelationship> search(String sourceObjectId) {
        Criteria criteria;
        criteria = Criteria.where("is_deleted").is(DataDeleteStatus.NOT_DELETED.code());
        criteria.andOperator(Criteria.where("source_object_id").is(sourceObjectId));

        Query query = new Query().addCriteria(criteria);
        List<ObjectRelationship> results = mongoOps.find(query, domain);
        return results;
    }
}