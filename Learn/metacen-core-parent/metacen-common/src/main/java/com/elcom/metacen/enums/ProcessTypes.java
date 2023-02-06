package com.elcom.metacen.enums;

/**
 * @author Admin
 */
public enum ProcessTypes {

    FUSION("FUSION", "Fusion"),
    SATELLITE_ANALYTICS_RAW("SATELLITE_ANALYTICS", "Satellite analytics raw"),
    SATELLITE_ANALYTICS_COMPARE("SATELLITE_ANALYTICS_COMPARE", "Satellite analytics compare"),
    VSAT_MEDIA_ANALYTICS("VSAT_MEDIA_ANALYTICS", "Vsat media analytics"),
    VOICE_TO_TEXT("VOICE_TO_TEXT", "Voice to text"),
    IMAGE_TO_TEXT("IMAGE_TO_TEXT", "Image to text"),
    UNKNOWN("UNKNOWN", "Unknown"),;

    private String type;

    private String description;

    ProcessTypes(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public boolean isFusion() {
        return type == FUSION.type();
    }

    public boolean isSatelliteAnalytics() {
        return type == SATELLITE_ANALYTICS_RAW.type();
    }
    
    public boolean isSatelliteAnalyticsCompare() {
        return type == SATELLITE_ANALYTICS_COMPARE.type();
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

    public boolean isUnknown() {
        return type == UNKNOWN.type();
    }

    public static ProcessTypes of(String type) {
        ProcessTypes[] validFlags = ProcessTypes.values();
        for (ProcessTypes validFlag : validFlags) {
            if (validFlag.type() == type) {
                return validFlag;
            }
        }
        return UNKNOWN;
    }
}
