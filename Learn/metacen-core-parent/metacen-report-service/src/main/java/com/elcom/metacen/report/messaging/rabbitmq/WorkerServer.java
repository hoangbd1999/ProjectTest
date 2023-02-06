package com.elcom.metacen.report.messaging.rabbitmq;

import com.elcom.metacen.report.model.DataAnalyzed;
import com.elcom.metacen.report.model.dto.DataAnalyzedRequestReportDTO;
import com.elcom.metacen.report.service.DataAnalyzedService;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

//    public WorkerServer() {
//    }

//    @Autowired
//    private WorkerConfig workerConfig;

    @Autowired
    private DataAnalyzedService dataAnalyzedService;

    @RabbitListener(queues = "${report.worker.queue}")
    public void workerReportReceive(String json) {
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            
            //TODO: không insert thống kê qua rabbitMQ nữa, dùng kafka -> streamsets
            /*DataAnalyzedRequestReportDTO dataAnalyzedRequestReportDTO = new ObjectMapper().readValue(json, DataAnalyzedRequestReportDTO.class);
            if ( dataAnalyzedRequestReportDTO != null ) {
                DataAnalyzed dataAnalyzed = dataAnalyzedService.insertLoggingProcess(dataAnalyzedRequestReportDTO);
                LOGGER.info(dataAnalyzed != null && !StringUtil.isNullOrEmpty(dataAnalyzed.getRefUuidKey()) ? "insertLoggingProcess success" : "insertLoggingProcess error!");
            } else
                LOGGER.info("Error mapping DataAnalyzedRequestReportDTO");*/
            
        } catch (Exception ex) {
            LOGGER.error("workerReportReceive.ex: ", ex.getMessage());
        }
    }
}
