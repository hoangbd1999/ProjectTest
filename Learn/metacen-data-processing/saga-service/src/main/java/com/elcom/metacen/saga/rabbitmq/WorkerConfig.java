/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.rabbitmq;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
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

    @Value("${saga.worker.queue}")
    private String workerQueue;

    @Bean("workerQueue")
    public Queue initWorkerQueue() {
        return new Queue(workerQueue);
    }

    @Bean
    public WorkerServer workerServer() {
        return new WorkerServer();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
