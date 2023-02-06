package com.elcom.metacen.content.kafka;

import com.elcom.metacen.content.dto.*;
import com.elcom.metacen.content.model.VsatMediaAnalyzed;
import com.elcom.metacen.content.service.VsatMediaAnalyzedService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class KafkaWorkerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaWorkerService.class);
    private BlockingQueue<Message> blockingQueueRecognition = null;
    private BlockingQueue<Message> blockingQueueMail = null;

    @Autowired
    private KafkaClient kafkaClient;

    @Autowired
    private VsatMediaAnalyzedService vsatMediaAnalyzedService;

    public KafkaWorkerService() {
        LOGGER.info("WorkerServer constructor ...........");
        if (blockingQueueRecognition == null) {
            blockingQueueRecognition = new LinkedBlockingQueue<>();
        }
//        if (blockingQueueMail == null) {
//            blockingQueueMail = new LinkedBlockingQueue<>();
//        }
//        scanAndTakeRecognition();
        for (int i= 1; i<=3;i++){
            saveRecognitionThread();
        }
//        saveMail();
    }

    void saveRecognitionThread(){
        new Thread() {
            @Override
            public void run() {
                while (true) {

                    List<Message> recognitionMessageList = new ArrayList<>();
                    synchronized (blockingQueueRecognition) {
                        try {
                            Thread.sleep(300); // 1s
                            if (blockingQueueRecognition != null && blockingQueueRecognition.size() > 0) {
                                //
                                recognitionMessageList = new ArrayList<>();
                                blockingQueueRecognition.drainTo(recognitionMessageList);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(recognitionMessageList!=null && recognitionMessageList.size()>0){
                        ObjectMapper mapper = new ObjectMapper();
                        List<VsatMediaAnalyzed> listRecognitionPlates = new ArrayList<>();
                        for(Message message: recognitionMessageList){
                            VsatMediaAnalyzed recognitionPlate;
                            try {
                                recognitionPlate = mapper.readValue(mapper.writeValueAsString(message.getData()), VsatMediaAnalyzed.class);
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                                cal.setTime(recognitionPlate.getEventTime());
                                cal.add(Calendar.HOUR,-7);
                                recognitionPlate.setEventTime(cal.getTime());
                                cal.setTime(recognitionPlate.getIngestTime());
                                cal.add(Calendar.HOUR,-7);
                                recognitionPlate.setIngestTime(cal.getTime());
                                cal.setTime(recognitionPlate.getProcessTime());
                                cal.add(Calendar.HOUR,-7);
                                recognitionPlate.setProcessTime(cal.getTime());
                                listRecognitionPlates.add(recognitionPlate );
                            } catch (JsonProcessingException ex) {
                                LOGGER.error(ex.toString());
                            }

                        }
                        Iterable<VsatMediaAnalyzed> iterable = vsatMediaAnalyzedService.saveAll(listRecognitionPlates);
                        listRecognitionPlates.parallelStream().forEach(item->LOGGER.info("VsatMediaUuidKey : {}",item.getVsatMediaUuidKey()));
                        recognitionMessageList.stream().forEach(message -> sendSaga(message.getTransactionId(), message, true));
                        LOGGER.info("Need insert 2 Elastic media content : {} => Success insert: {}", recognitionMessageList.size(),
                                iterable != null ? StreamSupport.stream(iterable.spliterator(), false).count() : 0);
                    }


                }
            }
        }.start();
    }


//    void saveMail(){
//        new Thread() {
//            @Override
//            public void run() {
//                while (true) {
//
//                    List<Message> mailMessageList = new ArrayList<>();
//                    synchronized (blockingQueueMail) {
//                        try {
//                            Thread.sleep(300); // 1s
//                            if (blockingQueueMail != null && blockingQueueMail.size() > 0) {
//                                //
//                                mailMessageList = new ArrayList<>();
//                                blockingQueueMail.drainTo(mailMessageList);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    List<VsatMediaAnalyzed> mediaAnalyzeds = new ArrayList<>();
//                    if(mailMessageList!=null && mailMessageList.size()>0){
//                        ObjectMapper mapper = new ObjectMapper();
//                        List<EmailAnalyzed> emailAnalyzeds = new ArrayList<                 >();
//                        for(Message message: mailMessageList){
//                            EmailAnalyzed emailAnalyzed;
//                            VsatMediaAnalyzed vsatMediaAnalyzed;
//                            try {
//                                EmailAnalyzedRequest emailAnalyzedRequest = mapper.readValue(mapper.writeValueAsString(message.getData()), EmailAnalyzedRequest.class);
//                                vsatMediaAnalyzed= mapper.readValue(mapper.writeValueAsString(message.getData()), VsatMediaAnalyzed.class);
//                                vsatMediaAnalyzed.setFileContentUtf8(emailAnalyzedRequest.getSubject()+" "+emailAnalyzedRequest.getContents());
//                                emailAnalyzedRequest.setRaw(mapper.writeValueAsString(emailAnalyzedRequest.getRaw()));
//                                emailAnalyzed = mapper.readValue(mapper.writeValueAsString(emailAnalyzedRequest), EmailAnalyzed.class);
//                                emailAnalyzeds.add(emailAnalyzed);
//                                mediaAnalyzeds.add(vsatMediaAnalyzed);
//                            } catch (JsonProcessingException ex) {
//                                LOGGER.error(ex.toString());
//                            }
//
//                        }
//
//
//                        Iterable<VsatMediaAnalyzed> iterable = vsatMediaAnalyzedService.saveAll(mediaAnalyzeds);
//                        emailService.saveAll(emailAnalyzeds);
//                        mailMessageList.stream().forEach(message -> sendSaga(message.getTransactionId(), message, true));
//                        LOGGER.info("Need insert 2 Elastic recognition : {} => Success insert: {}", mailMessageList.size(),
//                                iterable != null ? StreamSupport.stream(iterable.spliterator(), false).count() : 0);
//                    }
//
//
//                }
//            }
//        }.start();
//    }

    @KafkaListener(topics = "${elastic.topic.request}")
    @Async("threadPool")
    public void workerRecevie(String json) throws JsonProcessingException, InterruptedException {
        long start = System.currentTimeMillis();
        long end = 0;

        try {
//            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ElasticRequest request = mapper.readValue(json, ElasticRequest.class);
            if (request != null && request.getData() != null) {
                if ("VSAT_MEDIA".equalsIgnoreCase(request.getPath())) {
                    try {
                        String jsonMeg = mapper.writeValueAsString(request.getData());
                        Message message = mapper.readValue(jsonMeg, Message.class);
                        if (message != null) {
                            String jsonData = mapper.writeValueAsString(message.getData());
                            VsatMediaAnalyzed data = mapper.readValue(jsonData, VsatMediaAnalyzed.class);
                            LOGGER.info("nhân uuid:{}",data.getVsatMediaUuidKey());
                            switch (request.getAction()) {
                                case "INSERT":
                                    blockingQueueRecognition.put(message);
                                    break;
                                case "UPDATE":
                                    //Delete old data
//                                    String id = data.getParentId() != null ? data.getParentId() : data.getId();
//                                    recognitionPlateElasticService.removeById(id);
//                                    //Insert new data
//                                    recognitionPlateElasticService.save(data);
//                                    LOGGER.info("Update Recognition data : {} to Elasttic OK", data.getId());
                                    break;
                                case "DELETE":
//                                    recognitionPlateElasticService.remove(data);
//                                    LOGGER.info("Delete Recognition data : {} to Elasttic OK", data.getId());
                                    break;
                                default:
                                    break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.info(e.getMessage());
                        Message message = mapper.readValue(request.getData().toString(), Message.class);
                        sendSaga(message.getTransactionId(), message, false);
                    }


//                } else if ("VSAT_MEDIA_EMAIL".equalsIgnoreCase(request.getPath())) {
//
//                    try{
//                        String jsonMeg = mapper.writeValueAsString(request.getData());
//                        Message message = mapper.readValue(jsonMeg, Message.class);
//                        if (message != null) {
//                            String jsonData = mapper.writeValueAsString(message.getData());
//                            EmailAnalyzedRequest data = mapper.readValue(jsonData, EmailAnalyzedRequest.class);
//                            switch (request.getAction()) {
//                                case "INSERT":
//                                    blockingQueueMail.put(message);
//                                    break;
//                                case "UPDATE":
//                                    //Delete old data
////                                    String id = data.getParentId() != null ? data.getParentId() : data.getId();
////                                    recognitionPlateElasticService.removeById(id);
////                                    //Insert new data
////                                    recognitionPlateElasticService.save(data);
////                                    LOGGER.info("Update Recognition data : {} to Elasttic OK", data.getId());
//                                    break;
//                                case "DELETE":
////                                    recognitionPlateElasticService.remove(data);
////                                    LOGGER.info("Delete Recognition data : {} to Elasttic OK", data.getId());
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
//
//                    } catch(Exception e){
//                        e.printStackTrace();
//                        LOGGER.info(e.getMessage());
//
//                        Message message = mapper.readValue(request.getData().toString(), Message.class);
//                        sendSaga(message.getTransactionId(), message, false);
//                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Error to handle ElasticRequest >>> {}", ex.getMessage());
        }

        end = System.currentTimeMillis();
    }

    void sendSaga(String transactionId, Message message, boolean processStatus) {
        SagaMessage sagaMessage = SagaMessage.builder()
                .nodeName("ELASTICSEARCH")
                .processName("MEDIA-ELASTICSEARCH")
                .receivedData(message)
                .sentData(message)
                .status(processStatus)
                .transactionId(transactionId)
                .build();
        kafkaClient.callKafkaServerWorker(KafkaProperties.SAGA_WORKER_TOPIC, sagaMessage.toJsonString());
    }
}
