package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.DataCollectorConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataCollectionConfigRepository extends CrudRepository<DataCollectorConfig, String> {
    DataCollectorConfig findByCollectType(String collectType);
}
