package com.elcom.metacen.dispatcher.process.redis.jobqueue;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@NoArgsConstructor // for Jackson
public class JobMetaData implements Serializable {

    private LocalDateTime insertedAt;
    private LocalDateTime executedAt;
    private LocalDateTime finishedAt;

    private Integer executionCount = 0;

    public JobMetaData(LocalDateTime insertedAt) {
        this.insertedAt = insertedAt;
    }

    /**
     * @return the insertedAt
     */
    public LocalDateTime getInsertedAt() {
        return insertedAt;
    }

    /**
     * @param insertedAt the insertedAt to set
     */
    public void setInsertedAt(LocalDateTime insertedAt) {
        this.insertedAt = insertedAt;
    }

    /**
     * @return the executedAt
     */
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    /**
     * @param executedAt the executedAt to set
     */
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    /**
     * @return the finishedAt
     */
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    /**
     * @param finishedAt the finishedAt to set
     */
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * @return the executionCount
     */
    public Integer getExecutionCount() {
        return executionCount;
    }

    /**
     * @param executionCount the executionCount to set
     */
    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }
}
