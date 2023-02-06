package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum MetacenProcessStatus {

    UNKNOWN(-1, "Không xác định"),
    NOT_PROCESS(0, "Chưa xử lý"),
    PROCESSING(1, "Đang xử lý"),
    SUCCESS(2, "Xử lý thành công"),
    ERROR(3, "Xử lý lỗi"),;

    private final int code;

    private final String description;

    MetacenProcessStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public static MetacenProcessStatus of(int code) {
        MetacenProcessStatus[] validFlags = MetacenProcessStatus.values();
        for (MetacenProcessStatus validFlag : validFlags)
            if (validFlag.code() == code)
                return validFlag;
        return UNKNOWN;
    }
}
