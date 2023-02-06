package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.model.DataCollectorConfig;
import com.elcom.metacen.contact.repository.DataCollectionConfigRepoCustomize;
import com.elcom.metacen.contact.repository.DataCollectionConfigRepository;
import com.elcom.metacen.contact.service.DataCollectorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataCollectorConfigImpl implements DataCollectorConfigService {

    @Autowired
    DataCollectionConfigRepository repository;

    @Autowired
    DataCollectionConfigRepoCustomize repoCustomize;

    @Override
    public List<DataCollectorConfig> findAll() {
        return (List<DataCollectorConfig>) repository.findAll();
    }

    @Override
    public DataCollectorConfig findByCollectType(String collectType) {
        try {
            return repository.findByCollectType(collectType);
        }
        catch (Exception e){
            return  null;
        }
    }

    @Override
    public boolean updateConfigValue(String collectType, String configValue) {
        try{
           return repoCustomize.updateConfigValue(collectType,configValue);
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean updateIsRunningProcess(String collectType, boolean isRunningProcess) {
        try{
            return repoCustomize.updateIsRunning(collectType,isRunningProcess);
        }
        catch (Exception e){
            return false;
        }
    }
}
