/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.messaging.rabbitmq;

import com.elcom.metacen.id.model.dto.DeleteDataPublishMessage;
import com.elcom.metacen.id.service.UnitService;
import com.elcom.metacen.message.RequestMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author admin
 */
public class SubscriberServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberServer.class);
    
    
    @Autowired
    private UnitService unitService;
    

    
    @RabbitListener(queues = "#{directAutoDeleteSubscriberQueue.name}")
    public void subscriberReceiveDeleteData(String json) {
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            DeleteDataPublishMessage deleteDataMessage = mapper.readValue(json, DeleteDataPublishMessage.class);
            //Process here
            if (deleteDataMessage != null) {
                

                switch (deleteDataMessage.getDataType()) {
                    case "STAGE":
                        handleDeleteStageMessage(deleteDataMessage);
                        break;
                    
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    private void handleDeleteStageMessage(DeleteDataPublishMessage deleteDataMessage){
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        List<String> listStageIds = mapper.convertValue(deleteDataMessage.getData(), new TypeReference<List<String>>(){});
        unitService.deleteStageData(listStageIds);
    }
}
