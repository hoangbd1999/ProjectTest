package com.elcom.metacen.vsat.collector.repository.mongodb.dbconfig;

import com.elcom.metacen.vsat.collector.model.mongodb.DataCollectorConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author Admin
 */
public interface DataCollectorConfigRepository extends MongoRepository<DataCollectorConfig, String> {

    DataCollectorConfig findDataCollectorConfigByCollectType(String collectType);
}
