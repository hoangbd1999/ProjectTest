package com.elcom.metacen.saga.kafka;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
public class KafkaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClient.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AdminClient adminClient;

    private static final int NUM_PARTITION = 10;

    public boolean callKafkaServerWorker(String topicRequest, String msg) {
        LOGGER.info("Call Worker Kafka Server Topic: {}, msg: {}", topicRequest, msg);
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topicRequest, msg);
            ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send(record);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public void recreateTopic(String topicName) {
        try {

            List<String> listTopic = new ArrayList<>();
            listTopic.add(topicName);
            adminClient.deleteTopics(listTopic);
            LOGGER.info("DELETE TOPIC {}", topicName);
            NewTopic newTopic = new NewTopic(topicName, NUM_PARTITION, (short) 1);
            List<NewTopic> listNewTopics = new ArrayList<>();
            listNewTopics.add(newTopic);
            adminClient.createTopics(listNewTopics);
            LOGGER.info("CREATE TOPIC {}", topicName);

        } catch (Exception e) {
            return;
        }

    }
}
