/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.messaging.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Admin
 */
@Component
public class RabbitMQClient {

    private final Logger logger = LoggerFactory.getLogger(RabbitMQClient.class);

    @Autowired
    @Qualifier("rabbitAdmin")
    private AmqpAdmin amqpAdmin;

    @Autowired
    @Qualifier("rabbitAdmin2")
    private AmqpAdmin amqpAdmin2;

    @Autowired
    @Qualifier("rabbitTemplate")
    private AmqpTemplate amqpTemplate;

    @Autowired
    @Qualifier("rabbitTemplate2")
    private AmqpTemplate amqpTemplate2;

    @Autowired
    @Qualifier("directAutoDeleteQueue")
    private Queue directAutoDeleteQueue;

    //1
    public String callRpcService(String exchangeName, String queueName, String key, String msg) {
        logger.info("callRpcService - exchangeName: {}, queueName: {}, key : {}",
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
        Message message = new Message(msg.getBytes(), messageProperties);
        return (String) amqpTemplate.convertSendAndReceive(exchangeName, key, message);
    }

    public boolean callPublishService(String exchangeName, String key, String msg) {
        logger.info("callPublishService - exchangeName: {}, key : {}", exchangeName, key);
        //Exchange
        DirectExchange exchange = new DirectExchange(exchangeName);
        addExchange(exchange);
        //Binding
        addBinding(directAutoDeleteQueue, exchange, key);

        //Send msg
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        Message message = new Message(msg.getBytes(), messageProperties);

        try {
            amqpTemplate.convertAndSend(exchangeName, key, message);
        } catch (AmqpException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean callWorkerService(String queueName, String msg) {
        logger.info("callWorkerService - queueName : {}", queueName);
        //Queue
        Queue queue = new Queue(queueName);
        addQueue(queue);

        //Send msg
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        try {
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);
            amqpTemplate.convertAndSend(queueName, message);
        } catch (Exception ex) {
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

    //2
    public String callRpcService2(String exchangeName, String queueName, String key, String msg) {
        logger.info("callRpcService2 - exchangeName: {}, queueName: {}, key : {}",
                exchangeName, queueName, key);
        //Queue
        Queue queue = new Queue(queueName);
        addQueue2(queue);
        //Exchange
        DirectExchange exchange = new DirectExchange(exchangeName);
        addExchange2(exchange);
        //Binding
        addBinding2(queue, exchange, key);

        //Send msg
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        try {
            Message message;
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
            return (String) amqpTemplate2.convertSendAndReceive(exchangeName, key, message);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean callPublishService2(String exchangeName, String key, String msg) {
        logger.info("callPublishService2 - exchangeName: {}, key : {}", exchangeName, key);
        //Exchange
        DirectExchange exchange = new DirectExchange(exchangeName);
        addExchange2(exchange);
        //Binding
        addBinding2(directAutoDeleteQueue, exchange, key);

        //Send msg
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        Message message = new Message(msg.getBytes(), messageProperties);

        try {
            amqpTemplate2.convertAndSend(exchangeName, key, message);
        } catch (AmqpException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public String callRpcServiceByte(String exchangeName, String queueName, String key, String msg) {
        try {
            logger.info("callRpcService - exchangeName: {}, queueName: {}, key : {}",
                    exchangeName, queueName, key);
//            Queue
            Queue queue = new Queue(queueName);
            addQueue(queue);
//            Exchange
            DirectExchange exchange = new DirectExchange(exchangeName);
            addExchange(exchange);
//            Binding
            addBinding(queue, exchange, key);

//            Send msg
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);
            byte[] res = (byte[]) amqpTemplate.convertSendAndReceive(exchangeName, key, message);
            return new String(res, StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean callWorkerService2(String queueName, String msg) {
        logger.info("callWorkerService2 - queueName : {}", queueName);
        //Queue
        Queue queue = new Queue(queueName);
        addQueue2(queue);

        //Send msg
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        try {
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);
            amqpTemplate2.convertAndSend(queueName, message);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private String addQueue2(Queue queue) {
        return amqpAdmin2.declareQueue(queue);
    }

    private void addExchange2(AbstractExchange exchange) {
        amqpAdmin2.declareExchange(exchange);
    }

    private void addBinding2(Queue queue, DirectExchange exchange, String routingKey) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        amqpAdmin2.declareBinding(binding);
    }
}
