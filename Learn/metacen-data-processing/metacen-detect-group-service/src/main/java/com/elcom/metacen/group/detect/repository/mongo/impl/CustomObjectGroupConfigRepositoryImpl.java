package com.elcom.metacen.group.detect.repository.mongo.impl;

import com.elcom.metacen.group.detect.model.ObjectGroupConfig;
import com.elcom.metacen.group.detect.repository.mongo.CustomObjectGroupConfigRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.elcom.metacen.group.detect.constant.ObjectGroupConfigConstant.IS_ACTIVE;


/**
 * @author Admin
 */
@Component
public class CustomObjectGroupConfigRepositoryImpl extends BaseCustomMongoRepositoryImpl<ObjectGroupConfig> implements CustomObjectGroupConfigRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomObjectGroupConfigRepositoryImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<ObjectGroupConfig> getActiveConfig() {
        Criteria criteria = new Criteria(IS_ACTIVE).is(1);
        MatchOperation matchQuery = new MatchOperation(criteria);
        Aggregation aggregation = Aggregation.newAggregation(matchQuery);
        return mongoOps.aggregate(aggregation, ObjectGroupConfig.class, ObjectGroupConfig.class)
                .getMappedResults();
    }
}
