package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.ObjectGroupMapping;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupMappingDTO;

import com.elcom.metacen.contact.repository.CustomObjectGroupMappingRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;


import java.util.List;


import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;


/**
 * @author Admin
 */
@Component
public class CustomObjectGroupMappingRepositoryImpl extends BaseCustomRepositoryImpl<ObjectGroupMapping> implements CustomObjectGroupMappingRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomObjectGroupMappingRepositoryImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<ObjectGroupMappingDTO> findByObjIdAndObjName(String term) {
        Criteria criteria;
        criteria = Criteria.where("objId").ne(null);

        Criteria termCriteria = new Criteria();
        termCriteria.orOperator(
                    Criteria.where("objName").regex(term, "i"),
                    Criteria.where("objId").regex(term, "i")
            );
        MatchOperation match = Aggregation.match(criteria);
        MatchOperation matchTermStage = Aggregation.match(termCriteria);
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                project("objId", "objName", "objTypeId", "objNote", "groupId", "isDeleted", "takedToSync", "createdTime", "updatedTime"),
                matchTermStage);
        AggregationResults<ObjectGroupMappingDTO> output = mongoOps.aggregate(aggregation, ObjectGroupMapping.class, ObjectGroupMappingDTO.class);
        List<ObjectGroupMappingDTO> results = output.getMappedResults();
        return results;
    }

}
