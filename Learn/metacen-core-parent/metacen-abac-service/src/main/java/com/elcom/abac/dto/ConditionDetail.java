package com.elcom.abac.dto;

import java.io.Serializable;

public class ConditionDetail implements Serializable {
    private String param;
    private String condition;
    private Object value;
    private boolean require;
    private String description;
    private boolean changeValue;
    private String api;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isRequire() {
        return require;
    }

    public void setRequire(boolean require) {
        this.require = require;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(boolean changeValue) {
        this.changeValue = changeValue;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }
}
