package com.elcom.metacen.content.controller;

import com.elcom.metacen.content.dto.ContentMetaDataRequest;
import com.elcom.metacen.content.kafka.KafkaClient;
import com.elcom.metacen.content.kafka.KafkaProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@RestController
public class ProcessContentController {
    @Autowired
    private KafkaClient kafkaClient;
    @PostMapping("/v1.0/test/process")
    public ContentMetaDataRequest findByTitle(@RequestBody ContentMetaDataRequest data) throws ParseException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String msg = objectMapper.writeValueAsString(data);

        kafkaClient.callKafkaServerWorker(KafkaProperties.CONTENT_TOPIC_REQUEST,data.getContentDTOS().get(0).getUuidKey(),msg);
        return  data;
    }
}
