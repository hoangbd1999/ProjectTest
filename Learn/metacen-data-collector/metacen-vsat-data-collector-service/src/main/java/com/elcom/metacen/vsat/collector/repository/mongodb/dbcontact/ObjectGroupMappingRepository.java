package com.elcom.metacen.vsat.collector.repository.mongodb.dbcontact;

import com.elcom.metacen.vsat.collector.model.mongodb.ObjectGroupMapping;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author Admin
 */
public interface ObjectGroupMappingRepository extends MongoRepository<ObjectGroupMapping, String> {

    List<ObjectGroupMapping> findObjectGroupMappingByTakedToSync(Integer takedToSync);
}
