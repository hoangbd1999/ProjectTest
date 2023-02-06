/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Admin
 */
@Configuration
public class RpcConfig {

    @Value("${menumanagement.rpc.exchange}")
    private String exchange;
    @Value("${menumanagement.rpc.queue}")
    private String queue;
    @Value("${menumanagement.rpc.key}")
    private String key;

    @Bean("rpcQueue")
    public Queue rpcQueue() {
        return new Queue(queue);
    }

    @Bean("rpcExchange")
    public DirectExchange rpcExchange() {
        return new DirectExchange(exchange);
    }

    @Bean("rpcBinding")
    public Binding binding(DirectExchange rpcExchange, Queue rpcQueue) {
        return BindingBuilder.bind(rpcQueue).to(rpcExchange).with(key);
    }

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer();
    }
}
