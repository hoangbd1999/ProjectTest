package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum DataActiveStatus {

    INACTIVE(0, "Inactive"),
    ACTIVE(1, "Active"),
    UNKNOWN(2, "Unknown"),;

    private int code;

    private String description;

    DataActiveStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isInactive() {
        return code == INACTIVE.code();
    }

    public boolean isActive() {
        return code == ACTIVE.code();
    }

    public boolean isUnknown() {
        return code == UNKNOWN.code();
    }

    public static DataActiveStatus of(int code) {
        DataActiveStatus[] validFlags = DataActiveStatus.values();
        for (DataActiveStatus validFlag : validFlags) {
            if (validFlag.code() == code) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
