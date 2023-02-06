package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum InfrastructureType {

    MILITARY(0, "Quân sự"),
    CIVIL(1, "Dân sự"),
    DUAL(2, "Lưỡng dụng"),
    UNKNOWN(3, "");
    private int code;

    private String description;

    InfrastructureType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isMilitary() {
        return code == MILITARY.code();
    }

    public boolean isCivil() {
        return code == CIVIL.code();
    }

    public boolean isDual() {
        return code == DUAL.code();
    }

    public boolean isUnknown() {
        return code == UNKNOWN.code();
    }

    public static InfrastructureType of(int code) {
        InfrastructureType[] validFlags = InfrastructureType.values();
        for (InfrastructureType validFlag : validFlags) {
            if (validFlag.code() == code) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
