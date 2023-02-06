package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum ObjectType {

    PEOPLE,
    ORGANISATION,
    VEHICLE,
    VESSEL,
    AIRPLANE,
    OTHER_VEHICLE,
    EVENT,
    AREA,
    RESOURCES,
    INFRASTRUCTURE,
    OTHER_OBJECT;

    public boolean isPeople() {
        return this == ObjectType.PEOPLE;
    }

    public boolean isOrganisation() {
        return this == ObjectType.ORGANISATION;
    }

    public boolean isVehicle() {
        return this == ObjectType.VEHICLE;
    }

    public boolean isVessel() {
        return this == ObjectType.VESSEL;
    }

    public boolean isAirplane() {
        return this == ObjectType.AIRPLANE;
    }

    public boolean isOtherVehicle() {
        return this == ObjectType.OTHER_VEHICLE;
    }

    public boolean isEvent() {
        return this == ObjectType.EVENT;
    }

    public boolean isArea() {
        return this == ObjectType.AREA;
    }

    public boolean isResources() {
        return this == ObjectType.RESOURCES;
    }

    public boolean isInfrastructure() {
        return this == ObjectType.INFRASTRUCTURE;
    }

    public boolean isOtherObject() {
        return this == ObjectType.OTHER_OBJECT;
    }

}
