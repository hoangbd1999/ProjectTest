package com.elcom.metacen.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum GsanViolationType {

    CROWD_FLOWS("CROWD_FLOWS"),
    FIRE_FLOWS("FIRE_FLOWS"),
    ENCROACHING_FLOWS("ENCROACHING_FLOWS"),
    LITERRING_FLOWS("LITERRING_FLOWS"),
    FLOODING_FLOWS("FLOODING_FLOWS"),;

    private String description;

    GsanViolationType(String description) {
        this.description = description;
    }

    public static List<String> getGsanViolationTypes() {
        return Arrays.stream(GsanViolationType.values()).map(GsanViolationType::getDescription).collect(Collectors.toList());
    }

}
