package com.elcom.metacen.content.kafka;

import com.elcom.metacen.content.dto.ContentDTO;
import com.elcom.metacen.content.dto.ContentMetaDataRequest;
import com.elcom.metacen.content.dto.ResponseContentsDTO;
import com.elcom.metacen.content.service.impl.ProcessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
public class KafkaWorkerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaWorkerService.class);

    @Autowired
    private KafkaClient kafkaClient;

    @Autowired
    private ProcessService processContent;

//    private BlockingQueue<RecognitionPlate> blockingQueue = null;

    public KafkaWorkerService() {
//        LOGGER.info("WorkerServer constructor ...........");
//        if (blockingQueue == null) {
//            blockingQueue = new LinkedBlockingQueue<>();
//        }
////        scanAndTake();
//        for (int i = 1; i <= 4; i++) {
//            saveRecognition();
//        }
//        saveDb();
    }

    @Async("threadPool")
    @KafkaListener(topics = "${content.topic.request}")
    public void workerRecevie(String json) {
        long start = System.currentTimeMillis();
        long end = start;
        LOGGER.info(" [-->] Server received request json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ContentMetaDataRequest request = mapper.readValue(json, ContentMetaDataRequest.class);

            if (request != null) {
                for (ContentDTO contentFile : request.getContentDTOS()) {
                    if(Long.valueOf(contentFile.getFileSize())<15485760) {
                        CompletableFuture<ResponseContentsDTO> result = processContent.processContent(contentFile);
                    }
                }
            } else {
//                LOGGER.info("Content file request is null. No process here...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
//            LOGGER.error("Error to handle ContentMetaDataRequest  >>> {}", ex.getMessage());
        }
        end = System.currentTimeMillis();
//        LOGGER.info("Process time: {} ms", (end - start));
    }
}
