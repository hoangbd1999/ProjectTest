package com.elcom.metacen.vsat.collector.service.impl;

import com.elcom.metacen.enums.DataSynkStatus;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.vsat.collector.model.mongodb.DataCollectorConfig;
import com.elcom.metacen.vsat.collector.model.mongodb.ObjectGroupMapping;
import com.elcom.metacen.vsat.collector.repository.mongodb.dbconfig.DataCollectorConfigRepository;
import com.elcom.metacen.vsat.collector.repository.mongodb.dbcontact.ObjectGroupMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elcom.metacen.vsat.collector.service.DataCollectorService;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author anhdv
 */
@Service
@SuppressWarnings("unchecked")
public class DataCollectorServiceImpl implements DataCollectorService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCollectorServiceImpl.class);

    @Autowired
    private DataCollectorConfigRepository dataCollectorConfigRepository;
    
    @Autowired
    private ObjectGroupMappingRepository objectGroupMappingRepository;
    
//    @Autowired
//    private CustomizeRepository clickhouseCustomizeRepository;
    
    /** Get config value by collect type, string value return is represent with JSON format
     * @param collectType
     * @return String */
    @Override
    public DataCollectorConfig getConfigValue(String collectType) {
        return this.dataCollectorConfigRepository.findDataCollectorConfigByCollectType(collectType);
    }
    
    @Override
    public List<ObjectGroupMapping> findObjectGroupMappingByTakedToSync(Integer takedToSync) {
        return this.objectGroupMappingRepository.findObjectGroupMappingByTakedToSync(takedToSync);
    }
    
//    @Override
//    public boolean sinkLstObjectGroupMappingToDimTable(List<ObjectGroupMapping> objectGroupMappings) {
//        return this.clickhouseCustomizeRepository.sinkLstObjectGroupMappingToDimTable(objectGroupMappings);
//    }
    
    @Override
    public boolean updateTakedToSyncForLstObjectGroupMapping(List<ObjectGroupMapping> objectGroupMappings) {
        
        for( ObjectGroupMapping e : objectGroupMappings ) {
            e.setTakedToSync(DataSynkStatus.TAKED.code());
            e.setUpdatedTime(DateUtils.convertToLocalDateTime(new Date()));
        }
        
        List<ObjectGroupMapping> lstUpdated = this.objectGroupMappingRepository.saveAll(objectGroupMappings);
        
        LOGGER.info("updateTakedToSyncForLstObjectGroupMapping.saved [{}] items", lstUpdated != null ? lstUpdated.size() : 0);
        
        return lstUpdated != null;
    }
}
