/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.uploadservice.messaging.rabbitmq;

import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class RabbitMQClient {

    private final Logger LOGGER = LoggerFactory.getLogger(RabbitMQClient.class);

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    @Qualifier("directAutoDeleteQueue")
    private Queue directAutoDeleteQueue;

    public String callRpcService(String exchangeName, String queueName, String key, String msg) {
        try {
            LOGGER.info("callRpcService - exchangeName: {}, queueName: {}, key : {}",
                    exchangeName, queueName, key);
            //Queue
            Queue queue = new Queue(queueName);
            addQueue(queue);
            //Exchange
            DirectExchange exchange = new DirectExchange(exchangeName);
            addExchange(exchange);
            //Binding
            addBinding(queue, exchange, key);

            //Send msg
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);
            return (String) amqpTemplate.convertSendAndReceive(exchangeName, key, message);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("callRpcService UnsupportedEncodingException >>> {}", ex.toString());
            ex.printStackTrace();
        }
        return null;
    }

    public boolean callPublishService(String exchangeName, String key, String msg) {
        LOGGER.info("callPublishService - exchangeName: {}, key : {}", exchangeName, key);
        //Exchange
        DirectExchange exchange = new DirectExchange(exchangeName);
        addExchange(exchange);
        //Binding
        addBinding(directAutoDeleteQueue, exchange, key);

        //Send msg
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        try {
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);
            amqpTemplate.convertAndSend(exchangeName, key, message);
        } catch (UnsupportedEncodingException | AmqpException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean callWorkerService(String queueName, String msg) {
        LOGGER.info("callWorkerService - queueName : {}", queueName);
        //Queue
        Queue queue = new Queue(queueName);
        addQueue(queue);

        //Send msg
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        try {
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);
            amqpTemplate.convertAndSend(queueName, message);
        } catch (UnsupportedEncodingException | AmqpException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private String addQueue(Queue queue) {
        return amqpAdmin.declareQueue(queue);
    }

    private void addExchange(AbstractExchange exchange) {
        amqpAdmin.declareExchange(exchange);
    }

    private void addBinding(Queue queue, DirectExchange exchange, String routingKey) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        amqpAdmin.declareBinding(binding);
    }
}
