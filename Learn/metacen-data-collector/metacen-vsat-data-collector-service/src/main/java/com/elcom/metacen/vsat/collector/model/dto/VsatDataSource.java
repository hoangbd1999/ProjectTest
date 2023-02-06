package com.elcom.metacen.vsat.collector.model.dto;

import com.elcom.metacen.vsat.collector.redis.jobqueue.Job;
import java.io.Serializable;

/**
 *
 * @author anhdv
 */
public class VsatDataSource extends Job implements Serializable {

    private Long dataSourceId;
    private String dataSourceName;

    public VsatDataSource() {
    }

    public VsatDataSource(Long dataSourceId, String dataSourceName) {
        this.dataSourceId = dataSourceId;
        this.dataSourceName = dataSourceName;
    }

    /**
     * @return the dataSourceId
     */
    public Long getDataSourceId() {
        return dataSourceId;
    }

    /**
     * @param dataSourceId the dataSourceId to set
     */
    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    /**
     * @return the dataSourceName
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * @param dataSourceName the dataSourceName to set
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
}
