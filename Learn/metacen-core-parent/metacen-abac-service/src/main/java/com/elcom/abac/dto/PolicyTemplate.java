package com.elcom.abac.dto;

import com.elcom.abac.model.Policy;

import javax.persistence.Column;

public class PolicyTemplate {
    private  int id ;

    private String subjectType;

    private String subjectCondition;

    private String subjectValue;

    private String method;

    private String effect;

    private String policyType;

    private String resourceCode;

    private ConditionPolicy rules;

    public PolicyTemplate() {
    }

    public PolicyTemplate(Policy policy){
        this.id = policy.getId();
        this.effect= policy.getEffect();
        this.policyType = policy.getPolicyType();
        this.subjectCondition= policy.getSubjectCondition();
        this.subjectType = policy.getSubjectType();
        this.resourceCode = policy.getResourceCode();
        this.method = policy.getMethod();
        this.subjectValue = policy.getSubjectValue();
        this.rules = policy.getConditionPolicy();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectCondition() {
        return subjectCondition;
    }

    public void setSubjectCondition(String subjectCondition) {
        this.subjectCondition = subjectCondition;
    }

    public String getSubjectValue() {
        return subjectValue;
    }

    public void setSubjectValue(String subjectValue) {
        this.subjectValue = subjectValue;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public ConditionPolicy getRules() {
        return rules;
    }

    public void setRules(ConditionPolicy rules) {
        this.rules = rules;
    }
}
