/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.rabbitmq;

import com.elcom.metacen.saga.message.SagaMessage;
import com.elcom.metacen.saga.model.ProcessNode;
import com.elcom.metacen.saga.service.ProcessNodeService;
import com.elcom.metacen.saga.worker.SagaWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author admin
 */
public class WorkerServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerServer.class);

    @Autowired
    private ProcessNodeService processNodeService;

    @Autowired
    private SagaWorker sagaWorker;

    public WorkerServer() {
    }

    @RabbitListener(queues = "${saga.worker.queue}")
    public void workerRecevie(String json) {
        LOGGER.info(" [-->] SERVER RECEIVE REQUEST : " + json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            SagaMessage sagaMessage = mapper.readValue(json, SagaMessage.class);
            ProcessNode processNode = processNodeService.findByProcessNameAndNodeName(sagaMessage.getProcessName(), sagaMessage.getNodeName());
            if (processNode == null) {
                return;
            }
            if (sagaMessage.isStatus()) {
                sagaWorker.handleSuccessMessage(sagaMessage, processNode);
            } else {
                sagaWorker.handleFailedMessage(sagaMessage, processNode);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Error to handle message >>> {}", ex.getMessage());
        }
    }

}
