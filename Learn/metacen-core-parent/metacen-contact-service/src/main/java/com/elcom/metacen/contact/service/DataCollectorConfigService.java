package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.DataCollectorConfig;

import java.util.List;


public interface DataCollectorConfigService {
    List<DataCollectorConfig> findAll();

    DataCollectorConfig findByCollectType(String collectType);

    boolean updateConfigValue(String collectType, String configValue);

    boolean updateIsRunningProcess(String collectType, boolean isRunningProcess);
}

