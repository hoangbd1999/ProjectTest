/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.raw.data.messaging.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author admin
 */
@Component
public class WorkerServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerServer.class);

    public WorkerServer() {
    }

    @Autowired
    private WorkerConfig workerConfig;

//    @RabbitListener(queues = "${raw-data.worker.queue}")
//    public void workerRecevie(String json) {
//        try {
//            LOGGER.info(" [-->] Server received request for : {}", json);
//        } catch (Exception ex) {
//            LOGGER.error(ex.getMessage());
//            ex.printStackTrace();
//        }
//    }
}
