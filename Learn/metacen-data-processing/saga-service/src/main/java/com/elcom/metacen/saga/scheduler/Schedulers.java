package com.elcom.metacen.saga.scheduler;

import com.elcom.metacen.saga.kafka.KafkaClient;
import com.elcom.metacen.saga.model.TransactionData;
import com.elcom.metacen.saga.service.TransactionDataService;
import com.elcom.metacen.saga.worker.SagaWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

/**
 *
 * @author anhdv
 */
@Configuration
@Service
public class Schedulers {

    @Autowired
    private SagaWorker sagaWorker;

    @Autowired
    private TransactionDataService transactionDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedulers.class);

    @Scheduled(fixedDelayString = "60000")
    public void scanTimeoutMessage() throws JsonProcessingException {
        List<TransactionData> listTimeoutTransaction = transactionDataService.getListTransactionTimeOut();
        for (TransactionData transactionData : listTimeoutTransaction){
            sagaWorker.handleTimeOutTransaction(transactionData);
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        return scheduler;
    }

}
