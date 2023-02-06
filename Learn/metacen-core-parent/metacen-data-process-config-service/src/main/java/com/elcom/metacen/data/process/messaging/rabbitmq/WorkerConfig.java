/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.messaging.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author admin
 */
@Configuration
public class WorkerConfig {

    @Value("${data-process-config.worker.queue}")
    private String workerQueue;

    @Bean("workerQueue")
    public Queue initWorkerQueue() {
        return new Queue(workerQueue);
    }

    //@Bean
    public WorkerServer workerServer() {
        return new WorkerServer();
    }

}
