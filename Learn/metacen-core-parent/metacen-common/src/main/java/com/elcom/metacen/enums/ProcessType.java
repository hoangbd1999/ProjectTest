package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum ProcessType {

    VSAT_MEDIA_ANALYTICS("VSAT_MEDIA_ANALYTICS", "Xử lý dữ liệu media"),
    VOICE_TO_TEXT("VOICE_TO_TEXT", "Xử lý voice to text"),
    IMAGE_TO_TEXT("IMAGE_TO_TEXT", "Xử lý image to text"),
    SATELLITE("SATELLITE", "Xử lý ảnh vệ tinh"),
    FUSION("FUSION", "Fusion"),
    UNKNOWN("UNKNOWN", "Unknown"),;

    private String type;

    private String description;

    ProcessType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public boolean isVsatMediaAnalytics() {
        return type == VSAT_MEDIA_ANALYTICS.type();
    }

    public boolean isVoiceToText() {
        return type == VOICE_TO_TEXT.type();
    }

    public boolean isImageToText() {
        return type == IMAGE_TO_TEXT.type();
    }

    public boolean isSatellite() {
        return type == SATELLITE.type();
    }

    public boolean isFusion() {
        return type == FUSION.type();
    }

    public boolean isUnknown() {
        return type == UNKNOWN.type();
    }

    public static ProcessType of(String type) {
        ProcessType[] validFlags = ProcessType.values();
        for (ProcessType validFlag : validFlags) {
            if (validFlag.type() == type) {
                return validFlag;
            }
        }

        return UNKNOWN;
    }
}
