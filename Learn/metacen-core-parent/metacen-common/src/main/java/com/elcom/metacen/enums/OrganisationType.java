package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum OrganisationType {

    GOV("GOV", "Tổ chức chính phủ"),
    MULTI_GOV("MULTI_GOV", "Tổ chức liên chính phủ"),
    NON_GOV("NON_GOV", "Tổ chức phi chính phủ"),
    UNKNOWN("", "");

    private String type;

    private String description;

    OrganisationType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public static OrganisationType of(String type) {
        OrganisationType[] validFlags = OrganisationType.values();
        for (OrganisationType validFlag : validFlags) {
            if (validFlag.type().equals(type)) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
