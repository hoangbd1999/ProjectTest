/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.comment.messaging.rabbitmq;

/**
 *
 * @author Admin
 */

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Link management
 *
 * @author kiven·ing
 */
@Configuration
public class RabbitMQConfig {

    //RabbitMQ 1
    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    //RabbitMQ 2
    @Value("${spring.rabbitmq2.host}")
    private String host2;

    @Value("${spring.rabbitmq2.port}")
    private int port2;

    @Value("${spring.rabbitmq2.username}")
    private String username2;

    @Value("${spring.rabbitmq2.password}")
    private String password2;

    //ConnectionFactory rabbitmq 1
    @Bean("connectionFactory")
    @Primary
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setChannelCacheSize(40);
        return connectionFactory;
    }

    //ConnectionFactory rabbitmq 2
    @Bean("connectionFactory2")
    public ConnectionFactory connectionFactory2() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername(username2);
        connectionFactory.setPassword(password2);
        connectionFactory.setHost(host2);
        connectionFactory.setPort(port2);
        connectionFactory.setChannelCacheSize(40);
        return connectionFactory;
    }

    @Bean(name = "rabbitAdmin")
    @Primary
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean(name = "rabbitAdmin2")
    public RabbitAdmin rabbitAdmin2() {
        return new RabbitAdmin(connectionFactory2());
    }

    @Bean(name = "rabbitTemplate")
    @Primary
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        //rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        //rabbitTemplate.setReceiveTimeout(TimeUnit.SECONDS.toMillis(10));
        rabbitTemplate.setReplyTimeout(TimeUnit.SECONDS.toMillis(20));
        return rabbitTemplate;
    }

    @Bean(name = "rabbitTemplate2")
    public RabbitTemplate rabbitTemplate2() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory2());
        //rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        //rabbitTemplate.setReceiveTimeout(TimeUnit.SECONDS.toMillis(10));
        rabbitTemplate.setReplyTimeout(TimeUnit.SECONDS.toMillis(20));
        return rabbitTemplate;
    }

    @Bean("directAutoDeleteQueue")
    public Queue directAutoDeleteQueue() {
        return new AnonymousQueue();
    }
}
