package com.elcom.abac.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class AuthenticationRedis implements Serializable {
    private Map<String, List<Long>> methodPolicy;
    private String uuid;

    public Map<String, List<Long>> getMethodPolicy() {
        return methodPolicy;
    }

    public void setMethodPolicy(Map<String, List<Long>> methodPolicy) {
        this.methodPolicy = methodPolicy;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
