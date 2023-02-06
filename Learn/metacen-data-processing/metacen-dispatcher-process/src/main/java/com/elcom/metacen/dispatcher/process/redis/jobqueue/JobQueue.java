package com.elcom.metacen.dispatcher.process.redis.jobqueue;

import lombok.Getter;

public class JobQueue {

    private static final String WAITING_QUEUE_NAME_TEMPLATE = "%s:waiting_queue";
    private static final String PROCESSING_QUEUE_NAME_TEMPLATE = "%s:processing_queue";
    private static final String DEAD_QUEUE_NAME_TEMPLATE = "%s:dead_queue";

    @Getter
    private String queueName;
    
    @Getter
    private Integer maxRetries = 1;
    
    /** Sau 60 giây thì hết hạn xử lý cho mỗi bản tin, sẽ đánh dấu là lỗi và đẩy vào dead_queue */
    //private Integer maxTimeOut = 30; //in minutes
    @Getter
    private Long maxTimeOut = 60L;

    public JobQueue(String queueName) {
        if (queueName == null)
            throw new IllegalArgumentException("Queue name must be set");
        this.queueName = queueName;
    }

    public JobQueue(String queueName, Integer maxRetries, Long maxTimeOut) {
        this(queueName);
        this.maxRetries = maxRetries;
        this.maxTimeOut = maxTimeOut;
    }

    public String getWaitingQueueName() {
        return String.format(WAITING_QUEUE_NAME_TEMPLATE, this.queueName);
    }

    public String getProcessingQueueName() {
        return String.format(PROCESSING_QUEUE_NAME_TEMPLATE, this.queueName);
    }

    public String getDeadQueueName() {
        return String.format(DEAD_QUEUE_NAME_TEMPLATE, this.queueName);
    }
}
