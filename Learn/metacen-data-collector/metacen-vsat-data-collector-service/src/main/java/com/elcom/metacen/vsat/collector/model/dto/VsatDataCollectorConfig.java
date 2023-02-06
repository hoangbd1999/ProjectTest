package com.elcom.metacen.vsat.collector.model.dto;

import java.io.Serializable;

/**
 *
 * @author anhdv
 */
public class VsatDataCollectorConfig implements Serializable {
    
    private String kafkaBroker;
    private String kafkaTopic;
    private boolean runningProcess;

    public VsatDataCollectorConfig() {
    }

    public VsatDataCollectorConfig(String kafkaBroker, String kafkaTopic, boolean runningProcess) {
        this.kafkaBroker = kafkaBroker;
        this.kafkaTopic = kafkaTopic;
        this.runningProcess = runningProcess;
    }

    /**
     * @return the kafkaTopic
     */
    public String getKafkaTopic() {
        return kafkaTopic;
    }

    /**
     * @param kafkaTopic the kafkaTopic to set
     */
    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    /**
     * @return the kafkaBroker
     */
    public String getKafkaBroker() {
        return kafkaBroker;
    }

    /**
     * @param kafkaBroker the kafkaBroker to set
     */
    public void setKafkaBroker(String kafkaBroker) {
        this.kafkaBroker = kafkaBroker;
    }

    /**
     * @return the runningProcess
     */
    public boolean isRunningProcess() {
        return runningProcess;
    }

    /**
     * @param runningProcess the runningProcess to set
     */
    public void setRunningProcess(boolean runningProcess) {
        this.runningProcess = runningProcess;
    }
}
