package com.elcom.metacen.dispatcher.process.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 *
 * @author Admin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataProcessConfig implements Serializable {

    private String dataType;
    private String processType;
    private DataProcessConfigValue detailConfig;
    private String startTime;
    private String endTime;
    private Integer status;

    public DataProcessConfig() {
    }

    /**
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
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
     * @return the detailConfig
     */
    public DataProcessConfigValue getDetailConfig() {
        return detailConfig;
    }

    /**
     * @param detailConfig the detailConfig to set
     */
    public void setDetailConfig(DataProcessConfigValue detailConfig) {
        this.detailConfig = detailConfig;
    }

    /**
     * @return the startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
