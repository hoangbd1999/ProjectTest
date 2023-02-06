package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum DataSynkStatus {

    NOT_TAKE(0, "Not take"),
    TAKED(1, "Taked"),
    UNKNOWN(2, "Unknown");

    private final int code;

    private final String description;

    DataSynkStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public static DataSynkStatus of(int code) {
        DataSynkStatus[] validFlags = DataSynkStatus.values();
        for (DataSynkStatus validFlag : validFlags)
            if (validFlag.code() == code)
                return validFlag;
        return UNKNOWN;
    }
}
