package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum MediaType {

    AUDIO(1, "Audio"),
    VIDEO(2, "Video"),
    WEB(3, "Web"),
    EMAIL(4, "Email"),
    TRANSFERFILE(5, "TransferFile"),
    UNDEFINED(8, "Undefined"),
    UNKNOWN(99, "Unknown");

    private final int code;

    private final String description;

    MediaType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public static MediaType of(int code) {
        MediaType[] validFlags = MediaType.values();
        for (MediaType validFlag : validFlags)
            if (validFlag.code() == code)
                return validFlag;
        return UNKNOWN;
    }
}
