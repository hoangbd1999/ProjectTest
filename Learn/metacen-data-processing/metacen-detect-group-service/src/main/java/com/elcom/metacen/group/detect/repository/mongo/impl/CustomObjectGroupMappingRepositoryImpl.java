package com.elcom.metacen.group.detect.repository.mongo.impl;

import com.elcom.metacen.group.detect.model.ObjectGroupMapping;
import com.elcom.metacen.group.detect.repository.mongo.CustomObjectGroupMappingRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomObjectGroupMappingRepositoryImpl extends BaseCustomMongoRepositoryImpl<ObjectGroupMapping> implements CustomObjectGroupMappingRepository {
    @Override
    public List<ObjectGroupMapping> getObjectGroupMappingByObjectGroupUuid(List<String> uuids) {
        Criteria criteria = new Criteria("group_id").in(uuids);
        Query query = new Query().addCriteria(criteria);
        return mongoContactOps.find(query, ObjectGroupMapping.class);
    }

    @Override
    public Integer insert(List<ObjectGroupMapping> objectGroupMappings) {
        return mongoContactOps.insertAll(objectGroupMappings).size();
    }

//    @Override
//    public Integer updateGroups(List<ObjectGroupMapping> objectGroupMappings) {
//        List<Pair<Query, Update>> updateQueries = objectGroupMappings.stream()
//                .map(group -> {
//                    Criteria updateQuery = new Criteria("uuid").is(group.getUuid());
//                    Update update = new Update()
//                            .set("updated_by", group.getUpdatedBy())
//                            .set("updated_at", group.getUpdatedAt());
//                    return Pair.of(new Query().addCriteria(updateQuery), update);
//                })
//                .collect(Collectors.toList());
//        return mongoOps.bulkOps(BulkOperations.BulkMode.UNORDERED, ObjectGroup.class)
//                .updateOne(updateQueries)
//                .execute().getModifiedCount();
//    }
}
