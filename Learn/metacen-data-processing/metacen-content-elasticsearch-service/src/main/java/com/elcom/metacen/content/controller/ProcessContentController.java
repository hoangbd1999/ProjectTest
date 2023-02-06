package com.elcom.metacen.content.controller;

import com.elcom.metacen.content.dto.ContentMetaDataRequest;
import com.elcom.metacen.content.dto.ElasticRequest;
import com.elcom.metacen.content.kafka.KafkaClient;
import com.elcom.metacen.content.kafka.KafkaProperties;
import com.elcom.metacen.content.model.VsatMediaAnalyzed;
import com.elcom.metacen.content.service.VsatMediaAnalyzedService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
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
    @Autowired
    private VsatMediaAnalyzedService vsatMediaAnalyzedService;
    @PostMapping("/v1.0/test/process")
    public ContentMetaDataRequest findByTitle(@RequestBody ContentMetaDataRequest data) throws ParseException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String msg = objectMapper.writeValueAsString(data);
        kafkaClient.callKafkaServerWorker(KafkaProperties.ELASTIC_TOPIC_REQUEST,msg);
        return  data;
    }
    @PostMapping("/v1.0/test/elasticsearch")
    public ElasticRequest testDo(@RequestBody ElasticRequest data) throws ParseException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String msg = objectMapper.writeValueAsString(data);
//        SearchHits<VsatMediaAnalyzed> t = vsatMediaAnalyzedService.test();

        kafkaClient.callKafkaServerWorker(KafkaProperties.ELASTIC_TOPIC_REQUEST,msg);
        return  data;
    }
    @PostMapping("/v1.0/test/filter")
    public ElasticRequest testFilter(@RequestBody ElasticRequest data) throws ParseException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String msg = objectMapper.writeValueAsString(data);
        SearchHits<VsatMediaAnalyzed> t = vsatMediaAnalyzedService.test();
        List<SearchHit<VsatMediaAnalyzed>> a =t.getSearchHits();
        List<VsatMediaAnalyzed> b = vsatMediaAnalyzedService.test1();

//        kafkaClient.callKafkaServerWorker(KafkaProperties.ELASTIC_TOPIC_REQUEST,msg);
        return  data;
    }
}
