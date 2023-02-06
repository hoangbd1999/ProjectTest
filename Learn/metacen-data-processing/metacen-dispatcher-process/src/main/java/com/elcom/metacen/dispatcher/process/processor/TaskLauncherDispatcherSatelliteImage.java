package com.elcom.metacen.dispatcher.process.processor;

import com.elcom.metacen.dispatcher.process.constant.Constant;
import com.elcom.metacen.dispatcher.process.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.dispatcher.process.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.dispatcher.process.model.dto.DataProcessConfig;
import com.elcom.metacen.dispatcher.process.model.kafka.consumer.satelliteimage.SatelliteImageMessageFull;
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
import com.elcom.metacen.dispatcher.process.utils.geo.PointUtil;
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
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

/**
 *
 * @author Admin
 */
@Component
public class TaskLauncherDispatcherSatelliteImage implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLauncherDispatcherSatelliteImage.class);
    
    private static final long GMT_TIME_SUBTRACT = 7L * 60L * 60L * 1000L;
    
    @Value("${kafka.consumer.id.satellite.image.raw}")
    private String kafkaConsumerIdSatelliteImageRaw;
    
    @Value("${kafka.consumer.id.satellite.image.raw.retry}")
    private String kafkaConsumerIdSatelliteImageRawRetry;
    
    @Value("${kafka.topic.sink.satellite.image.raw.refined}")
    private String kafkaTopicSinkSatelliteImageRawRefined;
    
    @Value("${kafka.topic.sink.data.analyzed.report}")
    private String kafkaSinkTopicDataAnalyzedReport;
    
//    @Value("${metacen.media.file.storage.path}")
//    private String metacenMediaFileStoragePath;
//            
//    @Value("${metacen.upload.service.media.url}")
//    private String metacenUploadServiceMediaUrl;
    
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
    
    @KafkaListener(id = "${kafka.consumer.id.satellite.image.raw}", groupId = "${kafka.consumer.groupId.satellite.image.raw}"
                , topics = "${kafka.topic.source.satellite.image.raw}", autoStartup = "false")
    protected void onMessageSatelliteImageRaw(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        SatelliteImageMessageFull satelliteImageMessage = JSONConverter.toObject(message, SatelliteImageMessageFull.class);
        if( satelliteImageMessage == null || StringUtil.isNullOrEmpty(satelliteImageMessage.getUuidKey()) )
            return;

        LOGGER.info("(Raw) Received from partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, satelliteImageMessage.getUuidKey());

        try {
            
            satelliteImageMessage.setRetryNum(0);
            
            this.jobQueueService.insertToWaitingQueue(Constant.REDIS_JOB_QUEUE_SATELLITE_IMAGE, satelliteImageMessage);
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }
    
    @KafkaListener(id = "${kafka.consumer.id.satellite.image.raw.retry}", groupId = "${kafka.consumer.groupId.satellite.image.raw.retry}"
                , topics = "${kafka.topic.source.satellite.image.raw.retry}", autoStartup = "false")
    protected void onMessageSatelliteImageRawRetry(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        SatelliteImageMessageFull satelliteImageMessageRetry = JSONConverter.toObject(message, SatelliteImageMessageFull.class);
        if( satelliteImageMessageRetry == null || StringUtil.isNullOrEmpty(satelliteImageMessageRetry.getUuidKey()) )
            return;

        LOGGER.info("(Retry) Received from partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, satelliteImageMessageRetry.getUuidKey());

        try {
            
            if( satelliteImageMessageRetry.getRetryNum() != null && satelliteImageMessageRetry.getRetryNum() == Constant.MAX_RETRY_NUMBER ) {
                LOGGER.info("(Retry) record with uuidKey [{}] exceed limit retry number [ {} ], return!", satelliteImageMessageRetry.getUuidKey(), Constant.MAX_RETRY_NUMBER);
                
                /*rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE
                                                , JSONConverter.toJSON(
                                                        new DataProcessLogging(satelliteImageMessageRetry.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_RAW.type()
                                                        , satelliteImageMessageRetry.getCaptureTime(), System.currentTimeMillis(), MetacenProcessStatus.ERROR.code()
                                                )
                ));*/
                
                insertLoggingProcess(satelliteImageMessageRetry.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_RAW.type()
                                    , satelliteImageMessageRetry.getCaptureTime(), System.currentTimeMillis(), MetacenProcessStatus.ERROR.code());
                
                return;
            }
            
            this.jobQueueService.insertToWaitingQueue(Constant.REDIS_JOB_QUEUE_SATELLITE_IMAGE, satelliteImageMessageRetry);
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }
    
    @Scheduled(cron = "${scanTimes.redisQueue.process.satellite.image}")
    protected void processingSatelliteImageRawFromRedisQueue() {
        
        List<SatelliteImageMessageFull> satelliteImageMessages = new ArrayList<>();
        try {
            SatelliteImageMessageFull item;
            while (true) {
                item = (SatelliteImageMessageFull) this.jobQueueService.getJobSatelliteImageRawToProcess(Constant.REDIS_JOB_QUEUE_SATELLITE_IMAGE);
                
                /*if( satelliteImageMessages.size() > 500 ) {
                    this.jobQueueService.clearProcessedJob2(Constant.REDIS_JOB_QUEUE_SATELLITE_IMAGE, item);
                    satelliteImageMessages.add(item);
                    break;
                }*/
                
                if ( item == null ) // Queue empty, break while.
                    break;

                //Nếu đã pop được item thì xóa khỏi waiting_queue đi
                this.jobQueueService.clearProcessedJob2(Constant.REDIS_JOB_QUEUE_SATELLITE_IMAGE, item);

                satelliteImageMessages.add(item);
            }
            LOGGER.info("FINISH take [ {} ] SatelliteImage records from redis!", satelliteImageMessages.size());
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        if( !satelliteImageMessages.isEmpty() ) {
            try {
                
                // ------------------ Lọc dữ liệu theo rule ---------------------------
                RequestMessage rpcRequest = new RequestMessage();
                rpcRequest.setRequestMethod("GET");
                rpcRequest.setRequestPath(RabbitMQProperties.DATA_PROCESS_CONFIG_INTERNAL_URI);
//                LOGGER.info("==> Rpc request: {}", rpcRequest.toJsonString());
                
                String result = this.rabbitMQClient.callRpcService(RabbitMQProperties.DATA_PROCESS_CONFIG_RPC_EXCHANGE, RabbitMQProperties.DATA_PROCESS_CONFIG_RPC_QUEUE, RabbitMQProperties.DATA_PROCESS_CONFIG_RPC_KEY, rpcRequest.toJsonString());
//                String result = "{\"status\":200,\"message\":\"200 OK\",\"data\":{\"status\":200,\"message\":\"200 OK\",\"data\":[{\"id\":\"635b305d9b933e1adcff9f65\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 08:29:01\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 08:29:01\",\"uuid\":\"f2447233-c3aa-4bab-90d4-1a0de8414779\",\"dataType\":\"SATELLITE\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"109.408332625 16.5763015273, 113.71497325 18.5977859023, 116.52747325 16.4884109023, 117.494270125 13.2364577773, 116.263801375 11.0391921523, 112.396613875 10.4239577773, 109.6720045 11.3907546523, 109.408332625 16.5763015273\"},\"status\":1,\"startTime\":null,\"endTime\":null},{\"id\":\"635b30639b933e1adcff9f66\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 08:29:07\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-28 08:39:10\",\"uuid\":\"fa9ddb8a-26c1-4f8c-aff8-d27e856a8ca9\",\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"\",\"detailConfig\":{\"source_ip\":\"1.1.1.1\",\"dest_ip\":\"1.1.1.2\",\"source_id\":\"94939\",\"data_type\":\"Audio, Web\",\"format\":\".mp3, .amr, .docx, .xls\"},\"status\":1,\"startTime\":null,\"endTime\":null},{\"id\":\"635b411f9b933e1adcff9f67\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 09:40:31\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-28 09:40:46\",\"uuid\":\"3e513232-01d1-4371-ba6b-b3fb81af594e\",\"dataType\":\"SATELLITE\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"108.5902748354 20.2723705298, 111.4668415306 20.3058189014, 110.6306288719 17.4626983321, 107.8544084144 18.2654614964, 108.5902748354 20.2723705298\"},\"status\":1,\"startTime\":\"2022-10-01 00:00:00\",\"endTime\":\"2022-10-10 00:00:00\"},{\"id\":\"635b412b9b933e1adcff9f68\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 09:40:43\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-28 11:30:28\",\"uuid\":\"b32fd1c9-aaae-4be4-9aef-0c1f29b765c4\",\"dataType\":\"SATELLITE\",\"processType\":\"FUSION\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"34.2294302545 0.0475526924, 46.6863182223 -2.4438249011, 49.7061698508 -14.5987277061, 31.6625563702 -20.411942091, 34.2294302545 0.0475526924\"},\"status\":0,\"startTime\":\"2022-10-27 00:00:00\",\"endTime\":\"2022-10-27 00:00:00\"},{\"id\":\"635b41979b933e1adcff9f69\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 09:42:31\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 09:42:31\",\"uuid\":\"c1d4433d-e0e9-48a4-8854-b73b59834f2b\",\"dataType\":\"AIS\",\"processType\":\"FUSION\",\"dataVendor\":\"AIS-01\",\"detailConfig\":{\"mmsi\":\"66735206,66735207,66735210\"},\"status\":1,\"startTime\":null,\"endTime\":null},{\"id\":\"635b43c79b933e1adcff9f6a\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 09:51:51\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 09:51:51\",\"uuid\":\"b69e743a-5a0c-44ac-bd20-8aa4a3e70f89\",\"dataType\":\"SATELLITE\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"109.7993522734 20.7513830424, 110.9858328147 20.4433544403, 111.3394952838 19.8044803027, 111.0428751484 18.6864505618, 109.1604781357 18.0475764241, 108.0196314613 18.2072949585, 107.8941383271 19.9641988371, 108.3618854636 21.127862445, 109.7993522734 20.7513830424\"},\"status\":1,\"startTime\":null,\"endTime\":null},{\"id\":\"635b43d79b933e1adcff9f6b\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 09:52:07\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 09:52:07\",\"uuid\":\"27d7daf5-6718-4839-b457-cd6c93231f42\",\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"10.2.2.3\",\"dest_ip\":\"10.2.2.3\",\"source_id\":\"94932\",\"data_type\":\"\",\"format\":\"\"},\"status\":1,\"startTime\":\"2022-10-19 00:00:00\",\"endTime\":\"2022-10-29 00:00:00\"},{\"id\":\"635b51e69b933e1adcff9f70\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 10:52:06\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 10:52:06\",\"uuid\":\"37cf8a6f-9361-4170-82a2-cb8b18ce96b2\",\"dataType\":\"SATELLITE\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"\",\"detailConfig\":{\"coordinates\":\"118.8803997171 11.6273122986, 116.2174560205 7.8725601489, 117.8152215233 7.4997472447, 120.664571672 10.7751699224, 120.6912037193 12.4794546749, 119.2532126213 11.5207939429, 118.8803997171 11.6273122986\"},\"status\":1,\"startTime\":\"2022-10-20 00:00:00\",\"endTime\":\"2022-10-28 00:00:00\"},{\"id\":\"635b5bc89b933e1adcff9f71\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 11:34:16\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 11:34:16\",\"uuid\":\"688b5647-4125-4fb4-8698-0270a8bbd56b\",\"dataType\":\"VSAT\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"10.2.2.3\",\"dest_ip\":\"10.2.2.3\",\"source_id\":\"94938\",\"data_type\":\"Email\",\"format\":\".POP3\"},\"status\":1,\"startTime\":\"2022-10-28 00:00:00\",\"endTime\":\"2022-10-29 00:00:00\"},{\"id\":\"635b75069b933e1adcff9f72\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 13:21:58\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-28 13:22:27\",\"uuid\":\"b5fdb7e8-05e2-4af9-99c4-60115e843d05\",\"dataType\":\"VSAT\",\"processType\":\"FUSION\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"10.2.2.3\",\"dest_ip\":\"10.2.2.3\",\"source_id\":\"94937\",\"data_type\":\"Web\",\"format\":\".doc\"},\"status\":1,\"startTime\":\"2022-10-29 00:00:00\",\"endTime\":\"2022-10-31 00:00:00\"},{\"id\":\"635b7c119b933e1adcff9f73\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 13:52:01\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 13:52:01\",\"uuid\":\"5c162acf-f9fb-4506-86ac-3b9b38af5e72\",\"dataType\":\"AIS\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"AIS-01\",\"detailConfig\":{\"mmsi\":\"120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138\"},\"status\":1,\"startTime\":\"2022-10-20 00:00:00\",\"endTime\":\"2022-10-28 00:00:00\"},{\"id\":\"635b7d219b933e1adcff9f74\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 13:56:33\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 13:56:33\",\"uuid\":\"2e308e80-4a39-4ccf-ab14-88a18301ad65\",\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"1.1.1.1,1.1.1.2,1.1.1.3,1.1.1.4,1.1.1.5,1.1.1.6,1.1.1.7,1.1.1.8,1.1.1.9,1.1.1.10,1.1.1.11,1.1.1.12,1.1.1.13,1.1.1.14,1.1.1.15,1.1.1.16,1.1.1.17,1.1.1.18,1.1.1.19\",\"dest_ip\":\"1.1.2.1,1.1.2.2,1.1.2.3,1.1.2.4,1.1.2.5,1.1.2.6,1.1.2.7,1.1.2.8,1.1.2.9,1.1.2.10,1.1.2.11,1.1.2.12,1.1.2.13,1.1.2.14,1.1.2.15,1.1.2.16,1.1.2.17,1.1.2.18,1.1.2.19\",\"source_id\":\"94939\",\"data_type\":\"Audio, Video, Web, Email, Transfer file, Undefined\",\"format\":\".mp3, .wav, .amr, .g711u, .g711a, .g711mu, .g722, .g7221, .g723, .g7231, .g728, .g729, .g719, .mp4, .ts, .h263, .h263+, .h264, .H264MC, .H264 polycom, .html, .txt, .json, .doc, .docx, .xls, .xlsx, .pdf, .pptx, .git, .3GP, .WMV, .AVI, .MJPEG, .mov, .flv, .POP3, .SMTP, .IMAP4, .Coremail, .UDP, .TCP, .AH, .eml, .ppt, .tiff, .zip, .rar, .jpeg, .jpg, .png, .xml, .pcap, .ESP, .bmp, .g726, .g727\"},\"status\":0,\"startTime\":\"2022-10-28 00:00:00\",\"endTime\":\"2022-10-29 00:00:00\"},{\"id\":\"635b9210eab3f25799aee80d\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 15:25:52\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 15:25:52\",\"uuid\":\"ab4e88b8-2835-4a9f-be92-c60f0221193e\",\"dataType\":\"SATELLITE\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"1 2, 3 4, 5 6\"},\"status\":1,\"startTime\":\"2022-10-01 00:00:00\",\"endTime\":\"2022-10-20 00:00:00\"}]}}";
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
                            
                            List<DataProcessConfig> dataProcessConfigs2 = filterConfigProcessForSatelliteImage(dataProcessConfigs);
//                            if( dataProcessConfigs2 == null || dataProcessConfigs2.isEmpty() )
//                                LOGGER.info("Rule approve for SI is not exists, accept all record!");
                           
                            long now = System.currentTimeMillis();
                            for( SatelliteImageMessageFull satelliteImageMessage : satelliteImageMessages ) {
                                try {
                                    if( dataProcessConfigs2 != null && !dataProcessConfigs2.isEmpty() ) {

                                        boolean pass = false;

                                        // Filter các điều kiện trong cấu hình ( theo tọa độ vùng )
                                        for( DataProcessConfig dataProcessConfig : dataProcessConfigs2 ) {
                                            try {
                                                String coordinates = dataProcessConfig.getDetailConfig().getCoordinates();

                                                if( !StringUtil.isNullOrEmpty(coordinates) ) {

                                                    // Tạo danh sách các tọa độ thuộc Vùng cấu hình.
                                                    String[] arr = coordinates.trim().split(",");
                                                    Coordinate[] coordinatesArea1 = new Coordinate[arr.length];
                                                    int i = 0;
                                                    for( String s : arr ) {
                                                        String[] arr2 = s.trim().split(" ");
                                                        coordinatesArea1[i] = new Coordinate(Double.parseDouble(arr2[0]), Double.parseDouble(arr2[1]));
                                                        i++;
                                                    }

                                                    /*
                                                        Tạo danh sách các tọa độ thuộc Vùng cắt ảnh
                                                        Từ  2 điểm:                                         19.2015, 114.1885 (1)(5)    20.4095, 115.1475 (3)
                                                        ==> suy ra 2 điểm còn lại theo hình vuông/chữ nhật: 19.2015, 115.1475 (2)       20.4095, 114.1885 (4)
                                                    */
                                                    Coordinate[] coordinatesArea2 = new Coordinate[] {
                                                        new Coordinate(satelliteImageMessage.getOriginLongitude(), satelliteImageMessage.getOriginLatitude())
                                                        , new Coordinate(satelliteImageMessage.getCornerLongitude(), satelliteImageMessage.getOriginLatitude())
                                                        , new Coordinate(satelliteImageMessage.getCornerLongitude(), satelliteImageMessage.getCornerLatitude())
                                                        , new Coordinate(satelliteImageMessage.getOriginLongitude(), satelliteImageMessage.getCornerLatitude())
                                                        , new Coordinate(satelliteImageMessage.getOriginLongitude(), satelliteImageMessage.getOriginLatitude())
                                                    };

                                                    // Check xem Vùng cắt ảnh có giao với Vùng cấu hình không?
                                                    if( PointUtil.geoPolygonIntersect(coordinatesArea1, coordinatesArea2) ) {
                                                        pass = true;
                                                        break;
                                                    }
                                                }
                                            } catch (Exception e) {
                                                LOGGER.error("ex: {}", e);
                                            }
                                        }
                                        if( !pass ) {
                                            LOGGER.info("satellite image raw [ {} ] not match rule, continue ...", satelliteImageMessage.getUuidKey());
                                            continue;
                                        }
                                    }

                                    String msgAsJson = JSONConverter.toJSON(satelliteImageMessage);

                                    ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send(
                                            kafkaTopicSinkSatelliteImageRawRefined, satelliteImageMessage.getUuidKey(), msgAsJson
                                    );
                                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                                        @Override
                                        public void onSuccess(SendResult<String, String> result) {

                                            LOGGER.info("Sent OK -> [" + kafkaTopicSinkSatelliteImageRawRefined + "], offset [" + result.getRecordMetadata().offset() + "], key [" + result.getProducerRecord().key() + "] partition [" + result.getRecordMetadata().partition() + "]");

                                            // dispatcherProcessService.updateProcessStatus(MetacenProcessStatus.NOT_PROCESS.code(), satelliteImageMessage.getUuidKey(), "satellite_image_data_analyzed");

                                            //TODO: Tạo dữ liệu thống kê cho bản ghi này với trạng thái là `Chưa Xử Lý`
                                            /*if( satelliteImageMessage.getRetryNum() == null || satelliteImageMessage.getRetryNum() == 0 ) {
                                                rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE
                                                                                , JSONConverter.toJSON(
                                                                                        new DataProcessLogging(satelliteImageMessage.getUuidKey()
                                                                                        , ProcessTypes.SATELLITE_ANALYTICS_RAW.type(), satelliteImageMessage.getCaptureTime()
                                                                                        , System.currentTimeMillis(), MetacenProcessStatus.NOT_PROCESS.code()
                                                                                )
                                                ));
                                            }*/
                                            
                                            insertLoggingProcess(satelliteImageMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_RAW.type()
                                                                , satelliteImageMessage.getCaptureTime(), now, MetacenProcessStatus.NOT_PROCESS.code());
                                        }

                                        @Override
                                        public void onFailure(Throwable ex) {
                                            LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                                        }
                                    });
                                } catch (Exception e) {
                                    LOGGER.error("ex: {}", e);
                                }
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
    
    @Override
    public void run(String... args) throws Exception {
        try {
            this.jobQueueService.registerJobQueue(Constant.REDIS_JOB_QUEUE_SATELLITE_IMAGE);
            
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdSatelliteImageRaw).start();
            
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdSatelliteImageRawRetry).start();
            
            LOGGER.info("Begin kafka consumer for satellite image raw");
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }
    
    @Bean
    public TaskScheduler satelliteImageTaskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(6);
        return scheduler;
    }
    
    private List<DataProcessConfig> filterConfigProcessForSatelliteImage(List<DataProcessConfig> dataProcessConfigs) {
        
        if( dataProcessConfigs == null || dataProcessConfigs.isEmpty() )
            return null;
        
        Predicate<DataProcessConfig> streamsPredicate = dataProcessConfig
                -> ( "SATELLITE".equals(dataProcessConfig.getDataType()) && "SATELLITE_ANALYTICS".equals(dataProcessConfig.getProcessType())
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
                    LOGGER.info("Sent OK -> [" + kafkaSinkTopicDataAnalyzedReport + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
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
