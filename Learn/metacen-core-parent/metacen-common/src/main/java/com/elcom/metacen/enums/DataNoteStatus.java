package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum DataNoteStatus {

    NO_NOTED(0, "No noted"),
    NOTED(1, "Noted"),
    UNKNOWN(2, "Unknown"),;

    private int code;

    private String description;

    DataNoteStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isNoNoted() {
        return code == NO_NOTED.code();
    }

    public boolean isNoted() {
        return code == NOTED.code();
    }

    public boolean isUnknown() {
        return code == UNKNOWN.code();
    }

    public static DataNoteStatus of(int code) {
        DataNoteStatus[] validFlags = DataNoteStatus.values();
        for (DataNoteStatus validFlag : validFlags) {
            if (validFlag.code() == code) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
