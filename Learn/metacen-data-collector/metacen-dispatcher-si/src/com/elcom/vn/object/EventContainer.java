package com.elcom.vn.object;

/**
 *
 * @author Admin
 */
public class EventContainer {

    public static final int MESSAGE_TYPE_SATELLITE_IMAGE = 1;
    
    public int messageType;
    
    public String messageAsJson;
    
    public String messageUuidKey;

    public EventContainer(int messageType, String messageAsJson, String messageUuidKey) {
        this.messageType = messageType;
        this.messageAsJson = messageAsJson;
        this.messageUuidKey = messageUuidKey;
    }
}
