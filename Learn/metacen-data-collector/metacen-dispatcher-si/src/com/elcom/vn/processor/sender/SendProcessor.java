package com.elcom.vn.processor.sender;

import com.elcom.util.miscellaneous.thread.ActionThread;
import com.elcom.util.queue.BoundBlockingQueue;
import com.elcom.vn.config.AppConfig;
import com.elcom.vn.object.EventContainer;
import com.elcom.vn.utils.StringUtil;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;

public class SendProcessor extends ActionThread {

    private Producer kafkaProducer;
    
    private static final String KAFKA_BROKER;
    
    private static final String KAFKA_TOPIC_SATELLITE_RAW;
    
    protected final Logger logger;
    protected final BoundBlockingQueue<EventContainer> vsatQueue;
    
    static {
        
        KAFKA_BROKER = AppConfig.getConfig().getProperty("KAFKA_BROKER", "192.168.61.106:29092,192.168.61.106:29093,192.168.61.106:29094", "SETTINGS");
        
        KAFKA_TOPIC_SATELLITE_RAW = AppConfig.getConfig().getProperty("KAFKA_TOPIC", "SATELLITE_IMAGE_RAW", "SETTINGS");
    }
    
    public SendProcessor(String name, Logger logger, BoundBlockingQueue<EventContainer> vsatQueue) {
        super(name);
        logger.info("SendProcessor.init");
        
        this.logger = logger;
        this.vsatQueue = vsatQueue;

        if( this.kafkaProducer == null ) {
            try {
                Properties configProps = new Properties();
                configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
                configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
                configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
                configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "90000");
                this.kafkaProducer = new KafkaProducer<>(configProps);
                logger.info("kafka client producer is connected!");
            } catch (Exception e) {
                logger.error("kafka connect err: ", e);
            }
        }
        
    }

    @Override
    protected void onThrowable(Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Unexpected throwable", t);
        }
    }

    @Override
    protected void onExecuting() throws Throwable {
        if (logger.isInfoEnabled()) {
            logger.info("Running...");
        }
    }

    @Override
    protected void onKilling() {
        if (logger.isInfoEnabled()) {
            logger.info("Terminated.");
        }
    }

    @Override
    protected long sleeptime() throws Throwable {
        return -1;
    }

    @Override
    protected void action() throws Exception {
        
        EventContainer evt = vsatQueue.take();
        
        try {
            logger.info("msg: " + evt.messageAsJson);
            String topic = KAFKA_TOPIC_SATELLITE_RAW;
            try {
                this.kafkaProducer.send(new ProducerRecord<>(topic
                        , evt.messageUuidKey, evt.messageAsJson), (RecordMetadata rm, Exception e) -> {
//                    logger.info("publish to [{}] success, offset: {} ", topic, rm.offset() + ", partition: " + rm.partition());
                    if( e != null )
                        logger.error("publish to ["+topic+"] err, rm: {}, ex: {}", rm!=null?rm.offset():"null", StringUtil.printException(e));
                });
            } catch (Exception e) {
                logger.error("publish to ["+topic+"] err, ex: " + StringUtil.printException(e));
            }
        } catch (Exception e) {
            logger.error("ex: ", e);
        }
    }
}
