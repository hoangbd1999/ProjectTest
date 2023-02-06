package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.MarineVesselInfo;
import com.elcom.metacen.contact.repository.impl.BaseCustomRepositoryImpl;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MarineVesselInfoCustomRepository extends BaseCustomRepositoryImpl<MarineVesselInfo> {
    public List<MarineVesselInfo> findListMarineVessel(List listId) {
        Criteria criteria;
        criteria = Criteria.where("uuidkey").in(listId);
        Query query = new Query().addCriteria(criteria);
        List<MarineVesselInfo> listMarine = mongoOps.find(query, domain);
        return listMarine;
    }
}
