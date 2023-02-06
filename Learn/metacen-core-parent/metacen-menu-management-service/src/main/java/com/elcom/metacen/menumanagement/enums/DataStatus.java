package com.elcom.metacen.menumanagement.enums;

/**
 * @author hanh
 */
public enum DataStatus {

    DISABLE(0, "Disable"),
    ENABLE(1, "Enable"),
    DELETE(2, "Delete"),
    UNKNOWN(3, "Unknown"),;

    private int code;

    private String description;

    DataStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isDisable() {
        return code == DISABLE.code();
    }

    public boolean isEnable() {
        return code == ENABLE.code();
    }

    public boolean isDelete() {
        return code == DELETE.code();
    }

    public boolean isUnknown() {
        return code == UNKNOWN.code();
    }

    public static DataStatus of(int code) {
        DataStatus[] validFlags = DataStatus.values();
        for (DataStatus validFlag : validFlags) {
            if (validFlag.code() == code) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
