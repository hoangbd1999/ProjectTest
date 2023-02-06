package com.elcom.metacen.enums;

public enum DataSequenceStatus {

    P("P", "Người"),
    O("O", "Tổ chức"),
    V("V", "Phương tiện"),
    E("E", "Sự kiện"),
    I("I", "Cơ sở hạ tầng"),
    A("A", "Khu vực"),
    R("R", "Tài nguyên"),
    D("D", "Khác"),
    UNKNOWN("UNKNOWN", "Unknown"),;

    private String type;

    private String description;

    DataSequenceStatus(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public static DataSequenceStatus of(String type) {
        DataSequenceStatus[] validFlags = DataSequenceStatus.values();
        for (DataSequenceStatus validFlag : validFlags) {
            if (validFlag.type().equals(type)) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
