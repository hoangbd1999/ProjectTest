package com.elcom.abac.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConditionPolicy implements Serializable {
    private String condition;

    private String description;
    private List<ConditionDetail> value;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<ConditionDetail> getValue() {
        return value;
    }

    public void setValue(List<ConditionDetail> value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
