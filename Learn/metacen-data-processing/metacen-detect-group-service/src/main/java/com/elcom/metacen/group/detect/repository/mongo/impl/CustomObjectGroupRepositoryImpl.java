package com.elcom.metacen.group.detect.repository.mongo.impl;

import com.elcom.metacen.group.detect.model.ObjectGroup;
import com.elcom.metacen.group.detect.repository.mongo.CustomObjectGroupRepository;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CustomObjectGroupRepositoryImpl extends BaseCustomMongoRepositoryImpl<ObjectGroup> implements CustomObjectGroupRepository {

    @Override
    public List<ObjectGroup> findGroupOverPeriod(String id) {
        Criteria criteria = new Criteria("config_uuid").is(id)
                .and("is_deleted").is(0);
        Query query = new Query().addCriteria(criteria);
        return mongoContactOps.find(query, ObjectGroup.class);
    }

    @Override
    public Integer insert(List<ObjectGroup> objectGroups) {
        return mongoContactOps.insertAll(objectGroups).size();
    }

    @Override
    public Integer updateGroups(List<ObjectGroup> objectGroups) {
        List<Pair<Query, Update>> updateQueries = objectGroups.stream()
                .map(group -> {
                    Criteria updateQuery = new Criteria("uuid").is(group.getUuid());
                    Update update = new Update()
                            .set("updated_by", group.getUpdatedBy())
                            .set("updated_at", group.getUpdatedAt())
                            .set("last_together_time", group.getLastTogetherTime())
                            .set("mapping_pair_info", group.getMappingPairInfos());
                    return Pair.of(new Query().addCriteria(updateQuery), update);
                })
                .collect(Collectors.toList());
        return mongoContactOps.bulkOps(BulkOperations.BulkMode.UNORDERED, ObjectGroup.class)
                .updateOne(updateQueries)
                .execute().getModifiedCount();
    }
}
