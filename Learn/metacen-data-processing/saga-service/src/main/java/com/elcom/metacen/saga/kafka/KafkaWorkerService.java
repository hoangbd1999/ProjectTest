package com.elcom.metacen.saga.kafka;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class KafkaWorkerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaWorkerService.class);

    @Autowired
    private ProcessNodeService processNodeService;

    @Autowired
    private SagaWorker sagaWorker;

    @KafkaListener(topics = "${saga.topic}")
    @Async("threadPoolTaskScheduler")
    public void handleSagaMessage(String json) {

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
