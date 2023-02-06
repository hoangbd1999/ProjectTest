package com.elcom.metacen.group.detect.repository.mongo;

import com.elcom.metacen.group.detect.model.ObjectGroupConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 *
 * @author Admin
 */
@Repository
public interface ObjectGroupConfigRepository extends MongoRepository<ObjectGroupConfig, String> {
}
