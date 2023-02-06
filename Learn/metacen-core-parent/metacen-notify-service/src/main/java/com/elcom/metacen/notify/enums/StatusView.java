package com.elcom.metacen.notify.enums;

/**
 * @author Admin
 */
public enum StatusView {

    NOT_SEEN(0, "Chưa xem"),
    SEEN(1, "Đã xem"),
    UNKNOWN(1, "Unknown"),;

    private int code;

    private String description;

    StatusView(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public boolean isNotSeen() {
        return code == NOT_SEEN.code();
    }

    public boolean isSeen() {
        return code == SEEN.code();
    }

    public static StatusView of(int code) {
        StatusView[] validFlags = StatusView.values();
        for (StatusView validFlag : validFlags) {
            if (validFlag.code() == code) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
