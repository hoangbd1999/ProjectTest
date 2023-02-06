package com.elcom.abac.dto;

import java.util.List;

public class ParamValueDto {
    private List<ValueTypeDto> valueType;

    public List<ValueTypeDto> getValueType() {
        return valueType;
    }

    public void setValueType(List<ValueTypeDto> valueType) {
        this.valueType = valueType;
    }
}
