package com.elcom.metacen.contact.service;

import java.util.Map;

public interface EnrichDataService {
    Boolean isExistObjectAnalyzed(Map<String, String> headerMap, Map<String, Object> bodyMap);
}
