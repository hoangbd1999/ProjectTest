package com.elcom.abac.dto;

import com.elcom.abac.model.Policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PolicyAuthenticationRedis implements Serializable {
    private String roleCode;

    private Map<String,Map<String,List<Policy>>> policies; // ResourceRole,method

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Map<String, Map<String, List<Policy>>> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<String, Map<String, List<Policy>>> policies) {
        this.policies = policies;
    }
}
