package com.elcom.metacen.group.detect.repository.mongo;

import com.elcom.metacen.group.detect.model.ObjectGroupGeneralConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectGroupGeneralConfigRepository extends MongoRepository<ObjectGroupGeneralConfig, String> {
}
