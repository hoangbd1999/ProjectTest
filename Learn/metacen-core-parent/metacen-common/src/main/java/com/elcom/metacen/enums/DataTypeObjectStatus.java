package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum DataTypeObjectStatus {

    VESSEL("VESSEL", "Vessel"),
    AIRPLANE("AIRPLANE", "Airplane"),
    PEOPLE("PEOPLE", "People"),
    ORGANISATION("ORGANISATION","Organisation"),
    EVENT("EVENT", "Event"),
    AREA("AREA", "Area"),
    UNKNOWN("UNKNOWN", "Unknown"),;

    private String type;

    private String description;

    DataTypeObjectStatus(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public boolean isVessel() {
        return type == VESSEL.type();
    }

    public boolean isAirplane() {
        return type == AIRPLANE.type();
    }

    public boolean isPeople() {
        return type == PEOPLE.type();
    }

    public boolean isOrganisation() {
        return type == ORGANISATION.type();
    }

    public boolean isEvent() {
        return type == EVENT.type();
    }

    public boolean isArea() {
        return type == AREA.type();
    }

    public boolean isUnknown() {
        return type == UNKNOWN.type();
    }

    public static DataTypeObjectStatus of(String type) {
        DataTypeObjectStatus[] validFlags = DataTypeObjectStatus.values();
        for (DataTypeObjectStatus validFlag : validFlags) {
            if (validFlag.type() == type) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
