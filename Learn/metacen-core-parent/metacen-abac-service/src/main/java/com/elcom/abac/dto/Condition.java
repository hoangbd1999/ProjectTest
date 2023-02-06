package com.elcom.abac.dto;

import java.io.Serializable;
import java.util.List;

public class Condition implements Serializable {
    private String condition;
    private List<Object> values;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<Object> getValue() {
        return values;
    }

    public void setValue(List<Object> value) {
        this.values = value;
    }
}
