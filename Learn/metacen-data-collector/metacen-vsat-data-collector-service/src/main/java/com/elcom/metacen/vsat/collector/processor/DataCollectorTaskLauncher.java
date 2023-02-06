package com.elcom.metacen.vsat.collector.processor;

//import com.elcom.metacen.contact.model.VsatDataSource;
import com.elcom.metacen.dto.redis.VsatDataSource;
import com.elcom.metacen.enums.DataSynkStatus;
import com.elcom.metacen.vsat.collector.constant.Constant;
import com.elcom.metacen.vsat.collector.model.mongodb.DataCollectorConfig;
//import com.elcom.metacen.vsat.collector.model.dto.VsatDataSource;
import com.elcom.metacen.vsat.collector.model.kafka.consumer.VsatAisMessage;
import com.elcom.metacen.vsat.collector.model.kafka.consumer.VsatMediaMessage;
import com.elcom.metacen.vsat.collector.model.kafka.producer.MetacenAisMessage;
import com.elcom.metacen.vsat.collector.model.kafka.producer.MetacenVsatAisMessage;
import com.elcom.metacen.vsat.collector.model.kafka.producer.MetacenVsatMediaMessage;
import com.elcom.metacen.vsat.collector.model.mongodb.ObjectGroupMapping;
import com.elcom.metacen.vsat.collector.redis.jobqueue.JobQueueService;
import com.elcom.metacen.vsat.collector.utils.JSONConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import com.elcom.metacen.vsat.collector.utils.StringUtil;
import org.springframework.kafka.core.KafkaTemplate;
import com.elcom.metacen.vsat.collector.service.DataCollectorService;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 *
 * @author anhdv
 */
@Component
public class DataCollectorTaskLauncher implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataCollectorTaskLauncher.class);
    
    @Value("${kafka.consumer.id.vsat.ais}")
    private String kafkaConsumerListenerIdVsatAis;
    
    @Value("${kafka.consumer.id.vsat.media}")
    private String kafkaConsumerListenerIdVsatMedia;
    
    @Value("${kafka.consumer.sink.topic.ais}")
    private String kafkaConsumerSinkTopicAis;
    
    @Value("${kafka.consumer.sink.topic.vsatAis}")
    private String kafkaConsumerSinkTopicVsatAis;
    
    @Value("${kafka.consumer.sink.topic.vsatMedia}")
    private String kafkaConsumerSinkTopicVsatMedia;
    
    @Value("${kafka.sink.topic.object.group.mapping}")
    private String kafkaSinkTopicDimObjectGroupMapping;
    
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    
    @Autowired
    private DataCollectorService vsatDataCollectorService;
    
    @Autowired
    private JobQueueService jobQueueService;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    private BlockingQueue<VsatAisMessage> processQueueVsatAis;
    
    private BlockingQueue<MetacenVsatMediaMessage> processQueueVsatMedia;
    
    private boolean runProcessingForVsatAisData;
    
    private boolean runProcessingForVsatMediaData;
    
    @KafkaListener(id = "${kafka.consumer.id.vsat.ais}", groupId = "${kafka.consumer.groupId.ais}", topics = "${kafka.consumer.source.topic.vsatAis}"
                   , autoStartup = "false")
    protected void onMessageVsatAis(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        VsatAisMessage vsatAisMessage = JSONConverter.toObject(message, VsatAisMessage.class);
        if( vsatAisMessage == null || !StringUtil.isNumeric(vsatAisMessage.getObjId()) ) {
            LOGGER.error("-> invalid msg : " + vsatAisMessage != null ? "objId = [ " + vsatAisMessage.getObjId() + " ]" : "aisMsg is null!");
            return;
        }

//        LOGGER.info("Received from partition: [{}], offset: [{}], mmsi: [{}], eventTime: [{}]", partition, offset, aisMsg.getObjId(), aisMsg.getEventTime());

        try {
            this.processQueueVsatAis.put(vsatAisMessage);
        } catch (Exception e) {
            LOGGER.error("processQueueVsatAis.put.ex: ", e);
        }

        /*MetacenVsatAisMessage metacenVsatAisMessage = new MetacenVsatAisMessage(aisMsg.getObjId(), aisMsg.getImo(), aisMsg.getCountryId(), aisMsg.getTypeId(), aisMsg.getDimA(), aisMsg.getDimB(), aisMsg.getDimC(), aisMsg.getDimD()
                                                                            , aisMsg.getDraugth(), aisMsg.getRot(), aisMsg.getSog(), aisMsg.getCog(), aisMsg.getLongitude(), aisMsg.getLatitude(), aisMsg.getNavstatus(), aisMsg.getTrueHanding()
                                                                            , aisMsg.getCallSign(), aisMsg.getName(), aisMsg.getEta(), aisMsg.getDestination(), aisMsg.getEventTime()
                                                                            , !StringUtil.isNullOrEmpty(aisMsg.getMmsiMaster()) ? aisMsg.getMmsiMaster() : "0"
                                                                            , aisMsg.getSourcePort(), aisMsg.getDestPort(), aisMsg.getSourceIp(), aisMsg.getDestIp()
                                                                            , aisMsg.getDirection(), aisMsg.getDataSource(), aisMsg.getDataSourceName(), aisMsg.getTimeKey());
        
        String msgAsJson = JSONConverter.toJSON(metacenVsatAisMessage);
        if( msgAsJson == null ) {
            LOGGER.error("-> msgAsJson is null!");
            return;
        }

        try {
            // Send msg to metacen kafka
            
            if( aisMsg.getDataSource() == 20000 ) { // Bắn sang topic `AIS_RAW` ( nguồn AIS - bảng `metacen.ais_data` )
                
                MetacenAisMessage metacenAisMessage = new MetacenAisMessage(aisMsg.getObjId(), aisMsg.getImo(), aisMsg.getCallSign(), aisMsg.getName(), "", aisMsg.getCountryId()
                                                                            , aisMsg.getRot(), aisMsg.getSog(), aisMsg.getCog(), aisMsg.getDraugth(), aisMsg.getLongitude(), aisMsg.getLatitude(), null, aisMsg.getEta()
                                                                            , aisMsg.getDestination(), 0, null, null, 0, 0
                                                                            , false, null, 0, 0, 0, 0, null, aisMsg.getNavstatus(), aisMsg.getTrueHanding()
                                                                            , false, false, false
                                                                            , aisMsg.getEventTime(), aisMsg.getTimeKey());
        
                String msgAsJson0 = JSONConverter.toJSON(metacenAisMessage);
                if( msgAsJson0 == null ) {
                    LOGGER.error("-> msgAsJson0 is null!");
                    return;
                }
                
                ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaConsumerSinkTopicAis, metacenAisMessage.getMmsi(), msgAsJson0);
                sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        LOGGER.info("Sent OK -> [" + kafkaConsumerSinkTopicAis + "], offset [" + result.getRecordMetadata().offset() + "], partition [" + result.getRecordMetadata().partition() + "]");
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        LOGGER.error("Unable to send message = [" + msgAsJson0 + "], due to : " + ex.getMessage());
                    }
                });
                
            } else { // Bắn sang topic `VSAT_AIS_RAW` ( nguồn VSAT - bảng `metacen.vsat_ais` )
                
                ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaConsumerSinkTopicVsatAis, metacenVsatAisMessage.getMmsi(), msgAsJson);
                sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        LOGGER.info("Sent OK -> [" + kafkaConsumerSinkTopicVsatAis + "], offset [" + result.getRecordMetadata().offset() + "], partition [" + result.getRecordMetadata().partition() + "]");
                        jobQueueService.insertToWaitingQueue(Constant.PROCESS_VSAT_AIS_DATA_QUEUE_NAME + kafkaConsumerListenerIdVsatAis
                                                            , new VsatDataSource(aisMsg.getDataSource(), aisMsg.getDataSourceName()));
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                    }
                });
            }
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }*/
    }
    
    @Scheduled( initialDelay = 1000L * 30L, fixedDelay = 1000 * 60 )
    protected void processQueueVsatAis() {
        
        if( this.processQueueVsatAis != null && !this.processQueueVsatAis.isEmpty() ) {
            
            LOGGER.info("-> processQueueAis.size: {}", this.processQueueVsatAis.size());
            
            List<VsatAisMessage> metacenVsatAisMessages = new ArrayList<>();
            
            final int maxSizeToDrain = 1100;
            
            this.processQueueVsatAis.drainTo(metacenVsatAisMessages, maxSizeToDrain);
            
            LOGGER.info("-> processQueueAis.sizeRemain: {}", this.processQueueVsatAis.size());
            
            if( metacenVsatAisMessages.isEmpty() )
                return;
            
            for( VsatAisMessage e : metacenVsatAisMessages ) {
                try {
                    if( e.getDataSource() == 20000 ) { // Bắn sang topic `AIS_RAW` ( nguồn AIS - bảng `metacen.ais_data` )

                        String msgAsJson = JSONConverter.toJSON(new MetacenAisMessage(e.getObjId(), e.getImo(), e.getCallSign(), e.getName(), "", e.getCountryId()
                                                                                    , e.getRot(), e.getSog(), e.getCog(), e.getDraugth(), e.getLongitude(), e.getLatitude(), null, e.getEta()
                                                                                    , e.getDestination(), 0, null, null, 0, 0
                                                                                    , false, null, 0, 0, 0, 0, null, e.getNavstatus(), e.getTrueHanding()
                                                                                    , false, false, false
                                                                                    , e.getEventTime(), e.getTimeKey()));
                        if( msgAsJson == null ) {
                            LOGGER.error("-> msgAsJson0 is null!");
                            continue;
                        }

                        ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaConsumerSinkTopicAis, e.getObjId(), msgAsJson);
                        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                            @Override
                            public void onSuccess(SendResult<String, String> result) {
                                
                                LOGGER.info("Sent OK -> [" + kafkaConsumerSinkTopicAis + "], offset [" + result.getRecordMetadata().offset() + "], partition [" + result.getRecordMetadata().partition() + "]");
                                
                                try {
                                    Thread.sleep(10L);
                                } catch (Exception e) {
                                    LOGGER.error("ex: ", e);
                                }
                            }
                            @Override
                            public void onFailure(Throwable ex) {
                                LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                            }
                        });

                    } else { // Bắn sang topic `VSAT_AIS_RAW` ( nguồn VSAT - bảng `metacen.vsat_ais` )

                        String msgAsJson = JSONConverter.toJSON(new MetacenVsatAisMessage(e.getObjId(), e.getImo(), e.getCountryId(), e.getTypeId(), e.getDimA(), e.getDimB(), e.getDimC(), e.getDimD()
                                                                            , e.getDraugth(), e.getRot(), e.getSog(), e.getCog(), e.getLongitude(), e.getLatitude(), e.getNavstatus(), e.getTrueHanding()
                                                                            , e.getCallSign(), e.getName(), e.getEta(), e.getDestination(), e.getEventTime()
                                                                            , !StringUtil.isNullOrEmpty(e.getMmsiMaster()) ? e.getMmsiMaster() : "0"
                                                                            , e.getSourcePort(), e.getDestPort(), e.getSourceIp(), e.getDestIp()
                                                                            , e.getDirection(), e.getDataSource(), e.getDataSourceName(), e.getTimeKey()));
                        if( msgAsJson == null ) {
                            LOGGER.error("-> aisMsgAsJson is null!");
                            continue;
                        }
                        
                        ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaConsumerSinkTopicVsatAis, e.getObjId(), msgAsJson);
                        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                            @Override
                            public void onSuccess(SendResult<String, String> result) {
                                
                                LOGGER.info("Sent OK -> [" + kafkaConsumerSinkTopicVsatAis + "], offset [" + result.getRecordMetadata().offset() + "], partition [" + result.getRecordMetadata().partition() + "]");
                                
                                // Thêm vào redis queue để xử lý tính nguồn thu mới
                                jobQueueService.insertToWaitingQueue(Constant.PROCESS_VSAT_AIS_DATA_QUEUE_NAME + kafkaConsumerListenerIdVsatAis
                                                                    , new VsatDataSource(e.getDataSource(), e.getDataSourceName()));
                                try {
                                    Thread.sleep(10L);
                                } catch (Exception e) {
                                    LOGGER.error("ex: ", e);
                                }
                            }
                            @Override
                            public void onFailure(Throwable ex) {
                                LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                            }
                        });
                    }
                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }
            }
            LOGGER.info("Finish sent ais messages to kafka!");
        }
    }
    
    @KafkaListener(id = "${kafka.consumer.id.vsat.media}", groupId = "${kafka.consumer.groupId.media}", topics = "${kafka.consumer.source.topic.vsatMedia}"
                   , autoStartup = "false")
    protected void onMessageVsatMedia(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        VsatMediaMessage vsatMediaMsg = JSONConverter.toObject(message, VsatMediaMessage.class);
        
        if( vsatMediaMsg == null ) {
            LOGGER.error("-> vsatMediaMsg is null!");
            return;
        }
        
        if( ( vsatMediaMsg.getSrcId() != null && vsatMediaMsg.getSrcId() < 0L )
            || ( vsatMediaMsg.getDestId() != null && vsatMediaMsg.getDestId() < 0L ) ) {
            LOGGER.error("-> vsatMediaMsg not valid -> srcId: [ {} ], destId: [ {} ]", vsatMediaMsg.getSrcId(), vsatMediaMsg.getDestId());
            return;
        }
        
//        LOGGER.info("Received from partition: [{}], offset: [{}], uuidKey: [{}], eventTime: [{}], filePath: [{}]", partition, offset, mediaMsg.getUuidKey(), mediaMsg.getEventTime(), mediaMsg.getFilePath());

        MetacenVsatMediaMessage metacenVsatMediaMessage = new MetacenVsatMediaMessage(vsatMediaMsg.getUuidKey(), vsatMediaMsg.getEventTime(), vsatMediaMsg.getMediaTypeId()
                        , vsatMediaMsg.getMediaTypeName(), vsatMediaMsg.getSourceIp(), vsatMediaMsg.getDestIp(), vsatMediaMsg.getSrcId(), vsatMediaMsg.getDestId(), vsatMediaMsg.getSrcName()
                        , vsatMediaMsg.getDestName(), vsatMediaMsg.getSourcePort(), vsatMediaMsg.getDestPort(), vsatMediaMsg.getSourcePhone(), vsatMediaMsg.getDestPhone(), vsatMediaMsg.getFilePath()
                        , vsatMediaMsg.getFileName(), vsatMediaMsg.getFileSize(), vsatMediaMsg.getFileType(), vsatMediaMsg.getDataSource(), vsatMediaMsg.getDataSourceName(), vsatMediaMsg.getDirection());
        
        /*String metacenMediaFilePath = this.metacenMediaFolderStorage + File.separator + vsatMediaMsg.getFilePath().substring(vsatMediaMsg.getFilePath().indexOf("media_files"));
        File metacenMediaFile = new File(metacenMediaFilePath);
        File parentFolder = metacenMediaFile.getParentFile();
        if ( parentFolder != null && !parentFolder.exists() && !parentFolder.mkdirs() )
            ;*/
        
                // TODO xử lý upload media của hệ thống VSAT sang hệ thống lưu trữ media của METACEN
                // Stopwatch stopwatch = Stopwatch.createStarted();
        /* if( !FileUtil.copyFileUsingChannelFast(new File(vsatMediaMsg.getFilePath()), metacenMediaFile) ) {
                // LOGGER.error("Unable to upload media from = [ {} ] to [ {} ]", vsatMediaMsg.getFilePath(), metacenMediaFilePath);
            return;
        }*/
                // stopwatch.stop();
                // LOGGER.info("Finish upload, spent: [ {} ]", getElapsedTime(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        
        /* metacenVsatMediaMessage.setFilePath(metacenMediaFilePath); */
        
        try {
            this.processQueueVsatMedia.put(metacenVsatMediaMessage);
        } catch (Exception e) {
            LOGGER.error("processQueueVsatMedia.put.ex: ", e);
        }
        
        /*String msgAsJson = JSONConverter.toJSON(metacenVsatMediaMessage);
        if( msgAsJson == null ) {
            LOGGER.error("-> msgAsJson is null!");
            return;
        }
        
        try {
            // Send msg to metacen kafka
            ListenableFuture<SendResult<String, String>> sendFuture
                    = this.kafkaTemplate.send(this.kafkaConsumerSinkTopicVsatMedia, metacenVsatMediaMessage.getUuidKey(), msgAsJson);
            sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    
                    //TODO: bug -> khi gửi liên tục bản tin kafka sẽ gây lỗi: `NetworkException: The server disconnected before a response was received`
                    LOGGER.info("Sent OK -> [" + kafkaConsumerSinkTopicVsatMedia + "], offset [" + result.getRecordMetadata().offset() + "], partition [" + result.getRecordMetadata().partition() + "]");
                    
                    // Thêm vào redis queue để xử lý tính nguồn thu mới
                    jobQueueService.insertToWaitingQueue(Constant.PROCESS_VSAT_AIS_DATA_QUEUE_NAME + kafkaConsumerListenerIdVsatAis
                                                        , new VsatDataSource(vsatMediaMsg.getDataSource(), vsatMediaMsg.getDataSourceName()));
                }

                @Override
                public void onFailure(Throwable ex) {
                    LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                }
            });
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }*/
    }
    
    @Scheduled( initialDelay = 1000L * 30L, fixedDelay = 1000 * 60 )
    protected void processQueueVsatMedia() {
        
        if( this.processQueueVsatMedia != null && !this.processQueueVsatMedia.isEmpty() ) {
            
            LOGGER.info("-> processQueueMedia.size: {}", this.processQueueVsatMedia.size());
            
            List<MetacenVsatMediaMessage> metacenVsatMediaMessages = new ArrayList<>();
            
            final int maxSizeToDrain = 1100;
            
            this.processQueueVsatMedia.drainTo(metacenVsatMediaMessages, maxSizeToDrain);
            
            LOGGER.info("-> processQueueMedia.sizeRemain: {}", this.processQueueVsatMedia.size());
            
            if( metacenVsatMediaMessages.isEmpty() )
                return;
            
            for( MetacenVsatMediaMessage e : metacenVsatMediaMessages ) {
                
                String msgAsJson = JSONConverter.toJSON(e);
                if( msgAsJson == null ) {
                    LOGGER.error("-> mediaMsgAsJson is null!");
                    continue;
                }

                try {
                    // Send msg to metacen kafka
                    ListenableFuture<SendResult<String, String>> sendFuture
                            = this.kafkaTemplate.send(this.kafkaConsumerSinkTopicVsatMedia, e.getUuidKey(), msgAsJson);
                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {

                            //TODO: bug -> khi gửi liên tục bản tin kafka sẽ gây lỗi: `NetworkException: The server disconnected before a response was received`
                            LOGGER.info("Sent OK -> [" + kafkaConsumerSinkTopicVsatMedia + "], offset [" + result.getRecordMetadata().offset() + "], partition [" + result.getRecordMetadata().partition() + "]");

                            // Thêm vào redis queue để xử lý tính nguồn thu mới
                            jobQueueService.insertToWaitingQueue(Constant.PROCESS_VSAT_AIS_DATA_QUEUE_NAME + kafkaConsumerListenerIdVsatAis
                                                                , new VsatDataSource(e.getDataSourceId(), e.getDataSourceName()));
                            try {
                                Thread.sleep(10L);
                            } catch (Exception e) {
                                LOGGER.error("ex: ", e);
                            }
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
            LOGGER.info("Finish sent media messages to kafka!");
        }
    }
    
    @Scheduled( initialDelay = 1000 * 60 * 2, fixedDelay = 1000 * 60 * 2 )
    private boolean processVsatAisData() {
        
        boolean status = false;
        
        DataCollectorConfig dataCollectorConfig = this.vsatDataCollectorService.getConfigValue("VSAT_AIS");

        if( dataCollectorConfig != null && !StringUtil.isNullOrEmpty(dataCollectorConfig.getConfigValue()) ) {

            Boolean demandRunningProcess = dataCollectorConfig.getIsRunningProcess();
            
            if( dataCollectorConfig.getConfigValue() == null || demandRunningProcess == null ) {
                LOGGER.error("DataCollectorConfig is not valid");
                return false;
            }
            
            if( demandRunningProcess && !this.runProcessingForVsatAisData
                && !this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatAis).isRunning() ) {
                
                boolean success = true;
                
                this.jobQueueService.registerJobQueue(Constant.PROCESS_VSAT_AIS_DATA_QUEUE_NAME + this.kafkaConsumerListenerIdVsatAis);
                
                try {
                    this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatAis).start();
                    LOGGER.info("Kafka consumer [ {} ] is started!", this.kafkaConsumerListenerIdVsatAis);
                } catch (Exception e) {
                    LOGGER.error("ex: ", e);
                    success = false;
                }
                
                if( success )
                    this.runProcessingForVsatAisData = true;
                
                status = success;
                
            } else if( !demandRunningProcess && this.runProcessingForVsatAisData
                && this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatAis).isRunning() ) {
                boolean success = true;
                try {
                     this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatAis).stop();
                    LOGGER.info("Kafka consumer [ {} ] is stopped!", this.kafkaConsumerListenerIdVsatAis);
                } catch (Exception e) {
                    LOGGER.error("ex: ", e);
                    success = false;
                }
                
                if( success )
                    this.runProcessingForVsatAisData = false;
                
                status = success;
            }
            
        } else if ( this.runProcessingForVsatAisData && this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatAis).isRunning() ) {
            boolean success = true;
            try {
                this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatAis).stop();
                LOGGER.info("Stopped kafka consumer.");
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
                success = false;
            }
            if( success && this.runProcessingForVsatAisData )
                this.runProcessingForVsatAisData = false;
            
            status = success;
        }
        
        return status;
    }
    
    @Scheduled( initialDelay = 1000 * 60 * 2, fixedDelay = 1000 * 60 * 2 )
    private boolean processVsatMediaData() {

        boolean status = false;

        DataCollectorConfig dataCollectorConfig = this.vsatDataCollectorService.getConfigValue("VSAT_MEDIA");

        if( dataCollectorConfig != null && !StringUtil.isNullOrEmpty(dataCollectorConfig.getConfigValue()) ) {
            
            Boolean demandRunningProcess = dataCollectorConfig.getIsRunningProcess();
            
            if( dataCollectorConfig.getConfigValue() == null || demandRunningProcess == null ) {
                LOGGER.error("DataCollectorConfig is not valid");
                return false;
            }

            if( demandRunningProcess && !this.runProcessingForVsatMediaData
                && !this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatMedia).isRunning() ) {

                boolean success = true;

                // this.jobQueueService.registerJobQueue(Constant.PROCESS_VSAT_MEDIA_DATA_QUEUE_NAME + this.kafkaConsumerListenerIdVsatMedia);

                try {
                    this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatMedia).start();
                    LOGGER.info("Kafka consumer [ {} ] is started!", this.kafkaConsumerListenerIdVsatMedia);
                } catch (Exception e) {
                    LOGGER.error("ex: ", e);
                    success = false;
                }

                if( success )
                    this.runProcessingForVsatMediaData = true;

                status = success;

            } else if( !demandRunningProcess && this.runProcessingForVsatMediaData
                && this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatMedia).isRunning() ) {
                boolean success = true;
                try {
                     this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatMedia).stop();
                    LOGGER.info("Kafka consumer [ {} ] is stopped!", this.kafkaConsumerListenerIdVsatMedia);
                } catch (Exception e) {
                    LOGGER.error("ex: ", e);
                    success = false;
                }

                if( success )
                    this.runProcessingForVsatMediaData = false;

                status = success;
            }

        } else if ( this.runProcessingForVsatMediaData && this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatMedia).isRunning() ) {
            boolean success = true;
            try {
                this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerListenerIdVsatMedia).stop();
                LOGGER.info("Stopped kafka consumer.");
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
                success = false;
            }
            if( success && this.runProcessingForVsatMediaData )
                this.runProcessingForVsatMediaData = false;

            status = success;
        }

        return status;
    }
    
    @Scheduled( initialDelay = 1000 * 2, fixedDelay = 1000 * 60 * 5 )
    protected void syncDimTableFromMongoDbToClickhouse() {
        
        List<ObjectGroupMapping> objectGroupMappings = this.vsatDataCollectorService.findObjectGroupMappingByTakedToSync(DataSynkStatus.NOT_TAKE.code());
        
//        if( objectGroupMappings != null && !objectGroupMappings.isEmpty()
//            && this.vsatDataCollectorService.sinkLstObjectGroupMappingToDimTable(objectGroupMappings) )
//            this.vsatDataCollectorService.updateTakedToSyncForLstObjectGroupMapping(objectGroupMappings);
        
        if( objectGroupMappings != null && !objectGroupMappings.isEmpty() ) {
            
            try {
                
                final ZoneId ZONE_ID = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
                
                CountDownLatch latch = new CountDownLatch(objectGroupMappings.size());
                
                List<ObjectGroupMapping> lstSendSuccess = new ArrayList<>();
                
                for( ObjectGroupMapping e : objectGroupMappings ) {
                    try {
                        
                        e.setUpdatedTimeToMs(e.getUpdatedTime() != null ? e.getUpdatedTime().atZone(ZONE_ID).toEpochSecond() : e.getCreatedTime().atZone(ZONE_ID).toEpochSecond());
                        
                        String msgAsJson = JSONConverter.toJSON(e);

                        ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaSinkTopicDimObjectGroupMapping, msgAsJson);
                        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                            @Override
                            public void onSuccess(SendResult<String, String> result) {
                                
                                LOGGER.info("Sent OK -> [" + kafkaSinkTopicDimObjectGroupMapping + "], offset [" + result.getRecordMetadata().offset() + "]");
                                
                                e.setUpdatedTimeToMs(null);
                                lstSendSuccess.add(e);
                                
                                latch.countDown();
                            }
                            @Override
                            public void onFailure(Throwable ex) {
                                LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                                
                                latch.countDown();
                            }
                        });
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                        latch.countDown();
                    }
                }

                // wait for the latch to be decremented by the X remaining threads
                latch.await();

                if( !lstSendSuccess.isEmpty() )
                    this.vsatDataCollectorService.updateTakedToSyncForLstObjectGroupMapping(lstSendSuccess);
                
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
            }
        }
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
           
            if( this.processQueueVsatMedia == null || this.processQueueVsatMedia.isEmpty() )
                this.processQueueVsatMedia = new LinkedBlockingQueue();
            
            if( this.processQueueVsatAis == null || this.processQueueVsatAis.isEmpty() )
                this.processQueueVsatAis = new LinkedBlockingQueue();
            
            new Thread() {
                @Override
                public void run() {
                    
                    if( !processVsatAisData() )
                        LOGGER.info("Not running collect data for Vsat Ais");
                    else
                        runProcessingForVsatAisData = true;

                    try {
                        if( runProcessingForVsatAisData && !kafkaListenerEndpointRegistry.getListenerContainer(kafkaConsumerListenerIdVsatAis).isRunning() ) {
                            kafkaListenerEndpointRegistry.getListenerContainer(kafkaConsumerListenerIdVsatAis).start();
                            LOGGER.info("Begin kafka consumer for processing vsat ais data");
                        }
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                    
                }
            }.start();
            
            new Thread() {
                @Override
                public void run() {
                    
                    if( !processVsatMediaData() )
                        LOGGER.info("Not running collect data for Vsat Media");
                    else
                        runProcessingForVsatMediaData = true;

                    try {
                        if( runProcessingForVsatMediaData && !kafkaListenerEndpointRegistry.getListenerContainer(kafkaConsumerListenerIdVsatMedia).isRunning() ) {
                            kafkaListenerEndpointRegistry.getListenerContainer(kafkaConsumerListenerIdVsatMedia).start();
                            LOGGER.info("Begin kafka consumer for processing vsat media data");
                        }
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                    
                }
            }.start();
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20);
        return scheduler;
    }
    
    // Xử lý thêm mới nguồn thu VSAT
    @Scheduled(cron = "${scanTimes.redisQueue.process}")
    protected void processingVsatDataSourceFromRedisQueue() {
        
        final List<VsatDataSource> sourcesToCheck = new ArrayList<>();
        try {
            VsatDataSource source;
            
            int takeSizePerRun = 0;
            
            while (true) {
                
                source = (VsatDataSource) this.jobQueueService.getJobVsatDataSourceToProcess(Constant.PROCESS_VSAT_AIS_DATA_QUEUE_NAME + this.kafkaConsumerListenerIdVsatAis);
                
                if ( source == null || takeSizePerRun > 5000 ) // Queue empty or limit size per run, break while.
                    break;

                //Nếu đã pop được item thì xóa khỏi waiting_queue đi
                this.jobQueueService.clearProcessedJob2(Constant.PROCESS_VSAT_AIS_DATA_QUEUE_NAME + this.kafkaConsumerListenerIdVsatAis, source);

                sourcesToCheck.add(source);
                
                takeSizePerRun ++;
            }
            LOGGER.info("FINISH take [ {} ] records from redis!", sourcesToCheck.size());
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        if( !sourcesToCheck.isEmpty() ) {
            
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<Long, VsatDataSource> map = new HashMap<>();
                        for( VsatDataSource source : sourcesToCheck )
                            map.put(source.getDataSourceId(), source);

                        List<VsatDataSource> sourcesToCheck2 = new ArrayList<>();
                        for( Map.Entry<Long, VsatDataSource> entry : map.entrySet() )
                            sourcesToCheck2.add(entry.getValue());

                        List<VsatDataSource> sourcesFromCache = null;
                        try {
                            sourcesFromCache = (List<VsatDataSource>) redisTemplate.opsForList().range(Constant.METACEN_VSAT_DATA_SOURCE_LST, 0, 50000);
                        } catch (Exception e) {
                            LOGGER.error("ex: ", e);
                        }
                        if( sourcesFromCache == null || sourcesFromCache.isEmpty() ) {
                            LOGGER.warn("sourcesFromCache is empty, return .....");
                            return;
                        }

                        boolean found;
                        for( VsatDataSource sourceToCheck : sourcesToCheck2 ) {
                            found = false;
                            for( VsatDataSource sourceFromCache : sourcesFromCache ) {
                                if( sourceToCheck.getDataSourceId().intValue() == sourceFromCache.getDataSourceId().intValue() ) {
                                    found = true;
//                                    LOGGER.info("founded sourceId: {}", sourceToCheck.getDataSourceId().intValue());
                                    break;
                                }
                            }
                            if( !found ) {
                                LOGGER.info("==> INSERT new VsatDataSource [ {} ], [ {} ]", sourceToCheck.getDataSourceId(), sourceToCheck.getDataSourceName());
                                redisTemplate.opsForList().rightPush(Constant.METACEN_VSAT_DATA_SOURCE_LST
                                                                                                , new VsatDataSource(sourceToCheck.getDataSourceId(), sourceToCheck.getDataSourceName()));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("ex: {}", e);
                    }
                }
            }.run();
           
        }
    }
}
