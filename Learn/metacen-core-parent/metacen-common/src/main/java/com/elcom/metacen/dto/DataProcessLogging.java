package com.elcom.metacen.dto;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class DataProcessLogging implements Serializable {
    
    private String refUuidKey;

    private String processType;

    private Long eventTime;

    private Long processTime;

    private Integer processStatus;

    public DataProcessLogging() {
    }
    
    public DataProcessLogging(String refUuidKey, String processType, Long eventTime, Long processTime, Integer processStatus) {
        this.refUuidKey = refUuidKey;
        this.processType = processType;
        this.eventTime = eventTime;
        this.processTime = processTime;
        this.processStatus = processStatus;
    }

    /**
     * @return the refUuidKey
     */
    public String getRefUuidKey() {
        return refUuidKey;
    }

    /**
     * @param refUuidKey the refUuidKey to set
     */
    public void setRefUuidKey(String refUuidKey) {
        this.refUuidKey = refUuidKey;
    }

    /**
     * @return the processType
     */
    public String getProcessType() {
        return processType;
    }

    /**
     * @param processType the processType to set
     */
    public void setProcessType(String processType) {
        this.processType = processType;
    }

    /**
     * @return the eventTime
     */
    public Long getEventTime() {
        return eventTime;
    }

    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the processTime
     */
    public Long getProcessTime() {
        return processTime;
    }

    /**
     * @param processTime the processTime to set
     */
    public void setProcessTime(Long processTime) {
        this.processTime = processTime;
    }

    /**
     * @return the processStatus
     */
    public Integer getProcessStatus() {
        return processStatus;
    }

    /**
     * @param processStatus the processStatus to set
     */
    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }
}
