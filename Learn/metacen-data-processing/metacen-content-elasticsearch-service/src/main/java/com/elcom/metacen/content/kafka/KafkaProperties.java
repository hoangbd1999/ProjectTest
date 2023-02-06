package com.elcom.metacen.content.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaProperties {

    //Notify Trigger worker queue
    @Value("${elastic.topic.request}")
    public static String ELASTIC_TOPIC_REQUEST;

    @Value("${saga.worker.topic}")
    public static String SAGA_WORKER_TOPIC;

    @Autowired
    public KafkaProperties(
            @Value("${elastic.topic.request}") String contentRequestTopic,
            @Value("${saga.worker.topic}") String sagaTopic) {

        //Notify Trigger worker queue
        ELASTIC_TOPIC_REQUEST = contentRequestTopic;

        SAGA_WORKER_TOPIC = sagaTopic;
    }
}
