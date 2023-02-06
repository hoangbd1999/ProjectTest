package com.elcom.metacen.satelliteimage.process.processor;

import com.elcom.metacen.dto.DataProcessLogging;
import com.elcom.metacen.enums.MetacenProcessStatus;
import com.elcom.metacen.enums.ProcessTypes;
import com.elcom.metacen.satelliteimage.process.model.kafka.SatelliteImageCompareObjectInfo;
import com.elcom.metacen.satelliteimage.process.model.kafka.SatelliteImageRawObjectInfo;
import com.elcom.metacen.satelliteimage.process.model.kafka.consumer.SatelliteImageCompareProcessedMessage;
import com.elcom.metacen.satelliteimage.process.model.kafka.consumer.SatelliteImageRawMessageFull;
import com.elcom.metacen.satelliteimage.process.model.kafka.consumer.SatelliteImageRawProcessedMessage;
import com.elcom.metacen.satelliteimage.process.model.kafka.producer.SatelliteImageRawMessageSimplify;
import com.elcom.metacen.satelliteimage.process.utils.JSONConverter;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import com.elcom.metacen.satelliteimage.process.utils.StringUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.elcom.metacen.satelliteimage.process.service.ProcessService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * @author Admin
 */
@Component
public class TaskLauncherProcessSatelliteImage implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLauncherProcessSatelliteImage.class);
    
    private static final long GMT_TIME_SUBTRACT = 7L * 60L * 60L * 1000L;

    @Value("${kafka.topic.sink.data.analyzed.report}")
    private String kafkaSinkTopicDataAnalyzedReport;
    
    @Value("${kafka.consumer.id.satellite.image.raw.refined}")
    private String kafkaConsumerIdSatelliteImageRefined;
    
    @Value("${kafka.consumer.id.satellite.image.raw.processed}")
    private String kafkaConsumerIdSatelliteImageRawProcessed;
    
    @Value("${kafka.consumer.id.satellite.image.compare.processed}")
    private String kafkaConsumerIdSatelliteImageCompareProcessed;
    
    @Value("${kafka.topic.sink.satellite.image.raw.to.process}")
    private String kafkaTopicSinkSatelliteImageToProcess;
    
    @Value("${kafka.topic.sink.satellite.image.raw.retry}")
    private String kafkaTopicSinkSatelliteImageRetry;
    
    @Value("${kafka.topic.sink.satellite.image.raw.final}")
    private String kafkaTopicSinkSatelliteImageRawFinal;
    
    // @Value("${satellite.image.scan.folder}")
    // private String satelliteImageFolderScan;
    
    @Value("${satellite.image.raw.objects.final.folder}")
    private String satelliteImageObjectsFinalFolder;
    
    @Value("${kafka.topic.sink.satellite.image.compare.final}")
    private String kafkaTopicSinkSatelliteImageCompareFinal;
    
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    //@Autowired
    //private RabbitMQClient rabbitMQClient;
    
    @Autowired
    private ProcessService processService;
    
    @KafkaListener(id = "${kafka.consumer.id.satellite.image.raw.refined}", groupId = "${kafka.consumer.groupId.satellite.image.raw.refined}"
                , topics = "${kafka.topic.source.satellite.image.raw.refined}", autoStartup = "false")
    protected void onMessageSatelliteImageRefined(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload String message
                                                , @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        if( StringUtil.isNullOrEmpty(key) || StringUtil.isNullOrEmpty(message) ) {
            LOGGER.error("key or message is invalid. key [ {} ], message [ {} ] ", key, message);
            return;
        }
        
        SatelliteImageRawMessageFull satelliteImageMessageRefined = JSONConverter.toObject(message, SatelliteImageRawMessageFull.class);
        if( satelliteImageMessageRefined == null || StringUtil.isNullOrEmpty(satelliteImageMessageRefined.getUuidKey()) ) {
            LOGGER.error("model after parse not valid, message [ {} ] ", message);
            return;
        }
        
        LOGGER.info("(Refined) Received from partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, key);

        if( satelliteImageMessageRefined.getRetryNum() == null || satelliteImageMessageRefined.getRetryNum() == 0 ) {
            
            satelliteImageMessageRefined.setProcessStatus(MetacenProcessStatus.PROCESSING.code());
            
            if( this.processService.insertSatelliteImageDataProcess(satelliteImageMessageRefined) )
                this.sendKafkaMessage(satelliteImageMessageRefined);
            else
                LOGGER.error("Init new record err, uuidKey: [{}]", key);
        } else
            this.sendKafkaMessage(satelliteImageMessageRefined);
    }
    
    @KafkaListener(id = "${kafka.consumer.id.satellite.image.raw.processed}", groupId = "${kafka.consumer.groupId.satellite.image.raw.processed}"
                , topics = "${kafka.topic.source.satellite.image.raw.processed}", autoStartup = "false")
    protected void onMessageSatelliteImageRawProcessed(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        SatelliteImageRawProcessedMessage satelliteImageProcessedMessage = JSONConverter.toObject(message, SatelliteImageRawProcessedMessage.class);
        if( satelliteImageProcessedMessage == null || StringUtil.isNullOrEmpty(satelliteImageProcessedMessage.getUuidKey()) )
            return;

        LOGGER.info("(Raw.processed) Received from partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, satelliteImageProcessedMessage.getUuidKey());
        
//        satelliteImageProcessedMessage.setProcessStatus(Boolean.FALSE);
        
        // Nếu lỗi thì bắn bản tin về kafka topic `SATELLITE_IMAGE_RAW_RETRY` để xử lý lại từ dispatcher
        if( satelliteImageProcessedMessage.getProcessStatus() == null || !satelliteImageProcessedMessage.getProcessStatus() ) {
            SatelliteImageRawMessageSimplify satelliteImageMessageRetry = new SatelliteImageRawMessageSimplify(
                satelliteImageProcessedMessage.getUuidKey(), satelliteImageProcessedMessage.getRootDataFolderPath(), satelliteImageProcessedMessage.getOriginLongitude()
              , satelliteImageProcessedMessage.getOriginLatitude(), satelliteImageProcessedMessage.getCornerLongitude(), satelliteImageProcessedMessage.getCornerLatitude()
              , satelliteImageProcessedMessage.getRetryNum() + 1
            );
            String msgAsJson = JSONConverter.toJSON(satelliteImageMessageRetry);

            ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                    this.kafkaTopicSinkSatelliteImageRetry, satelliteImageProcessedMessage.getUuidKey(), msgAsJson
            );
            sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {

                    LOGGER.info("Sent OK -> [" + kafkaTopicSinkSatelliteImageRetry + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");

                    processService.updateSatelliteImageRawProcessStatus(MetacenProcessStatus.ERROR.code(), satelliteImageProcessedMessage.getUuidKey());
                    
                    //TODO: Tạo dữ liệu thống kê cho bản ghi này với trạng thái là `Chưa Xử Lý`
                    /*this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE
                                                    , JSONConverter.toJSON(
                                                            new DataProcessLogging(satelliteImageProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS.type(), satelliteImageProcessedMessage.getCaptureTime(), System.currentTimeMillis(), MetacenProcessStatus.NOT_PROCESS.code()
                                                    )
                    ));*/
                }

                @Override
                public void onFailure(Throwable ex) {
                    LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                }
            });
        } else { // SUCCESS
            /*TODO:  -> Scan vào folder chứa ảnh vệ tinh để lấy ra các ảnh đối tượng bên trong.
                    -> Upload các file ảnh đối tượng lên NAS.
                    -> Tạo các bản ghi lấy được từ các file metadata và bắn vào kafka topic `SATELLITE_IMAGE_FINAL`.
                    -> Xóa thư mục chứa file ảnh vệ tinh trên server bên AI.
                    -> streamsets SDC và batch insert vào Clickhouse DB bảng `metacen.satellite_image_object_analyzed`.
            */
            
            // File scanFolder = new File(this.satelliteImageFolderScan + File.separator + satelliteImageProcessedMessage.getOriginalFolderName() + File.separator + satelliteImageObjectsFinalFolder);
            File scanFolder = new File(satelliteImageProcessedMessage.getRootDataFolderPath() + File.separator + satelliteImageObjectsFinalFolder);

            if( !scanFolder.exists() ) {
                LOGGER.warn("objectsDataFolder [ {} ] not existed, return...", scanFolder.getPath());
                return;
            }
            
            File[] metadataFiles = scanFolder.listFiles((file, s) -> s.endsWith(".txt"));
            if( metadataFiles == null || metadataFiles.length == 0 ) {
                LOGGER.error("metadataFilesLst is empty, return...");
                return;
            }
            
            File[] imageFiles = scanFolder.listFiles((file, s) -> s.endsWith(".jpg"));
            if( imageFiles == null || imageFiles.length == 0 ) {
                LOGGER.error("imageFilesLst is empty, return...");
                return;
            }
            
            // Upload an image files
            /*boolean uploadErr = false;
            for( File imageFile : imageFiles ) {
                if( !FileUtil.copyFileUsingChannelFast(imageFile, new File(satelliteImageProcessedMessage.getRootDataFolderPath() + File.separator + satelliteImageObjectsFinalFolder + File.separator + imageFile.getName())) ) {
                    uploadErr = true;
                    break;
                }
            }
            if( uploadErr ) {
                LOGGER.error("upload image files error, return...");
                return;
            }*/
            
            // Create kafka messages with metadata
            for( File metadataFile : metadataFiles ) {
                
                Integer height = 0;
                Integer width = 0;
                Float lat = null;
                Float lon = null;
                try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
                    String line = reader.readLine();
                    while ( line != null ) {
                        line = line.trim();
                        
                        if( line.contains("h:") )
                            height = Integer.parseInt(line.substring(line.indexOf(":") + 2));
                        else if( line.contains("w:") )
                            width = Integer.parseInt(line.substring(line.indexOf(":") + 2));
                        else if( line.contains("lat:") )
                            lat = Float.parseFloat(line.substring(line.indexOf(":") + 2));
                        else if( line.contains("lon:") )
                            lon = Float.parseFloat(line.substring(line.indexOf(":") + 2));
                        
                        // read next line
                        line = reader.readLine();
                    }
                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                    continue;
                }
                
                File srcImageFile = new File(metadataFile.getPath().replace(".txt", ".jpg"));
                if( !srcImageFile.exists() ) {
                    LOGGER.error("srcImageFile [ {} ] is not exists, continue...", srcImageFile.getPath());
                    continue;
                }
                
                SatelliteImageRawObjectInfo satelliteImageObjectInfo = new SatelliteImageRawObjectInfo(
                        satelliteImageProcessedMessage.getUuidKey(), width, height, lat, lon
                      , null, srcImageFile.getPath(), "MSA-01"
                );
                
                String msgAsJson = JSONConverter.toJSON(satelliteImageObjectInfo);

                ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                        this.kafkaTopicSinkSatelliteImageRawFinal, satelliteImageProcessedMessage.getUuidKey(), msgAsJson
                );
                sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        LOGGER.info("Sent OK -> [" + kafkaTopicSinkSatelliteImageRawFinal + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                    }
                    @Override
                    public void onFailure(Throwable ex) {
                        LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                    }
                });
            }
            
            this.processService.updateSatelliteImageRawProcessStatus(MetacenProcessStatus.SUCCESS.code(), satelliteImageProcessedMessage.getUuidKey());
            
            //TODO: Tạo dữ liệu thống kê cho bản ghi này với trạng thái là `Xử lý thành công`
            // this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_RAW.type(), satelliteImageProcessedMessage.getCaptureTime(), System.currentTimeMillis(), MetacenProcessStatus.SUCCESS.code())));
            insertLoggingProcess(satelliteImageProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_RAW.type()
                                , satelliteImageProcessedMessage.getCaptureTime(), System.currentTimeMillis(), MetacenProcessStatus.SUCCESS.code());
            
            // Xóa folder gốc bên AI
            /*File scanRootFolder = new File(this.satelliteImageFolderScan + File.separator + satelliteImageProcessedMessage.getOriginalFolderName());

            try {

                // Files.delete(FileSystems.getDefault().getPath( scanRootFolder.getPath() ));

                LOGGER.info("Delete scanRootFolder [ {} ]: {}", scanRootFolder.getPath(), FileUtil.deleteDirectoryNonEmptyRecursively2(scanRootFolder));

            } catch (Exception ex) {
                LOGGER.error("Delete scanRootFolder [ " + scanRootFolder.getPath() + " ] FAILED!");
            }*/
        }
    }
    
    @KafkaListener(id = "${kafka.consumer.id.satellite.image.compare.processed}", groupId = "${kafka.consumer.groupId.satellite.image.compare.processed}"
                , topics = "${kafka.topic.source.satellite.image.compare.processed}", autoStartup = "false")
    protected void onMessageSatelliteImageCompareProcessed(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
        
        SatelliteImageCompareProcessedMessage satelliteImageCompareProcessedMessage = JSONConverter.toObject(message, SatelliteImageCompareProcessedMessage.class);
        if( satelliteImageCompareProcessedMessage == null || StringUtil.isNullOrEmpty(satelliteImageCompareProcessedMessage.getUuidKey()) )
            return;

        LOGGER.info("(Compare.processed) Received from partition: [{}], offset: [{}], uuidKey: [{}]", partition, offset, satelliteImageCompareProcessedMessage.getUuidKey());
        
        // Nếu lỗi thì bắn bản tin về kafka topic `SATELLITE_IMAGE_COMPARE_RETRY` để xử lý lại từ dispatcher
        if( satelliteImageCompareProcessedMessage.getProcessStatus() != null && satelliteImageCompareProcessedMessage.getProcessStatus() ) {
            
            /*TODO:  -> Scan vào folder chứa các file kết quả so sánh.
                    -> Tạo các bản ghi lấy được từ các file metadata và bắn vào kafka topic `SATELLITE_IMAGE_CHANGES_RESULT`.
                    -> streamsets SDC và batch insert vào Clickhouse DB bảng `metacen.satellite_image_changes_result`.
            */
            
            File resultFolder = new File(satelliteImageCompareProcessedMessage.getResultFolder());
            File[] files = resultFolder.listFiles();
            // Không có sự thay đổi nào khi kết thúc so sánh, folder rỗng.
            if( files == null || files.length == 0 ) {
                LOGGER.info("resultFolder is empty, no change detected");
                this.processService.updateSatelliteImageCompareProcessStatus(MetacenProcessStatus.SUCCESS.code(), satelliteImageCompareProcessedMessage.getUuidKey());
                // this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type(), null, System.currentTimeMillis(), MetacenProcessStatus.SUCCESS.code())));
                insertLoggingProcess(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type()
                                    , null, System.currentTimeMillis(), MetacenProcessStatus.SUCCESS.code());
                return;
            }
                
            File[] metadataFiles = resultFolder.listFiles((file, s) -> s.endsWith(".txt"));
            if( metadataFiles == null || metadataFiles.length == 0 ) {
                this.processService.updateSatelliteImageCompareProcessStatus(MetacenProcessStatus.ERROR.code(), satelliteImageCompareProcessedMessage.getUuidKey());
                // this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type(), null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code())));
                insertLoggingProcess(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type()
                                    , null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code());
                LOGGER.error("metadataFilesLst is empty, return...");
                return;
            }
            
            File[] imageFiles = resultFolder.listFiles((file, s) -> s.endsWith(".jpg"));
            if( imageFiles == null || imageFiles.length == 0 ) {
                this.processService.updateSatelliteImageCompareProcessStatus(MetacenProcessStatus.ERROR.code(), satelliteImageCompareProcessedMessage.getUuidKey());
                // this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type(), null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code())));
                insertLoggingProcess(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type()
                                    , null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code());
                LOGGER.error("imageFiles is empty, return...");
                return;
            }
            
            try {
                // Create kafka messages with metadata
                for( File metadataFile : metadataFiles ) {

                    Float originLon = null;
                    Float originLat = null;
                    Float cornerLon = null;
                    Float cornerLat = null;
                    Integer width = 0;
                    Integer height = 0;

                    try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
                        String line = reader.readLine();
                        while ( line != null ) {
                            line = line.trim();

                            if( line.contains("Origin Longitude =") )
                                originLon = Float.parseFloat(line.substring(line.indexOf("=") + 2));
                            else if( line.contains("Origin Latitude =") )
                                originLat = Float.parseFloat(line.substring(line.indexOf("=") + 2));
                            if( line.contains("Corner Longitude =") )
                                cornerLon = Float.parseFloat(line.substring(line.indexOf("=") + 2));
                            else if( line.contains("Corner Latitude =") )
                                cornerLat = Float.parseFloat(line.substring(line.indexOf("=") + 2));
                            else if( line.contains("Width =") )
                                width = Integer.parseInt(line.substring(line.indexOf("=") + 2));
                            else if( line.contains("Height =") )
                                height = Integer.parseInt(line.substring(line.indexOf("=") + 2));

                            // read next line
                            line = reader.readLine();
                        }
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                        continue;
                    }

                    File imageFileOrigin = new File(metadataFile.getPath().replace("_infor.txt", "_origin.jpg"));
                    if( !imageFileOrigin.exists() ) {
                        LOGGER.error("imageFileOrigin [ {} ] is not exists, continue...", imageFileOrigin.getPath());
                        continue;
                    }

                    File imageFileCompare = new File(metadataFile.getPath().replace("_infor.txt", "_compare.jpg"));
                    if( !imageFileCompare.exists() ) {
                        LOGGER.error("imageFileCompare [ {} ] is not exists, continue...", imageFileCompare.getPath());
                        continue;
                    }

                    SatelliteImageCompareObjectInfo satelliteImageCompareObjectInfo = new SatelliteImageCompareObjectInfo(
                            satelliteImageCompareProcessedMessage.getUuidKey(), originLon, originLat, cornerLon, cornerLat, width, height
                            , imageFileOrigin.getPath(), imageFileCompare.getPath()
                    );

                    String msgAsJson = JSONConverter.toJSON(satelliteImageCompareObjectInfo);

                    ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                            this.kafkaTopicSinkSatelliteImageCompareFinal, satelliteImageCompareProcessedMessage.getUuidKey(), msgAsJson
                    );
                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {
                            LOGGER.info("Sent OK -> [" + kafkaTopicSinkSatelliteImageCompareFinal + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");
                        }
                        @Override
                        public void onFailure(Throwable ex) {
                            LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                        }
                    });
                }
                
                this.processService.updateSatelliteImageCompareProcessStatus(MetacenProcessStatus.SUCCESS.code(), satelliteImageCompareProcessedMessage.getUuidKey());
            
                //TODO: Tạo dữ liệu thống kê cho bản ghi này với trạng thái là `Xử lý thành công`
                // this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type(), null, System.currentTimeMillis(), MetacenProcessStatus.SUCCESS.code())));
                insertLoggingProcess(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type()
                                    , null, System.currentTimeMillis(), MetacenProcessStatus.SUCCESS.code());
            } catch (Exception e) {
                LOGGER.error("ex: ", e);
                this.processService.updateSatelliteImageCompareProcessStatus(MetacenProcessStatus.ERROR.code(), satelliteImageCompareProcessedMessage.getUuidKey());
                // this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type(), null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code())));
                insertLoggingProcess(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type()
                                    , null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code());
            }
        } else {
            LOGGER.info("(Compare.processed) processStatus is false, uuidKey [ {} ]", satelliteImageCompareProcessedMessage.getUuidKey());
            this.processService.updateSatelliteImageCompareProcessStatus(MetacenProcessStatus.ERROR.code(), satelliteImageCompareProcessedMessage.getUuidKey());
            // this.rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type(), null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code())));
            insertLoggingProcess(satelliteImageCompareProcessedMessage.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type()
                                , null, System.currentTimeMillis(), MetacenProcessStatus.ERROR.code());
        }
    }
    
    private void sendKafkaMessage(SatelliteImageRawMessageFull satelliteImageMessageRefined) {
        
        String msgAsJson = JSONConverter.toJSON(satelliteImageMessageRefined);

        ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                this.kafkaTopicSinkSatelliteImageToProcess, satelliteImageMessageRefined.getUuidKey() + "", msgAsJson
        );
        sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {

                LOGGER.info("Sent OK -> [" + kafkaTopicSinkSatelliteImageToProcess + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");

                // processService.updateProcessStatus(MetacenProcessStatus.PROCESSING.code(), satelliteImageMessageRefined.getUuidKey());

                //TODO: -> Tạo dữ liệu thống kê cho bản ghi này với trạng thái là `Đang Xử Lý`
                // rabbitMQClient.callWorkerService(RabbitMQProperties.DATA_PROCESS_LOGGING_WORKER_QUEUE, JSONConverter.toJSON(new DataProcessLogging(satelliteImageMessageRefined.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_RAW.type(), satelliteImageMessageRefined.getCaptureTime(), System.currentTimeMillis(), MetacenProcessStatus.PROCESSING.code())));
                insertLoggingProcess(satelliteImageMessageRefined.getUuidKey(), ProcessTypes.SATELLITE_ANALYTICS_RAW.type()
                                    , satelliteImageMessageRefined.getCaptureTime(), System.currentTimeMillis(), MetacenProcessStatus.PROCESSING.code());
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
            }
        });
    }
    
    // @Scheduled(cron = "${process.satellite.image.retry.for.timeout.scheduled}")
    @Scheduled( fixedRate = 1200000 ) // 20 minutes
    protected void processingSatelliteImageTimeout() {
        
        List<SatelliteImageRawMessageSimplify> satelliteImageMessageSimplifies = this.processService.getLstSatelliteImageTimeoutProcess();
        
        if( satelliteImageMessageSimplifies != null && !satelliteImageMessageSimplifies.isEmpty() ) {
            
            for( SatelliteImageRawMessageSimplify satelliteImageMessageSimplify : satelliteImageMessageSimplifies ) {
                
                try {
                    if( satelliteImageMessageSimplify.getRetryNum() == null ) {
                        LOGGER.error("Fail to get retryNum item for process timeOut record!");
                        continue;
                    }

                    satelliteImageMessageSimplify.setRetryNum(satelliteImageMessageSimplify.getRetryNum() + 1);

                    String msgAsJson = JSONConverter.toJSON(satelliteImageMessageSimplify);

                    ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(
                            this.kafkaTopicSinkSatelliteImageRetry, satelliteImageMessageSimplify.getUuidKey(), msgAsJson
                    );
                    sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {

                            LOGGER.info("[TimeOut] Sent OK -> [" + kafkaTopicSinkSatelliteImageRetry + "], offset [" + result.getRecordMetadata().offset() + "], key: [" + result.getProducerRecord().key() + "], partition [" + result.getRecordMetadata().partition() + "]");

                            processService.increaseRetryTimesForSatelliteImage(satelliteImageMessageSimplify.getUuidKey());
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            LOGGER.error("Unable to send message = [" + msgAsJson + "], due to : " + ex.getMessage());
                        }
                    });
                    
                    try {
                        Thread.sleep(2000L);
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    }
                    
                } catch (Exception e) {
                    LOGGER.error("ex: ", e);
                }
            }
        }
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
            
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdSatelliteImageRefined).start();
            
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdSatelliteImageRawProcessed).start();
            
            this.kafkaListenerEndpointRegistry.getListenerContainer(this.kafkaConsumerIdSatelliteImageCompareProcessed).start();
            
            LOGGER.info("Begin kafka consumer for satellite image process");
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
    }
    
    private void insertLoggingProcess(String refUuidKey, String processTypes, Long eventTime, Long processTime, Integer processStatus) {
	try {
            String msgAsJson = JSONConverter.toJSON(new DataProcessLogging(refUuidKey, processTypes, eventTime != null && !eventTime.equals(0L) ? eventTime : processTime - GMT_TIME_SUBTRACT, processTime, processStatus));
            ListenableFuture<SendResult<String, String>> sendFuture = this.kafkaTemplate.send(this.kafkaSinkTopicDataAnalyzedReport, refUuidKey, msgAsJson);
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
    
    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(6);
        return scheduler;
    }
}
