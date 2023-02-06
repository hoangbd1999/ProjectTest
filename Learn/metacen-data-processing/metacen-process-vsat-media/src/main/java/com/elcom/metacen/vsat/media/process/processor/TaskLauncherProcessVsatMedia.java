package com.elcom.metacen.vsat.media.process.processor;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.dto.DataProcessLogging;
import com.elcom.metacen.enums.MetacenProcessStatus;
import com.elcom.metacen.enums.ProcessTypes;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.vsat.media.process.config.ApplicationConfig;
import com.elcom.metacen.vsat.media.process.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.vsat.media.process.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.vsat.media.process.model.dto.*;
import com.elcom.metacen.vsat.media.process.model.kafka.ElasticMessage;
import com.elcom.metacen.vsat.media.process.model.kafka.Message;
import com.elcom.metacen.vsat.media.process.model.kafka.SagaMessage;
import com.elcom.metacen.vsat.media.process.model.kafka.VsatMediaMessageFull;
import com.elcom.metacen.vsat.media.process.model.kafka.consumer.VsatMediaProcessedMessage;
import com.elcom.metacen.vsat.media.process.utils.JSONConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import com.elcom.metacen.vsat.media.process.utils.StringUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.elcom.metacen.vsat.media.process.service.ProcessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;
import org.apache.commons.collections4.ListUtils;
import org.modelmapper.ModelMapper;

/**
 * @author Admin
 */
@Component
public class TaskLauncherProcessVsatMedia implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLauncherProcessVsatMedia.class);

    @Value("${kafka.consumer.id.vsat.media.refined}")
    private String kafkaConsumerIdVsatMediaRefined;

    @Value("${kafka.consumer.id.vsat.media.processed}")
    private String kafkaConsumerIdVsatMediaProcessed;

    @Value("${kafka.topic.sink.vsat.media.to.process}")
    private String kafkaTopicSinkVsatMediaToProcess;

//    @Value("${kafka.topic.sink.vsat.media.raw.retry}")
//    private String kafkaTopicSinkVsatMediaRetry;

    @Value("${kafka.topic.sink.data.process.status}")
    private String kafkaSinkTopicDataProcessStatus;

    @Value("${kafka.topic.sink.data.analyzed.report}")
    private String kafkaSinkTopicDataAnalyzedReport;

    @Value("${kafka.topic.vsat.elaticsearch}")
    private String kafkaTopicVsatElaticsearch;

    @Value("${kafka.topic.vsat.saga}")
    private String kafkaTopicVsatSaga;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Autowired
    private ProcessService processService;

    private static final long GMT_TIME_SUBTRACT = 7L * 60L * 60L * 1000L;

    private BlockingQueue<VsatMediaMessageFull> blockingQueueToProcess = null;
    private BlockingQueue<VsatMediaProcessedMessage> blockingQueueProcessed = null;
//    private BlockingQueue<VsatMediaProcessedMessage> blockingQueueRetry = null;
    private BlockingQueue<VsatMediaDTO> blockingQueueContentKeyword = null;

    private static final HashMap<String, String> hash_map = new HashMap<String, String>();

    public TaskLauncherProcessVsatMedia() {

        LOGGER.info("TaskLauncherProcessVsatMedia constructor ...........");

        if (blockingQueueToProcess == null) {
            blockingQueueToProcess = new LinkedBlockingQueue<>();
        }

        if (blockingQueueProcessed == null) {
            blockingQueueProcessed = new LinkedBlockingQueue<>();
        }

//        if (blockingQueueRetry == null) {
//            blockingQueueRetry = new LinkedBlockingQueue<>();
//        }

        if (blockingQueueContentKeyword == null) {
            blockingQueueContentKeyword = new LinkedBlockingQueue<>();
        }

        try {
            Thread.sleep(1000L);
        } catch (Exception ex) {
            LOGGER.error("TaskLauncherProcessVsatMedia.ex: ", ex);
        }

        this.scanAndTakeToProcess();

        this.scanAndTakeProcessed();

        // this.scanAndTakeRetry();

        this.scanAndTakeContentKeyword();
    }

    private void scanAndTakeToProcess() {
        new Thread() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(5000L);

                        if (blockingQueueToProcess != null && !blockingQueueToProcess.isEmpty()) {
                            long start = System.currentTimeMillis();

                            List<VsatMediaMessageFull> vsatMediaList = new ArrayList<>();
                            blockingQueueToProcess.drainTo(vsatMediaList);
//                            LOGGER.info("vsat-media-message-refined size list: {}", vsatMediaList.size());

                            if (!vsatMediaList.isEmpty()) {
                                Date currentTime = new Date();
                                // insert to vsat_media_data_analyzed table on ClickHouse
                                processService.insertVsatMediaDataLstProcess(vsatMediaList, currentTime.getTime());

                                // push to `VSAT_MEDIA_TO_PROCESS` topic
                                handleVsatMediaToProcessTopic(vsatMediaList, currentTime);
                            }

//                            LOGGER.info("Processed vsat-media-message-refined, spent: {} ms", System.currentTimeMillis() - start);
                        }
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                }
            }
        }.start();
    }

    private void scanAndTakeProcessed() {
        new Thread() {
            @Override
            public void run() {
                List<VsatMediaProcessedMessage> vsatMediaList;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(5000L);

                        if (blockingQueueProcessed != null && !blockingQueueProcessed.isEmpty()) {
                            long start = System.currentTimeMillis();

                            vsatMediaList = new ArrayList<>();
                            blockingQueueProcessed.drainTo(vsatMediaList);

//                            LOGGER.info("vsat-media-message-processed size list: {}", vsatMediaList.size());
                            if (!vsatMediaList.isEmpty()) {
                                handleVsatMediaProcessedTopic(vsatMediaList, new Date());
                            }

//                            LOGGER.info("Processed vsat-media-message-processed, spent: {} ms", System.currentTimeMillis() - start);
                        }
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                }
            }
        }.start();
    }

//    private void scanAndTakeRetry() {
//        new Thread() {
//            @Override
//            public void run() {
//                while (!Thread.currentThread().isInterrupted()) {
//                    try {
//                        Thread.sleep(30000L);
//
//                        if (blockingQueueRetry != null && !blockingQueueRetry.isEmpty()) {
//                            long start = System.currentTimeMillis();
//
//                            List<VsatMediaProcessedMessage> vsatMediaListRetry = new ArrayList<>();
//                            blockingQueueRetry.drainTo(vsatMediaListRetry);
//
//                            LOGGER.info("vsat-media-message-retry size list: {}", vsatMediaListRetry.size());
//                            if (!vsatMediaListRetry.isEmpty()) {
//                                handleVsatMediaRetryTopic(vsatMediaListRetry, new Date());
//                            }
//
//                            LOGGER.info("Processed vsat-media-message-retry, spent: {} ms", System.currentTimeMillis() - start);
//                        }
//                    } catch (Exception e) {
//                        LOGGER.error("ex: ", e);
//                    }
//                }
//            }
//        }.start();
//    }

    private void scanAndTakeContentKeyword() {
        new Thread() {
            @Override
            public void run() {
                List<VsatMediaDTO> vsatMediaList;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(5000L);

                        if (blockingQueueContentKeyword != null && !blockingQueueContentKeyword.isEmpty()) {
                            long start = System.currentTimeMillis();

                            vsatMediaList = new ArrayList<>();
                            blockingQueueContentKeyword.drainTo(vsatMediaList);
//                            LOGGER.info("vsat-media-message-content-keyword size list: {}", vsatMediaList.size());

                            if (!vsatMediaList.isEmpty()) {
                                for (VsatMediaDTO vsatMedia : vsatMediaList) {
                                    handleContentKeyword(vsatMedia);
                                }
                            }

//                            LOGGER.info("Processed vsat-media-message-content-keyword, spent: {} ms", System.currentTimeMillis() - start);
                        }
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                }
            }
        }.start();
    }

    @KafkaListener(id = "${kafka.consumer.id.vsat.media.refined}", groupId = "${kafka.consumer.groupId.vsat.media.refined}",
            topics = "${kafka.topic.source.vsat.media.raw.refined}", autoStartup = "false")
    protected void onMessageVsatMediaRefined(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        if (StringUtil.isNullOrEmpty(key) || StringUtil.isNullOrEmpty(message)) {
            LOGGER.error("key or message is invalid. key [ {} ], message [ {} ] ", key, message);
            return;
        }

        LOGGER.info("(Refined) partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, key);
        try {
            // Genarate UUID for transactionId
            // String uuidKey = UUID.randomUUID().toString();

            VsatMediaMessageFull vsatMediaMessageFull = JSONConverter.toObject(message, VsatMediaMessageFull.class);
            vsatMediaMessageFull.setUuidKey(key);
            vsatMediaMessageFull.setMediaUuidKey(key);
            vsatMediaMessageFull.setDataVendor("VSAT-01");
            vsatMediaMessageFull.setAnalyzedEngine("MVMA-01");
            vsatMediaMessageFull.setProcessType("VSAT_MEDIA_ANALYTICS");
            vsatMediaMessageFull.setProcessStatus(MetacenProcessStatus.PROCESSING.code());
            vsatMediaMessageFull.setMediaFileUrl(vsatMediaMessageFull.getFilePath().replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, ApplicationConfig.MEDIA_LINK_ROOT_API));

            blockingQueueToProcess.put(vsatMediaMessageFull);
        } catch (Exception ex) {
            LOGGER.error("Error handle vsat-media-message-refined >>> {}", ex.getMessage());
        }
    }

    @KafkaListener(id = "${kafka.consumer.id.vsat.media.processed}", groupId = "${kafka.consumer.groupId.vsat.media.processed}",
            topics = "${kafka.topic.source.vsat.media.processed}", autoStartup = "false")
    protected void onMessageVsatMediaProcessed(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        if (StringUtil.isNullOrEmpty(message)) {
            LOGGER.error("(Processed) Message is empty");
            return;
        }

        VsatMediaProcessedMessage vsatMediaProcessedMessage = JSONConverter.toObject(message, VsatMediaProcessedMessage.class);
        if (vsatMediaProcessedMessage == null) {
            LOGGER.error("(Processed) Received null, return...");
            return;
        }

        LOGGER.info("(Processed) partition: [{}], offset: [{}], mediaUuidKey: [{}]", partition, offset, vsatMediaProcessedMessage.getMediaUuidKey());
        if (StringUtil.isNullOrEmpty(vsatMediaProcessedMessage.getMediaUuidKey())) {
            LOGGER.error("(Processed) mediaUuidKey is null, return...");
            return;
        }

        /*if (vsatMediaProcessedMessage.getStatus() == null || !vsatMediaProcessedMessage.getStatus()) {
            try {
                blockingQueueRetry.put(vsatMediaProcessedMessage);
            } catch (Exception ex) {
                LOGGER.error("Error handle vsat-media-message-retry >>> {}", ex.getMessage());
            }
        } else { // SUCCESS*/
            try {
                blockingQueueProcessed.put(vsatMediaProcessedMessage);
            } catch (Exception ex) {
                LOGGER.error("Error handle vsat-media-message-processed >>> {}", ex.getMessage());
            }
        //}
    }

    public static String getMessage(int i, int max, String message, List<String> array, int position) {
        i++;
        position++;
        if (i <= max) {
            message += (" " + array.get(position));
            getMessage(i, max, message, array, position);
        } else {
            hash_map.put("key", message);
        }
        return null;
    }

    public static Boolean containsHanScript(String s) {
        for (int i = 0; i < s.length();) {
            int codepoint = s.codePointAt(i);
            i += Character.charCount(codepoint);
            if (Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private boolean isMatched(String query, String text) {
        return text.toLowerCase().contains(query.toLowerCase());
    }

    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public ResponseMessage getListKeyword() {
        RequestMessage commentRpcRequest = new RequestMessage();
        commentRpcRequest.setRequestMethod("GET");
        commentRpcRequest.setRequestPath(RabbitMQProperties.CONTACT_RPC_INTERNAL);
        commentRpcRequest.setVersion(ResourcePath.VERSION);
        commentRpcRequest.setBodyParam(null);
        commentRpcRequest.setUrlParam("");
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.CONTACT_RPC_EXCHANGE, RabbitMQProperties.CONTACT_RPC_QUEUE, RabbitMQProperties.CONTACT_RPC_KEY, commentRpcRequest.toJsonString());

        // LOGGER.info("keyword - result: " + result);
        if (result != null) {
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Change success", result));
        }

        return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
    }

    public ResponseMessage insertKeywordData(Map<String, Object> bodyParam) {
        RequestMessage commentRpcRequest = new RequestMessage();
        commentRpcRequest.setRequestMethod("POST");
        commentRpcRequest.setRequestPath(RabbitMQProperties.CONTACT_RPC_INTERNAL1);
        commentRpcRequest.setVersion(ResourcePath.VERSION);
        commentRpcRequest.setUrlParam(null);
        commentRpcRequest.setBodyParam(bodyParam);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.CONTACT_RPC_EXCHANGE,
                RabbitMQProperties.CONTACT_RPC_QUEUE, RabbitMQProperties.CONTACT_RPC_KEY, commentRpcRequest.toJsonString());

        // LOGGER.info("keyword - result: " + result);
        if (result != null) {
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Change success", null));
        }

        return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
    }

    private void handleVsatMediaToProcessTopic(List<VsatMediaMessageFull> vsatMediaList, Date currentTime) {
        List<DataProcessStatusDTO> lstMediaProcessStatus = new ArrayList<>();
        for (VsatMediaMessageFull vsatMedia : vsatMediaList) {
            VsatMediaDTO vsatMediaDto = buildVsatMediaDto(vsatMedia, MetacenProcessStatus.PROCESSING.code(), currentTime);
            Message vsatMediaMessage = buildMessage(vsatMediaDto);

            // call to SAGA Service
            sendToSaga(vsatMedia.getUuidKey(), vsatMediaMessage, true);

            // call to Elastic Search Service
            sendToElasticSearch("INSERT", vsatMediaMessage);

            // Tạo dữ liệu thống kê cho bản ghi với trạng thái là `Đang xử lý`
            insertLoggingProcess(vsatMedia.getUuidKey(), ProcessTypes.VSAT_MEDIA_ANALYTICS.type(), vsatMedia.getEventTime(), currentTime.getTime(), MetacenProcessStatus.PROCESSING.code());

            lstMediaProcessStatus.add(
                    new DataProcessStatusDTO(vsatMedia.getMediaUuidKey(), MetacenProcessStatus.PROCESSING.code(), vsatMedia.getEventTime(), currentTime.getTime())
            );
        }

        // Send to kafka for insert table data_process_status on clickhouse
        if (!lstMediaProcessStatus.isEmpty()) {
            for (DataProcessStatusDTO e : lstMediaProcessStatus) {
                try {
                    String msgAsJson = JSONConverter.toJSON(e);

                    ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaSinkTopicDataProcessStatus, e.getRecordId(), msgAsJson);
                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {
//                            LOGGER.info("Sent OK -> [" + kafkaSinkTopicDataProcessStatus + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }
            }
        }

        // send to `VSAT_MEDIA_TO_PROCESS` topic
        int maxSizePerLst = 30;
        List<List<VsatMediaMessageFull>> vsatMediaChunkLst = ListUtils.partition(vsatMediaList, maxSizePerLst);
        if (vsatMediaChunkLst == null || vsatMediaChunkLst.isEmpty()) {
            LOGGER.error("vsatMediaChunkLst is empty!");
            return;
        }

        for (List<VsatMediaMessageFull> mediaLstToProcess : vsatMediaChunkLst) {
            ContentMetaDataRequestDTO contentMetaDataRequestDTO = new ContentMetaDataRequestDTO();
            contentMetaDataRequestDTO.setContentDTOS(mediaLstToProcess);

            String msgAsJson = contentMetaDataRequestDTO.toJsonString();
            ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(
                    this.kafkaTopicSinkVsatMediaToProcess, UUID.randomUUID().toString(), msgAsJson
            );
            sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
//                    LOGGER.info("Sent OK -> [" + kafkaTopicSinkVsatMediaToProcess + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                }

                @Override
                public void onFailure(Throwable ex) {
                    LOGGER.error("Send err, due to : " + ex.getMessage());
                }
            });
        }
    }

    private void handleVsatMediaProcessedTopic(List<VsatMediaProcessedMessage> vsatMediaList, Date currentTime) {
        List<DataProcessStatusDTO> lstMediaProcessStatus = new ArrayList<>();
        for (VsatMediaProcessedMessage vsatMedia : vsatMediaList) {
            
            int processStatus = vsatMedia.getStatus() == null || !vsatMedia.getStatus() ? MetacenProcessStatus.ERROR.code() : MetacenProcessStatus.SUCCESS.code();
            
            String uuidKey = vsatMedia.getUuidKey();

            VsatMediaDTO vsatMediaDto = buildVsatMediaDtoFromProcessedMessage(vsatMedia, currentTime);
            Message vsatMediaMessage = buildMessage(vsatMediaDto);

            // call to SAGA Service
            sendToSaga(uuidKey, vsatMediaMessage, true);

            // call to Elastic Search Service
            sendToElasticSearch("INSERT", vsatMediaMessage);

            // Tạo dữ liệu thống kê cho bản ghi với trạng thái là `Xử lý thành công`
            insertLoggingProcess(uuidKey, ProcessTypes.VSAT_MEDIA_ANALYTICS.type(), vsatMedia.getEventTime(), currentTime.getTime(), processStatus);

            // Gắn keyword cho bản ghi media dựa theo content media
            try {
                blockingQueueContentKeyword.put(vsatMediaDto);
            } catch (Exception ex) {
                LOGGER.error("Error handle vsat-media-message-content-keyword >>> {}", ex.getMessage());
            }

//            LOGGER.info("(Processed) Process success >>> vsatMediaId: [ {} ]", vsatMediaDto.getId());

            lstMediaProcessStatus.add(
                    new DataProcessStatusDTO(vsatMedia.getMediaUuidKey(), processStatus, vsatMedia.getEventTime(), currentTime.getTime())
            );
        }

        // Send to kafka for insert table data_process_status on clickhouse
        if (!lstMediaProcessStatus.isEmpty()) {

            // processService.insertDataProcessRawStatus(lstMediaProcessStatus);
            for (DataProcessStatusDTO e : lstMediaProcessStatus) {
                try {
                    String msgAsJson = JSONConverter.toJSON(e);

                    ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaSinkTopicDataProcessStatus, e.getRecordId(), msgAsJson);
                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {
//                            LOGGER.info("Sent OK -> [" + kafkaSinkTopicDataProcessStatus + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }
            }
        }
    }

    /*private void handleVsatMediaRetryTopic(List<VsatMediaProcessedMessage> vsatMediaListRetry, Date currentTime) {
        if (vsatMediaListRetry == null || vsatMediaListRetry.isEmpty()) {
            return;
        }

        try {
            List<DataProcessStatusDTO> lstMediaProcessStatus = new ArrayList<>();

            CountDownLatch latch = new CountDownLatch(vsatMediaListRetry.size());

            for (VsatMediaProcessedMessage vsatMedia : vsatMediaListRetry) {
                try {
                    VsatMediaMessageFull vsatMediaMessageFull = modelMapper.map(vsatMedia, VsatMediaMessageFull.class);
                    vsatMediaMessageFull.setRetryNum(vsatMediaMessageFull.getRetryNum() != null ? vsatMediaMessageFull.getRetryNum() + 1 : 0);
                    vsatMediaMessageFull.setProcessStatus(MetacenProcessStatus.ERROR.code());

                    String msgAsJson = vsatMediaMessageFull.toJsonString();
                    ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                            this.kafkaTopicSinkVsatMediaRetry, vsatMediaMessageFull.getMediaUuidKey(), msgAsJson
                    );

                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {
                            LOGGER.info("Sent OK -> [" + kafkaTopicSinkVsatMediaRetry + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");

                            /////////////// PROCESS STATUS: ERROR ///////////////
                            VsatMediaDTO vsatMediaDto = buildVsatMediaDto(vsatMediaMessageFull, MetacenProcessStatus.ERROR.code(), currentTime);
                            Message vsatMediaMessage = buildMessage(vsatMediaDto);

                            // call to SAGA Service
                            sendToSaga(vsatMediaMessageFull.getUuidKey(), vsatMediaMessage, true);

                            // call to Elastic Search Service
                            sendToElasticSearch("INSERT", vsatMediaMessage);

                            // DB: update process status
                            //                    processService.updateProcessStatusForVsatMedia(MetacenProcessStatus.ERROR.code(), currentTime.getTime(), vsatMediaMessageFull.getUuidKey());
                            // processService.insertDataProcessRawStatus(vsatMediaMessageFull.getUuidKey(), MetacenProcessStatus.ERROR.code(), vsatMediaMessageFull.getEventTime());
                            // Tạo dữ liệu thống kê cho bản ghi với trạng thái là `Xử lý lỗi`
                            insertLoggingProcess(vsatMediaMessageFull.getUuidKey(), ProcessTypes.VSAT_MEDIA_ANALYTICS.type(), vsatMediaMessageFull.getEventTime(), currentTime.getTime(), MetacenProcessStatus.ERROR.code());

                            lstMediaProcessStatus.add(
                                    new DataProcessStatusDTO(vsatMediaMessageFull.getUuidKey(), MetacenProcessStatus.ERROR.code(), vsatMediaMessageFull.getEventTime(), currentTime.getTime())
                            );
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            LOGGER.error("handleVsatMediaRetryTopic.send kafka err, due to : {}", ex.getMessage());
                            latch.countDown();
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("send to [ {} ] . ex : {}", this.kafkaTopicSinkVsatMediaRetry, e);
                    latch.countDown();
                }
            }

            // wait for the latch to be decremented by the X remaining threads
            latch.await();

            // Tạo dữ liệu trạng thái xử lý: --> kafka -> streamsets -> DB
            if (!lstMediaProcessStatus.isEmpty()) {

                // processService.insertDataProcessRawStatus(lstMediaProcessStatus);
                // exception: ConcurrentModificationException for `lstMediaProcessStatus` variable at here
                for (DataProcessStatusDTO e : lstMediaProcessStatus) {
                    try {
                        String msgAsJson = JSONConverter.toJSON(e);

                        ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaSinkTopicDataProcessStatus, msgAsJson);
                        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                            @Override
                            public void onSuccess(SendResult<String, String> result) {
                                LOGGER.info("Sent OK -> [" + kafkaSinkTopicDataProcessStatus + "], offset [" + result.getRecordMetadata().offset() + "]");
                            }

                            @Override
                            public void onFailure(Throwable ex) {
                                LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                            }
                        });
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("handleVsatMediaRetryTopic.ex: ", e);
        }
    }*/

    private void handleContentKeyword(VsatMediaDTO vsatMediaDto) {
        // 1. lay ds keyword
        String content = "";
        String contentReplace = "";
        String[] operators = new String[]{"-", "\\+", "\\*", "\\^", "_", "\n", "\r", "\t"};
        if (!StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentUtf8()) && !StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentGB18030()) && vsatMediaDto.getFileContentUtf8().contains("�") && !vsatMediaDto.getFileContentGB18030().contains("�")) {
            content = deAccent(vsatMediaDto.getFileContentGB18030());
            contentReplace = content.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
        } else if (!StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentUtf8()) && !StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentGB18030()) && vsatMediaDto.getFileContentGB18030().contains("�") && !vsatMediaDto.getFileContentUtf8().contains("�")) {
            content = deAccent(vsatMediaDto.getFileContentUtf8());
            contentReplace = content.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
        } else if (!StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentUtf8()) && !StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentGB18030()) && vsatMediaDto.getFileContentGB18030().contains("�") && vsatMediaDto.getFileContentUtf8().contains("�")) {
            content = deAccent(vsatMediaDto.getFileContentGB18030());
            contentReplace = content.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
        } else if (!StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentUtf8()) && StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentGB18030())) {
            content = deAccent(vsatMediaDto.getFileContentUtf8());
            contentReplace = content.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
        } else if (!StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentGB18030()) && StringUtil.isNullOrEmpty(vsatMediaDto.getFileContentUtf8())) {
            content = deAccent(vsatMediaDto.getFileContentGB18030());
            contentReplace = content.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
        } else if (!StringUtil.isNullOrEmpty(vsatMediaDto.getMailContents())) {
            content = deAccent(vsatMediaDto.getMailContents());
            contentReplace = content.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
        } else if (!StringUtil.isNullOrEmpty(vsatMediaDto.getMailSubject())) {
            content = deAccent(vsatMediaDto.getMailSubject());
            contentReplace = content.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
        } else {
            contentReplace = "";
        }

        try {
            ResponseMessage messageContent = getListKeyword();
            String mess = messageContent.getData().getData().toString();
            JSONObject jsonObjData = new JSONObject(mess);
            Object result = jsonObjData.get("data");
            JSONObject jsonObjResult = new JSONObject(result.toString());
            JSONArray jsonArrayResult = jsonObjResult.getJSONArray("data");
            for (int j = 0; j < jsonArrayResult.length(); j++) {
                JSONObject jsonObjFinal = jsonArrayResult.getJSONObject(j);
                String keyword = deAccent(jsonObjFinal.get("name").toString());
                String keywordReplace = keyword.replaceAll("đ", "d").replaceAll(Arrays.toString(operators), " ").toLowerCase();
                List<String> keywordReplaceAll = Arrays.asList(keywordReplace.split(" "));
                // TQ
                String keywordReplaceAllSpace = keywordReplace.replaceAll("", " ").trim();
                List<String> keywordFinal = Arrays.asList(keywordReplaceAllSpace.split(" "));
                // 2. xu ly compare keyword voi content media
                // Compare chinese
                if (containsHanScript(keywordReplace)) {
                    for (int i = 0; i < keywordFinal.size(); i++) {
                        try {
                            if (isMatched(keywordFinal.get(i), contentReplace)) {
                                Map<String, Object> bodyParam = new HashMap<>();
                                bodyParam.put("type", 2);
                                bodyParam.put("refId", vsatMediaDto.getId());
                                bodyParam.put("refType", "");
                                String keywordId = jsonObjFinal.get("uuid").toString();
                                ArrayList<String> strList = new ArrayList<>(
                                        Arrays.asList(keywordId));
                                bodyParam.put("keywordIds", strList);
                                // 3. insert ds keyword vao bang keyword_data
                                this.insertKeywordData(bodyParam);
                            }
                        } catch (Exception e) {
                            LOGGER.error("ex: ", e);
                        }
                    }
                } else if (keywordReplaceAll.size() == 1 || keywordReplaceAll.size() == 2) {
                    List<String> contentReplaceAllFinal = Arrays.asList(contentReplace.split(" "));
                    for (int h = 0; h < keywordReplaceAll.size(); h++) {
                        try {
                            Collection<String> similar = new HashSet<>(contentReplaceAllFinal);
                            similar.retainAll(Collections.singleton(keywordReplaceAll.get(h)));
                            if (!similar.isEmpty()) {
                                Map<String, Object> bodyParam = new HashMap<>();
                                bodyParam.put("type", 2);
                                bodyParam.put("refId", vsatMediaDto.getId());
                                bodyParam.put("refType", "");
                                String keywordId = jsonObjFinal.get("uuid").toString();
                                ArrayList<String> strList = new ArrayList<>(
                                        Arrays.asList(keywordId));
                                bodyParam.put("keywordIds", strList);
                                // 3. insert ds keyword vao bang keyword_data
                                this.insertKeywordData(bodyParam);
                            }
                        } catch (Exception e) {
                            LOGGER.error("ex: ", e);
                        }
                    }
                } else {
                    int runVariable = 1;
                    int max = keywordReplaceAll.size() % 2 == 0 ? keywordReplaceAll.size() / 2 : keywordReplaceAll.size() / 2 + 1;
                    for (int i = 0; i < (keywordReplaceAll.size() % 2 == 0 ? max + 1 : max); i++) {
                        try {
                            String messageKey = String.valueOf(keywordReplaceAll.get(i));
                            getMessage(runVariable, max, messageKey, keywordReplaceAll, i);
                            String mapKeyword = hash_map.get("key");
                            if (isMatched(mapKeyword, contentReplace)) {
                                Map<String, Object> bodyParam = new HashMap<>();
                                bodyParam.put("type", 2);
                                bodyParam.put("refId", vsatMediaDto.getId());
                                bodyParam.put("refType", "");
                                String keywordId = jsonObjFinal.get("uuid").toString();
                                ArrayList<String> strList = new ArrayList<>(
                                        Arrays.asList(keywordId));
                                bodyParam.put("keywordIds", strList);
                                // 3. insert ds keyword vao bang keyword_data
                                this.insertKeywordData(bodyParam);
                            }
                            hash_map.remove("key");
                        } catch (Exception e) {
                            LOGGER.error("ex: ", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error handleContentKeyword: ", e);
        }
    }

    // @Scheduled(cron = "${process.vsat.media.retry.for.timeout.scheduled}")
    // @Scheduled( fixedRate = 900000 ) // 15 minutes
    /*protected void processVsatMediaRetryForTimeout() {
        List<VsatMediaMessageFull> satelliteImageMessageSimplifies = this.processService.getLstVsatMediaTimeoutProcess();
        if (satelliteImageMessageSimplifies != null && !satelliteImageMessageSimplifies.isEmpty()) {
            for (VsatMediaMessageFull vsatMediaMessageFull : satelliteImageMessageSimplifies) {
                if (vsatMediaMessageFull.getRetryNum() == null) {
                    LOGGER.error("Fail to get retryNum item for process timeOut record!");
                    continue;
                }

                try {
                    vsatMediaMessageFull.setRetryNum(vsatMediaMessageFull.getRetryNum() + 1);
                    String msgAsJson = vsatMediaMessageFull.toJsonString();

                    ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(
                            this.kafkaTopicSinkVsatMediaRetry, vsatMediaMessageFull.getMediaUuidKey(), msgAsJson
                    );
                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {
                            LOGGER.info("Sent OK -> [" + kafkaTopicSinkVsatMediaRetry + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");

                            /////////////// PROCESS STATUS: ERROR ///////////////
                            VsatMediaDTO vsatMediaDto = buildVsatMediaDto(vsatMediaMessageFull, MetacenProcessStatus.ERROR.code(), new Date());
                            Message vsatMediaMessage = buildMessage(vsatMediaDto);

                            // call to SAGA Service
                            sendToSaga(vsatMediaMessageFull.getUuidKey(), vsatMediaMessage, true);

                            // call to Elastic Search Service
                            sendToElasticSearch("INSERT", vsatMediaMessage);

                            // Tạo dữ liệu thống kê cho bản ghi với trạng thái là `Xử lý lỗi`
                            insertLoggingProcess(vsatMediaMessageFull.getUuidKey(), vsatMediaMessageFull.getEventTime(), System.currentTimeMillis(), MetacenProcessStatus.ERROR.code());

                            processService.increaseRetryTimesForVsatMedia(vsatMediaMessageFull.getUuidKey());
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            LOGGER.error("Send err, due to : " + ex.getMessage());
                        }
                    });

                    try {
                        Thread.sleep(500L);
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    }
                } catch (Exception e) {
                    LOGGER.error("ex: ", e);
                }
            }
        }
    }*/
    @Override
    public void run(String... args) throws Exception {
        try {
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdVsatMediaRefined).start();

            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdVsatMediaProcessed).start();

            LOGGER.info("Begin kafka consumer for vsat media process");
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(7);
        return scheduler;
    }

    private void sendToSaga(String transactionId, Message message, boolean processStatus) {
        SagaMessage sagaMessage = SagaMessage.builder()
                .nodeName("MEDIA")
                .processName("MEDIA-ELASTICSEARCH")
                .receivedData(message)
                .sentData(null)
                .status(processStatus)
                .transactionId(transactionId)
                .build();

        String msgAsJson = sagaMessage.toJsonString();
        ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(
                this.kafkaTopicVsatSaga, transactionId, msgAsJson
        );
        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
//                LOGGER.info("Sent OK -> [" + kafkaTopicVsatSaga + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("Send err, due to : " + ex.getMessage());
            }
        });
    }

    private void sendToElasticSearch(String action, Message message) {
        ElasticMessage elasticMessage = ElasticMessage.builder()
                .path("VSAT_MEDIA")
                .action(action)
                .data(message)
                .build();

        String msgAsJson = elasticMessage.toJsonString();
        ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(
                this.kafkaTopicVsatElaticsearch, message.getTransactionId(), msgAsJson
        );
        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                // LOGGER.info("Sent OK -> [" + kafkaTopicVsatElaticsearch + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                LOGGER.info("ES - offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("Send err, due to : " + ex.getMessage());
            }
        });
    }

    private Message buildMessage(VsatMediaDTO vsatMediaDto) {
        Message message = Message.builder()
                .transactionId(vsatMediaDto.getId())
                .data(vsatMediaDto)
                .build();

        return message;
    }

    private VsatMediaDTO buildVsatMediaDto(VsatMediaMessageFull vsatMediaMessageFull, int processStatus, Date currentTime) {
        VsatMediaDTO vsatMediaDto = VsatMediaDTO.builder()
                .id(vsatMediaMessageFull.getUuidKey())
                .vsatMediaUuidKey(vsatMediaMessageFull.getMediaUuidKey())
                .mediaTypeId(Long.valueOf(vsatMediaMessageFull.getMediaTypeId()))
                .mediaTypeName(vsatMediaMessageFull.getMediaTypeName())
                .sourceId(vsatMediaMessageFull.getSourceId())
                .sourceName(vsatMediaMessageFull.getSourceName())
                .sourceIp(vsatMediaMessageFull.getSourceIp())
                .sourcePort(Long.valueOf(vsatMediaMessageFull.getSourcePort()))
                .destId(vsatMediaMessageFull.getDestId())
                .destName(vsatMediaMessageFull.getDestName())
                .destIp(vsatMediaMessageFull.getDestIp())
                .destPort(Long.valueOf(vsatMediaMessageFull.getDestPort()))
                .filePath(vsatMediaMessageFull.getFilePath())
                .fileType(vsatMediaMessageFull.getFileType())
                .fileSize(Long.valueOf(vsatMediaMessageFull.getFileSize()))
                .fileContentUtf8("")
                .fileContentGB18030("")
                .mailFrom("")
                .mailReplyTo("")
                .mailTo("")
                .mailAttachments("")
                .mailContents("")
                .mailSubject("")
                .mailScanVirus("")
                .mailScanResult("")
                .mailUserAgent("")
                .mailContentLanguage("")
                .mailXMail("")
                .mailRaw("")
                .dataSourceId(Long.valueOf(vsatMediaMessageFull.getDataSourceId()))
                .dataSourceName(vsatMediaMessageFull.getDataSourceName())
                .direction(vsatMediaMessageFull.getDirection())
                .dataVendor(vsatMediaMessageFull.getDataVendor())
                .analyzedEngine(vsatMediaMessageFull.getAnalyzedEngine())
                .processType(vsatMediaMessageFull.getProcessType())
                .eventTime(new Date(vsatMediaMessageFull.getEventTime()))
                .ingestTime(currentTime)
                .processTime(currentTime)
                .processStatus(processStatus)
                .retryNum(vsatMediaMessageFull.getRetryNum())
                .build();

        return vsatMediaDto;
    }

    private VsatMediaDTO buildVsatMediaDtoFromProcessedMessage(VsatMediaProcessedMessage vsatMediaProcessedMessage, Date currentTime) {
        Integer processStatus = vsatMediaProcessedMessage.getStatus() == null || !vsatMediaProcessedMessage.getStatus() ? MetacenProcessStatus.ERROR.code() : MetacenProcessStatus.SUCCESS.code();

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);

        String contentUTF8 = "";
        String contentGB18030 = "";
        String from = "";
        String replyTo = "";
        String to = "";
        String attachments = "";
        String contents = "";
        String subject = "";
        String scanVirus = "";
        String scanResult = "";
        String userAgent = "";
        String contentLanguage = "";
        String xMail = "";
        String raw = "";
        try {
            if (vsatMediaProcessedMessage.getStatus()) {
                Integer type = vsatMediaProcessedMessage.getType();
                String jsonMsg = mapper.writeValueAsString(vsatMediaProcessedMessage.getData());
                if (type == 0) { // Web, Transfer file
                    TextDTO textDto = mapper.readValue(jsonMsg, TextDTO.class);
                    if (textDto != null) {
                        contentUTF8 = !StringUtil.isNullOrEmpty(textDto.getContentUTF8()) ? textDto.getContentUTF8() : "";
                        contentGB18030 = !StringUtil.isNullOrEmpty(textDto.getContentGB18030()) ? textDto.getContentGB18030() : "";
                    }
                } else if (type == 1) { // Email
                    EmailDTO emailDto = mapper.readValue(jsonMsg, EmailDTO.class);
                    if (emailDto != null) {
                        from = emailDto.getFrom() != null ? JSONConverter.toJSON(emailDto.getFrom()) : "";
                        replyTo = emailDto.getReplyTo() != null ? JSONConverter.toJSON(emailDto.getReplyTo()) : "";
                        to = emailDto.getTo() != null ? JSONConverter.toJSON(emailDto.getTo()) : "";
                        attachments = emailDto.getAttachments() != null ? JSONConverter.toJSON(emailDto.getAttachments()) : "";
                        contents = !StringUtil.isNullOrEmpty(emailDto.getContents()) ? emailDto.getContents() : "";
                        subject = !StringUtil.isNullOrEmpty(emailDto.getSubject()) ? emailDto.getSubject() : "";
                        scanVirus = !StringUtil.isNullOrEmpty(emailDto.getScanVirus()) ? emailDto.getScanVirus() : "";
                        scanResult = !StringUtil.isNullOrEmpty(emailDto.getScanResult()) ? emailDto.getScanResult() : "";
                        userAgent = !StringUtil.isNullOrEmpty(emailDto.getUserAgent()) ? emailDto.getUserAgent() : "";
                        contentLanguage = !StringUtil.isNullOrEmpty(emailDto.getContentLanguage()) ? emailDto.getContentLanguage() : "";
                        xMail = !StringUtil.isNullOrEmpty(emailDto.getXMail()) ? emailDto.getXMail() : "";
                        raw = emailDto.getRaw() != null ? JSONConverter.toJSON(emailDto.getRaw()) : "";
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        VsatMediaDTO vsatMediaDto = VsatMediaDTO.builder()
                .id(vsatMediaProcessedMessage.getUuidKey())
                .vsatMediaUuidKey(vsatMediaProcessedMessage.getMediaUuidKey())
                .mediaTypeId(Long.valueOf(vsatMediaProcessedMessage.getMediaTypeId()))
                .mediaTypeName(vsatMediaProcessedMessage.getMediaTypeName())
                .sourceId(vsatMediaProcessedMessage.getSourceId())
                .sourceName(vsatMediaProcessedMessage.getSourceName())
                .sourceIp(vsatMediaProcessedMessage.getSourceIp())
                .sourcePort(Long.valueOf(vsatMediaProcessedMessage.getSourcePort()))
                .destId(vsatMediaProcessedMessage.getDestId())
                .destName(vsatMediaProcessedMessage.getDestName())
                .destIp(vsatMediaProcessedMessage.getDestIp())
                .destPort(Long.valueOf(vsatMediaProcessedMessage.getDestPort()))
                .filePath(vsatMediaProcessedMessage.getFilePath())
                .fileType(vsatMediaProcessedMessage.getFileType())
                .fileSize(Long.valueOf(vsatMediaProcessedMessage.getFileSize()))
                .fileContentUtf8(contentUTF8)
                .fileContentGB18030(contentGB18030)
                .mailFrom(from)
                .mailReplyTo(replyTo)
                .mailTo(to)
                .mailAttachments(attachments)
                .mailContents(contents)
                .mailSubject(subject)
                .mailScanVirus(scanVirus)
                .mailScanResult(scanResult)
                .mailUserAgent(userAgent)
                .mailContentLanguage(contentLanguage)
                .mailXMail(xMail)
                .mailRaw(raw)
                .dataSourceId(Long.valueOf(vsatMediaProcessedMessage.getDataSourceId()))
                .dataSourceName(vsatMediaProcessedMessage.getDataSourceName())
                .direction(vsatMediaProcessedMessage.getDirection())
                .dataVendor(vsatMediaProcessedMessage.getDataVendor())
                .analyzedEngine(vsatMediaProcessedMessage.getAnalyzedEngine())
                .processType(vsatMediaProcessedMessage.getProcessType())
                .eventTime(new Date(vsatMediaProcessedMessage.getEventTime()))
                .ingestTime(currentTime)
                .processTime(currentTime)
                .processStatus(processStatus)
                .retryNum(vsatMediaProcessedMessage.getRetryNum())
                .build();

        return vsatMediaDto;
    }

    private void insertLoggingProcess(String refUuidKey, String processType, Long eventTime, Long processTime, Integer processStatus) {
        try {
            String msgAsJson = JSONConverter.toJSON(new DataProcessLogging(refUuidKey, processType, eventTime != null && !eventTime.equals(0L) ? eventTime : processTime - GMT_TIME_SUBTRACT, processTime, processStatus));

            ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaSinkTopicDataAnalyzedReport, refUuidKey, msgAsJson);
            sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
//                    LOGGER.info("Sent OK -> [" + kafkaSinkTopicDataAnalyzedReport + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                }

                @Override
                public void onFailure(Throwable ex) {
                    LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                }
            });
        } catch (Exception ex) {
            LOGGER.error("insertLoggingProcess.ex: ", ex);
        }
    }
}
