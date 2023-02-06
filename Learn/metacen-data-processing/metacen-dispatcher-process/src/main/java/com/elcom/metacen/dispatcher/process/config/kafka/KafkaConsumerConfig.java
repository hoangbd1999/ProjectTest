package com.elcom.metacen.dispatcher.process.config.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

/**
 *
 * @author Admin
 */

@Configuration
public class KafkaConsumerConfig {
    
    @Value("${kafka.bootstrap.servers}")
    private String kafkaBootstrapServers;
    
//    @Value("${kafka.consumer.groupId}")
//    private String kafkaConsumerGroupId;
    
    @Value("${kafka.consumer.autoOffsetReset}")
    private String kafkaConsumerAutoOffsetReset;
    
    @Value("${kafka.consumer.listener.concurrency}")
    private int kafkaConsumerListenerConcurrency;
    
    /*@Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerAutoOffsetReset);
        return new DefaultKafkaConsumerFactory<>(props);
    }*/
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100); // default 5000 (5s)
        
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 120000); // default 30000 (30s)
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 90000); // default 45000 (45s)
        
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000); // default 300000 (5m)
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 250); // default 500
        
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 15000); // default 500
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 30000); // default 3000
        
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerAutoOffsetReset);
        
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(kafkaConsumerListenerConcurrency);
        return factory;
    }
}
