package com.elcom.metacen.report.kafka;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class KafkaProperties {

    @Value("${kafka.topic.elasticsearch}")
    public static String KAFKA_ELASTICSEARCH_TOPIC;

    @Value("${kafka.topic.notify-trigger}")
    public static String KAFKA_NOTIFY_TRIGGER_TOPIC;

    @Autowired
    public KafkaProperties(
            @Value("${kafka.topic.elasticsearch}") String kafkaElasticSeacrhTopic,
            @Value("${kafka.topic.notify-trigger}") String kafkaNotifyTriggerTopic
    ) {
        KAFKA_ELASTICSEARCH_TOPIC = kafkaElasticSeacrhTopic;
        KAFKA_NOTIFY_TRIGGER_TOPIC = kafkaNotifyTriggerTopic;

    }
}
