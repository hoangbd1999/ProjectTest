package com.elcom.metacen.dispatcher.process.redis.jobqueue;

import java.io.Serializable;
import java.util.UUID;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Job implements Serializable {

    private UUID jobId;

    /**
     * @return the jobId
     */
    public UUID getJobId() {
        return jobId;
    }

    /**
     * @param jobId the jobId to set
     */
    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }
}
