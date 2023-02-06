/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.messaging.rabbitmq;

/**
 *
 * @author Admin
 */
import com.elcom.metacen.config.GatewayConfig;
import com.elcom.metacen.utils.StringUtil;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Link management
 *
 * @author kiven·ing
 */
@Configuration
public class RabbitMQConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean("connectionFactory")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setChannelCacheSize(40);

        // set the heartbeat timeout to 60 seconds
        connectionFactory.setRequestedHeartBeat(60);

        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        //rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        //rabbitTemplate.setReceiveTimeout(TimeUnit.SECONDS.toMillis(GatewayConfig.RPC_TIMEOUT));
        rabbitTemplate.setReplyTimeout(TimeUnit.SECONDS.toMillis(GatewayConfig.RPC_TIMEOUT));
        return rabbitTemplate;
    }

    @Bean("directAutoDeleteQueue")
    public Queue directAutoDeleteQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public void initAllExchangeAndQueueAndBinding() {
        LOGGER.info("Initing all rpc, pub/sub and worker queue...");
        /* RPC */
        AmqpAdmin amqpAdmin = rabbitAdmin();
        Queue directAutoDeleteQueue = directAutoDeleteQueue();
        List<String> serviceList = GatewayConfig.SERVICE_LIST;
        if (serviceList != null && !serviceList.isEmpty()) {
            LOGGER.info("Total: {} services", serviceList.size());
            for (String service : serviceList) {
                LOGGER.info("+ Init for service: {}", service);
                //RPC
                String rpcQueue = GatewayConfig.SERVICE_MAP.get(service + ".rpc.queue");
                String rpcExchange = GatewayConfig.SERVICE_MAP.get(service + ".rpc.exchange");
                String rpcKey = GatewayConfig.SERVICE_MAP.get(service + ".rpc.key");
                if (!StringUtil.isNullOrEmpty(rpcQueue) && !StringUtil.isNullOrEmpty(rpcExchange) && !StringUtil.isNullOrEmpty(rpcKey)) {
                    Queue queue = new Queue(rpcQueue);
                    amqpAdmin.declareQueue(queue);
                    //Exchange
                    DirectExchange exchange = new DirectExchange(rpcExchange);
                    amqpAdmin.declareExchange(exchange);
                    //Binding
                    Binding binding = BindingBuilder.bind(queue).to(exchange).with(rpcKey);
                    amqpAdmin.declareBinding(binding);
                }
                //Pub/Sub
                String directExchange = GatewayConfig.SERVICE_MAP.get(service + ".direct.exchange");
                String directKey = GatewayConfig.SERVICE_MAP.get(service + ".direct.key");
                if (!StringUtil.isNullOrEmpty(directExchange) && !StringUtil.isNullOrEmpty(directKey)) {
                    //Exchange
                    DirectExchange exchange = new DirectExchange(directExchange);
                    amqpAdmin.declareExchange(exchange);
                    //Binding
                    Binding binding = BindingBuilder.bind(directAutoDeleteQueue).to(exchange).with(directKey);
                    amqpAdmin.declareBinding(binding);
                }
                //Worker
                String workerQueue = GatewayConfig.SERVICE_MAP.get(service + ".worker.queue");
                if (!StringUtil.isNullOrEmpty(workerQueue)) {
                    Queue queue = new Queue(workerQueue);
                    amqpAdmin.declareQueue(queue);
                }
            }
        }
        LOGGER.info("Initing all successful");
    }
}
