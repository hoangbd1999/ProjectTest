package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum DataType {

    VSAT("VSAT", "Vsat"),
    AIS("AIS", "Ais"),
    SATELLITE("SATELLITE", "Satellite"),
    UNKNOWN("UNKNOWN", "Unknown"),;

    private String type;

    private String description;

    DataType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public boolean isVsat() {
        return type == VSAT.type();
    }

    public boolean isAis() {
        return type == AIS.type();
    }

    public boolean isSatellite() {
        return type == SATELLITE.type();
    }

    public boolean isUnknown() {
        return type == UNKNOWN.type();
    }

    public static DataType of(String type) {
        DataType[] validFlags = DataType.values();
        for (DataType validFlag : validFlags) {
            if (validFlag.type() == type) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
