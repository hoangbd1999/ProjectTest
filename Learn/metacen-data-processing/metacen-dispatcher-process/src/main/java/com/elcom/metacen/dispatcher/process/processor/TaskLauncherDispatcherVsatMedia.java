package com.elcom.metacen.dispatcher.process.processor;

import com.elcom.metacen.dispatcher.process.constant.Constant;
import com.elcom.metacen.dispatcher.process.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.dispatcher.process.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.dispatcher.process.model.dto.DataProcessConfig;
import com.elcom.metacen.dispatcher.process.model.kafka.consumer.vsatmedia.VsatMediaMessageFull;
import com.elcom.metacen.dispatcher.process.redis.jobqueue.JobQueueService;
import com.elcom.metacen.dispatcher.process.utils.JSONConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import com.elcom.metacen.dispatcher.process.utils.StringUtil;
import com.elcom.metacen.dto.DataProcessLogging;
import com.elcom.metacen.enums.MetacenProcessStatus;
import com.elcom.metacen.enums.ProcessTypes;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

/**
 *
 * @author Admin
 */
@Component
public class TaskLauncherDispatcherVsatMedia implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLauncherDispatcherVsatMedia.class);
    
    private static final long GMT_TIME_SUBTRACT = 7L * 60L * 60L * 1000L;

    @Value("${kafka.topic.sink.data.analyzed.report}")
    private String kafkaSinkTopicDataAnalyzedReport;
    
    @Value("${kafka.consumer.id.vsat.media.raw}")
    private String kafkaConsumerIdVsatMediaRaw;
    
    @Value("${kafka.consumer.id.vsat.media.raw.retry}")
    private String kafkaConsumerIdVsatMediaRawRetry;
    
    @Value("${kafka.topic.sink.vsat.media.raw.refined}")
    private String kafkaTopicSinkVsatMediaRawRefined;
    
    @Value("${metacen.media.file.storage.path}")
    private String metacenMediaFileStoragePath;
    
    @Value("${metacen.upload.service.media.url}")
    private String metacenUploadServiceMediaUrl;
    
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    @Autowired
    private JobQueueService jobQueueService;
    
    @Autowired
    private RabbitMQClient rabbitMQClient;
    
//    @Autowired
//    private DispatcherProcessService dispatcherProcessService;
    
    @KafkaListener(id = "${kafka.consumer.id.vsat.media.raw}", groupId = "${kafka.consumer.groupId.vsat.media.raw}"
                , topics = "${kafka.topic.source.vsat.media.raw}", autoStartup = "false")
    protected void onMessageVsatMediaRaw(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        VsatMediaMessageFull vsatMediaMessageFull = JSONConverter.toObject(message, VsatMediaMessageFull.class);
        if( vsatMediaMessageFull == null || StringUtil.isNullOrEmpty(vsatMediaMessageFull.getUuidKey())
            || vsatMediaMessageFull.getMediaTypeId() == 1 || vsatMediaMessageFull.getMediaTypeId() == 2 ) // Không xử lý dữ liệu `Audio` và `Video`
            return;

//        LOGGER.info("(Raw) Received from partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, vsatMediaMessageFull.getUuidKey());

        try {
            
            vsatMediaMessageFull.setRetryNum(0);
            
            this.jobQueueService.insertToWaitingQueue(Constant.REDIS_JOB_QUEUE_VSAT_MEDIA, vsatMediaMessageFull);
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }
    
    @KafkaListener(id = "${kafka.consumer.id.vsat.media.raw.retry}", groupId = "${kafka.consumer.groupId.vsat.media.raw.retry}"
                , topics = "${kafka.topic.source.vsat.media.raw.retry}", autoStartup = "false")
    protected void onMessageVsatMediaRawRetry(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        VsatMediaMessageFull vsatMediaMessageRetry = JSONConverter.toObject(message, VsatMediaMessageFull.class);
        if( vsatMediaMessageRetry == null || StringUtil.isNullOrEmpty(vsatMediaMessageRetry.getUuidKey()) )
            return;

//        LOGGER.info("(Retry) Received from partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, vsatMediaMessageRetry.getUuidKey());

        try {
            
            if( vsatMediaMessageRetry.getRetryNum() != null && vsatMediaMessageRetry.getRetryNum() == Constant.MAX_RETRY_NUMBER ) {
                LOGGER.info("(Retry) media record [ {} ] has exceed limit retry number [ {} ] times, return!", vsatMediaMessageRetry.getUuidKey(), Constant.MAX_RETRY_NUMBER);
                return;
            }
            
            this.jobQueueService.insertToWaitingQueue(Constant.REDIS_JOB_QUEUE_VSAT_MEDIA, vsatMediaMessageRetry);
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }
    
    @Scheduled(cron = "${scanTimes.redisQueue.process.vsat.media}")
    protected void processingVsatMediaRawFromRedisQueue() {
        
        List<VsatMediaMessageFull> vsatMediaMessages = new ArrayList<>();
        try {
            VsatMediaMessageFull vsatMedia;
            
            Stopwatch stopwatch = Stopwatch.createStarted();
            
            int takeSizePerRun = 0;
            
            while ( !Thread.currentThread().isInterrupted() ) {
                
                vsatMedia = (VsatMediaMessageFull) this.jobQueueService.getJobVsatMediaRawToProcess(Constant.REDIS_JOB_QUEUE_VSAT_MEDIA);
                
                /*if( vsatMediaMessages.size() > 500 ) {
                    this.jobQueueService.clearProcessedJob2(Constant.REDIS_JOB_QUEUE_VSAT_MEDIA, item);
                    vsatMediaMessages.add(item);
                    break;
                }*/
                
                if ( vsatMedia == null || takeSizePerRun > 5000 ) // Queue empty or limit size per run, break while.
                    break;

                //Nếu đã pop được item thì xóa khỏi waiting_queue đi
                this.jobQueueService.clearProcessedJob2(Constant.REDIS_JOB_QUEUE_VSAT_MEDIA, vsatMedia);

                vsatMediaMessages.add(vsatMedia);
                
                takeSizePerRun ++;
            }
            
            stopwatch.stop();
            LOGGER.info("FINISH take [ {} ] VsatMedia records from redis, spent: [ {} ] ms", vsatMediaMessages.size(), getElapsedTime(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        if( !vsatMediaMessages.isEmpty() ) {
            try {
                
                // ------------------ Lọc dữ liệu theo rule ---------------------------
                RequestMessage rpcRequest = new RequestMessage();
                rpcRequest.setRequestMethod("GET");
                rpcRequest.setRequestPath(RabbitMQProperties.DATA_PROCESS_CONFIG_INTERNAL_URI);
//                LOGGER.info("==> Rpc request: {}", rpcRequest.toJsonString());
                
                String result = this.rabbitMQClient.callRpcService(RabbitMQProperties.DATA_PROCESS_CONFIG_RPC_EXCHANGE, RabbitMQProperties.DATA_PROCESS_CONFIG_RPC_QUEUE, RabbitMQProperties.DATA_PROCESS_CONFIG_RPC_KEY, rpcRequest.toJsonString());
//                LOGGER.info("<== Rpc response : {}", result);

                if( StringUtil.isNullOrEmpty(result) ) {
                    LOGGER.error("ProcessConfig is null!");
                    return;
                }

                ObjectMapper om = new ObjectMapper();
                om.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                
                ResponseMessage response;
                try {
                    response = om.readValue(result, ResponseMessage.class);
                } catch (Exception ex) {
                    LOGGER.error("ProcessConfig -> json parse err(1): ", ex);
                    return;
                }
                if( response == null || response.getStatus() != HttpStatus.OK.value() || response.getData() == null || response.getData().getData() == null ) {
                    LOGGER.error("ProcessConfig -> response is invalid: {}", response);
                    return;
                }

                List<DataProcessConfig> dataProcessConfigs;
                String dataStr = "";
                try {
                    dataStr = JSONConverter.toJSON(response.getData().getData());
                    dataProcessConfigs = om.readValue(dataStr, new TypeReference<List<DataProcessConfig>>(){});
                } catch (Exception e) {
                    LOGGER.error("ProcessConfig -> json parse err(2): {}, dataStr: {}", e, dataStr);
                    return;
                }
                
//                LOGGER.info("dataProcessConfigsOrigin: {}", JSONConverter.toJSON(dataProcessConfigs));
                // ---------------------------------------
                
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            
                            List<DataProcessConfig> dataProcessConfigs2 = filterConfigProcessForVsatMedia(dataProcessConfigs);
//                            if( dataProcessConfigs2 == null || dataProcessConfigs2.isEmpty() )
//                                LOGGER.info("Rule approve for VsatMedia is not exists, accept all record!");
                            
                            for( VsatMediaMessageFull vsatMediaMessage : vsatMediaMessages ) {
                    
                                if( dataProcessConfigs2 != null && !dataProcessConfigs2.isEmpty() ) {
                                    
                                    boolean pass = false;
                                    
                                    // Filter các điều kiện trong cấu hình ( source_ip, dest_ip, data_source_id, data_type, format )
                                    
                                    for( DataProcessConfig dataProcessConfig : dataProcessConfigs2 ) {

                                        String ruleSourceIp = dataProcessConfig.getDetailConfig().getSource_ip();
                                        String ruleDestIp = dataProcessConfig.getDetailConfig().getDest_ip();
                                        String ruleDataSourceId = dataProcessConfig.getDetailConfig().getSource_id();
                                        String ruleMediaType = dataProcessConfig.getDetailConfig().getData_type();
                                        String ruleMediaFormat = dataProcessConfig.getDetailConfig().getFormat();
                                        
                                        int countRuleApproved = 0;
                                        int countRecordMatchedRule = 0;
                                        
                                        if( !StringUtil.isNullOrEmpty(ruleSourceIp) ) {
                                            
//                                            LOGGER.info("ruleSourceIp approve!");
                                            countRuleApproved++;
                                            
                                            String[] arr1 = ruleSourceIp.split(",");
//                                            boolean isBreakOutside = false;
                                            for( String s : arr1 ) {
                                                if( s.trim().equalsIgnoreCase(vsatMediaMessage.getSourceIp()) ) {
//                                                    pass = true;
                                                    
                                                    countRecordMatchedRule++;
                                                    
//                                                    isBreakOutside = true;
                                                    break;
                                                }
                                            }
//                                            if( isBreakOutside )
//                                                continue;
                                        } else {
                                            countRuleApproved++;
                                            countRecordMatchedRule++;
                                        }

                                        if( !StringUtil.isNullOrEmpty(ruleDestIp) ) {
                                            
//                                            LOGGER.info("ruleDestIp approve!");
                                            countRuleApproved++;
                                            
                                            String[] arr1 = ruleDestIp.split(",");
//                                            boolean isBreakOutside = false;
                                            for( String s : arr1 ) {
                                                if( s.trim().equalsIgnoreCase(vsatMediaMessage.getDestIp()) ) {
//                                                    pass = true;
                                                    
                                                    countRecordMatchedRule++;
                                                    
//                                                    isBreakOutside = true;
                                                    break;
                                                }
                                            }
//                                            if( isBreakOutside )
//                                                continue;
                                        } else {
                                            countRuleApproved++;
                                            countRecordMatchedRule++;
                                        }
                                        
                                        if( !StringUtil.isNullOrEmpty(ruleDataSourceId) ) {
                                            
//                                            LOGGER.info("ruleDataSourceId approve!");
                                            countRuleApproved++;
                                            
                                            String[] arr1 = ruleDataSourceId.split(",");
//                                            boolean isBreakOutside = false;
                                            for( String s : arr1 ) {
                                                if( StringUtil.isNumeric(s.trim()) && ( Long.parseLong(s.trim()) == vsatMediaMessage.getDataSourceId() ) ) {
//                                                    pass = true;
                                                    
                                                    countRecordMatchedRule++;
                                                    
//                                                    isBreakOutside = true;
                                                    break;
                                                }
                                            }
//                                            if( isBreakOutside )
//                                                continue;
                                        } else {
                                            countRuleApproved++;
                                            countRecordMatchedRule++;
                                        }

                                        if( !StringUtil.isNullOrEmpty(ruleMediaType) ) {
                                            
//                                            LOGGER.info("ruleMediaType approve!");
                                            countRuleApproved++;
                                            
                                            String[] arr1 = ruleMediaType.split(",");
//                                            boolean isBreakOutside = false;
                                            for( String s : arr1 ) {
                                                if( s.trim().equalsIgnoreCase(vsatMediaMessage.getMediaTypeName()) ) {
//                                                    pass = true;
                                                    
                                                    countRecordMatchedRule++;
                                                    
//                                                    isBreakOutside = true;
                                                    break;
                                                }
                                            }
//                                            if( isBreakOutside )
//                                                continue;
                                        } else {
                                            countRuleApproved++;
                                            countRecordMatchedRule++;
                                        }

                                        if( !StringUtil.isNullOrEmpty(ruleMediaFormat) ) {
                                            
//                                            LOGGER.info("ruleMediaFormat approve!");
                                            countRuleApproved++;
                                            
                                            String[] arr1 = ruleMediaFormat.split(",");
//                                            boolean isBreakOutside = false;
                                            for( String s : arr1 ) {
                                                if( s.trim().equalsIgnoreCase(vsatMediaMessage.getFileType()) ) {
//                                                    pass = true;
                                                    
                                                    countRecordMatchedRule++;
                                                    
//                                                    isBreakOutside = true;
                                                    break;
                                                }
                                            }
//                                            if( isBreakOutside )
//                                                continue;
                                        } else {
                                            countRuleApproved++;
                                            countRecordMatchedRule++;
                                        }
                                        
//                                        LOGGER.info("countRuleApproved: [ {}] , countRecordMatchedRule: [ {} ] - mediaSourceIp: [ {} ], mediaDestIp: [ {} ], mediaDataSourceId: [ {} ], mediaType: [ {} ], mediaFormat: [ {} ], mediaUuid: [ {} ]"
//                                                    , countRuleApproved, countRecordMatchedRule, vsatMediaMessage.getSourceIp(), vsatMediaMessage.getDestIp(), vsatMediaMessage.getDataSourceId(), vsatMediaMessage.getMediaTypeName(), vsatMediaMessage.getFileType(), vsatMediaMessage.getUuidKey());
                                        
                                        if( countRecordMatchedRule >= countRuleApproved ) {
                                            pass = true;
//                                            LOGGER.info("{} PASSED!", vsatMediaMessage.getUuidKey());
                                            break;
                                        }
                                    }
                                    
                                    if( !pass ) {
//                                        LOGGER.info("vsat-media raw [ {} ] not match rule, continue ...", vsatMediaMessage.getUuidKey());
                                        //TODO: cho qua rule hết để test
                                        continue;
                                    }
                                }
                                
                                //TODO: nếu hệ thống không có Rule xử lý nào được áp dụng, thì sẽ mặc định xử lý tất ???
                                // if( dataProcessConfigs2 != null && !dataProcessConfigs2.isEmpty() ) {
                                    vsatMediaMessage.setMediaFileUrl(vsatMediaMessage.getFilePath().replace(metacenMediaFileStoragePath, metacenUploadServiceMediaUrl));
                                    sendKafkaMessage(vsatMediaMessage);
                                // }
                            }
                        } catch (Exception e) {
                            LOGGER.error("ex: {}", e);
                        }
                    }
                }.run();
                
            } catch(Exception ex) {
                LOGGER.error("ex: ", ex);
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("s: " + System.currentTimeMillis());
        System.out.println("s: " + new Date().getTime());
    }
    
    private void sendKafkaMessage(VsatMediaMessageFull vsatMediaMessageRefinedSink) {
        
        String msgAsJson = JSONConverter.toJSON(vsatMediaMessageRefinedSink);

        ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                this.kafkaTopicSinkVsatMediaRawRefined, vsatMediaMessageRefinedSink.getUuidKey(), msgAsJson
        );
        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                
//                LOGGER.info("Sent OK -> [" + kafkaTopicSinkVsatMediaRawRefined + "], offset [" + result.getRecordMetadata().offset() + "], key [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                
                // dispatcherProcessService.updateProcessStatus(MetacenProcessStatus.NOT_PROCESS.code(), vsatMediaMessageRefinedSink.getUuidKey(), "vsat_media_analyzed");
                 
                //TODO: Tạo dữ liệu thống kê cho bản ghi này với trạng thái là `Chưa Xử Lý`
                /*if( vsatMediaMessageRefinedSink.getRetryNum() == null || vsatMediaMessageRefinedSink.getRetryNum() == 0 ) {
                    rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE
                                                    , JSONConverter.toJSON(
                                                            new DataProcessLogging(vsatMediaMessageRefinedSink.getUuidKey(), ProcessTypes.VSAT_MEDIA_ANALYTICS.type(), vsatMediaMessageRefinedSink.getEventTime(), System.currentTimeMillis(), MetacenProcessStatus.NOT_PROCESS.code()
                                                    )
                    ));
                }*/
                insertLoggingProcess(vsatMediaMessageRefinedSink.getUuidKey(), ProcessTypes.VSAT_MEDIA_ANALYTICS.type()
                                        , vsatMediaMessageRefinedSink.getEventTime(), System.currentTimeMillis(), MetacenProcessStatus.NOT_PROCESS.code());
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
            }
        });
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
            this.jobQueueService.registerJobQueue(Constant.REDIS_JOB_QUEUE_VSAT_MEDIA);
            
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdVsatMediaRaw).start();
            
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdVsatMediaRawRetry).start();
            
            LOGGER.info("Begin kafka consumer for vsat-media raw");
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }
    
    @Bean
    public TaskScheduler vsatMediaTaskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        return scheduler;
    }
    
    /*private boolean configProcessForVsatMedia(DataProcessConfig dataProcessConfig) {
        
        if( dataProcessConfig == null ) 
            return false;
        
        return "VSAT".equals(dataProcessConfig.getDataType()) && "VSAT_MEDIA_ANALYTICS".equals(dataProcessConfig.getProcessType())
                && dataProcessConfig.getDetailConfig()!= null && dataProcessConfig.getStatus() != null && dataProcessConfig.getStatus() == 1;
    }*/
    
    private List<DataProcessConfig> filterConfigProcessForVsatMedia(List<DataProcessConfig> dataProcessConfigs) {
        
        if( dataProcessConfigs == null || dataProcessConfigs.isEmpty() )
            return null;
        
        Predicate<DataProcessConfig> streamsPredicate = dataProcessConfig
                -> ( "VSAT".equals(dataProcessConfig.getDataType()) && "VSAT_MEDIA_ANALYTICS".equals(dataProcessConfig.getProcessType())
                && dataProcessConfig.getDetailConfig()!= null && dataProcessConfig.getStatus() != null && dataProcessConfig.getStatus() == 1 );

        List<DataProcessConfig> tmpLst = dataProcessConfigs.stream()
                                                            .filter(streamsPredicate)
                                                            .collect(Collectors.toList());
        
        if( tmpLst == null || tmpLst.isEmpty() )
            return null;
        
        Date currentDate = new Date();
        
        List<DataProcessConfig> mainLst = new ArrayList<>();
        
        DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        
        try {
            for( DataProcessConfig item : tmpLst ) {
                if( !StringUtil.isNullOrEmpty(item.getStartTime()) && !StringUtil.isNullOrEmpty(item.getEndTime()) ) {
                    
                    Date startTime = FMT.parseDateTime(item.getStartTime()).toDate();
                    
                    Date endTime = FMT.parseDateTime(item.getEndTime()).toDate();
//                    if( item.getStartTime().equals(item.getEndTime()) )
//                        endTime = new DateTime(endTime).plusDays(1).toDate();
                    
                    if( startTime.after(currentDate) || endTime.before(currentDate) ) {
//                        LOGGER.info("Time process config was expired, startTime: [ {} ], endTime: [ {} ], continue...", item.getStartTime(), item.getEndTime());
                        continue;
                    }
                }
                mainLst.add(item);
            }
            return mainLst;
        } catch(Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
    
    private void insertLoggingProcess(String refUuidKey, String processTypes, Long eventTime, Long processTime, Integer processStatus) {
        try {
            String msgAsJson = JSONConverter.toJSON(new DataProcessLogging(refUuidKey, processTypes, eventTime != null && !eventTime.equals(0L) ? eventTime : processTime - GMT_TIME_SUBTRACT, processTime, processStatus));
            ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(kafkaSinkTopicDataAnalyzedReport, refUuidKey, msgAsJson);
            sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    // LOGGER.info("Sent OK -> [" + kafkaSinkTopicDataAnalyzedReport + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
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
    
    private String getElapsedTime(long miliseconds) {
        return miliseconds + " (ms)";
    }
}
