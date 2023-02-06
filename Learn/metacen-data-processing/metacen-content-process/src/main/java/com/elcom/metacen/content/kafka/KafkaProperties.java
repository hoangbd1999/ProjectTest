package com.elcom.metacen.content.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaProperties {

    //Elastic queue
    @Value("${content.topic.reply}")
    public static String CONTENT_REPLY_TOPIC;

    //Notify Trigger worker queue
    @Value("${content.topic.request}")
    public static String CONTENT_TOPIC_REQUEST;

    @Value("${saga.worker.topic}")
    public static String SAGA_WORKER_TOPIC;

    @Autowired
    public KafkaProperties(@Value("${content.topic.reply}") String content_reply_topic,
            @Value("${content.topic.request}") String contentRequestTopic,
            @Value("${saga.worker.topic}") String sagaTopic) {

        //Elastic
        CONTENT_REPLY_TOPIC = content_reply_topic;

        //Notify Trigger worker queue
        CONTENT_TOPIC_REQUEST = contentRequestTopic;

        SAGA_WORKER_TOPIC = sagaTopic;
    }
}
