package com.elcom.metacen.vsat.collector.service;

import com.elcom.metacen.vsat.collector.model.mongodb.DataCollectorConfig;
import com.elcom.metacen.vsat.collector.model.mongodb.ObjectGroupMapping;
import java.util.List;

/**
 *
 * @author anhdv
 */
public interface DataCollectorService {
    
    /** Get config value by collect type, string value return is represent with JSON format
     * @param collectType
     * @return String */
    public DataCollectorConfig getConfigValue(String collectType);
    
    public List<ObjectGroupMapping> findObjectGroupMappingByTakedToSync(Integer takedToSync);
    
//    public boolean sinkLstObjectGroupMappingToDimTable(List<ObjectGroupMapping> ObjectGroupMapping);
    
    public boolean updateTakedToSyncForLstObjectGroupMapping(List<ObjectGroupMapping> objectGroupMapping);
}
