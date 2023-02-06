/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enrich.data.messaging.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author admin
 */
@Configuration
public class SubscriberConfig {

    @Value("${user.direct.exchange}")
    private String directExchange;

    @Value("${user.direct.key}")
    private String directKey;

    @Bean("directExchange")
    public DirectExchange directExchange() {
        return new DirectExchange(directExchange);
    }

    @Bean("directAutoDeleteSubscriberQueue")
    public Queue directAutoDeleteSubscriberQueue() {
        return new AnonymousQueue();
    }

    @Bean("directBinding")
    public Binding bindingDirect(DirectExchange directExchange, Queue directAutoDeleteSubscriberQueue) {
        return BindingBuilder.bind(directAutoDeleteSubscriberQueue).to(directExchange).with(directKey);
    }

    @Bean
    public SubscriberServer subscriberServer() {
        return new SubscriberServer();
    }
}
