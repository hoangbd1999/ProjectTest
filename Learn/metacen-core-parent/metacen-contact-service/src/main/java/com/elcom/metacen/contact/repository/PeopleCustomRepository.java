package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.repository.impl.BaseCustomRepositoryImpl;
import org.aspectj.weaver.ast.Test;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PeopleCustomRepository extends BaseCustomRepositoryImpl<People> {

    public List<People> findListPeople(List listId) {
        Criteria criteria;
        criteria = Criteria.where("uuidkey").in(listId);
        Query query = new Query().addCriteria(criteria);
        List<People> listPeople = mongoOps.find(query, domain);
        return listPeople;
    }

}
