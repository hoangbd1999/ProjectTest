package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum DataDeleteStatus {

    NOT_DELETED(0, "Not deleted"),
    DELETED(1, "Deleted"),
    UNKNOWN(2, "Unknown"),;

    private int code;

    private String description;

    DataDeleteStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isNotDeleted() {
        return code == NOT_DELETED.code();
    }

    public boolean isDeleted() {
        return code == DELETED.code();
    }

    public boolean isUnknown() {
        return code == UNKNOWN.code();
    }

    public static DataDeleteStatus of(int code) {
        DataDeleteStatus[] validFlags = DataDeleteStatus.values();
        for (DataDeleteStatus validFlag : validFlags) {
            if (validFlag.code() == code) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
