/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.comment.messaging.rabbitmq;

import com.elcom.metacen.message.RequestMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author admin
 */
public class SubscriberServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberServer.class);

    @RabbitListener(queues = "#{directAutoDeleteSubscriberQueue.name}")
    public void subscriberReceive(String json) {
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            //Process here
            if (request != null) {
                String requestPath = request.getRequestPath();
                String urlParam = request.getUrlParam();
                String pathParam = request.getPathParam();
                Map<String, Object> bodyParam = request.getBodyParam();
                Map<String, String> headerParam = request.getHeaderParam();
                LOGGER.info("requestPath: " + requestPath + ", urlParam: " + urlParam
                        + ", pathParam: " + pathParam);
                if (bodyParam != null && !bodyParam.isEmpty()) {
                    LOGGER.info("bodyParam");
                    Iterator<Map.Entry<String, Object>> iterator = bodyParam.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> entry = iterator.next();
                        LOGGER.info(entry.getKey() + " => " + entry.getValue());
                    }
                }
                if (headerParam != null && !headerParam.isEmpty()) {
                    LOGGER.info("headerParam");
                    Iterator<Map.Entry<String, String>> iterator = headerParam.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> entry = iterator.next();
                        LOGGER.info(entry.getKey() + " => " + entry.getValue());
                    }
                }

                switch (request.getRequestMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        if ("/user/log".equalsIgnoreCase(requestPath)) {
                            String log = null;
                            String type = null;
                            if (bodyParam != null && !bodyParam.isEmpty()) {
                                log = (String) bodyParam.get("log");
                                type = (String) bodyParam.get("type");
                            }
                            LOGGER.info("Log type : " + type + " with content : " + log);
                            //Process log
                        }
                        break;
                    case "DELETE":
                        break;
                    default:
                        break;
                }
            }
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
