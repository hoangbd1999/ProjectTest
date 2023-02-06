package com.elcom.metacen.contact.service;

import java.util.Map;

public interface MappingDataService {
    Boolean isExistMappingRelation(Map<String, String> headerMap, Map<String, Object> bodyMap);
}
