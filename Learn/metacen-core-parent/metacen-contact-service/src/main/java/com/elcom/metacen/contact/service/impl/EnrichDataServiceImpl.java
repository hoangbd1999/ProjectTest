package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.contact.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.contact.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.contact.service.EnrichDataService;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class EnrichDataServiceImpl implements EnrichDataService {
    @Autowired private RabbitMQClient rabbitMQClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichDataServiceImpl.class);

    @Override
    public Boolean isExistObjectAnalyzed(Map<String, String> headerMap, Map<String, Object> bodyMap) {
        RequestMessage userRpcRequest = new RequestMessage();
        userRpcRequest.setRequestMethod("POST");
        userRpcRequest.setRequestPath(RabbitMQProperties.ENRICH_DATA_ANALYZED_OBJECT_URL);
        userRpcRequest.setVersion(ResourcePath.VERSION);
        userRpcRequest.setBodyParam(bodyMap);
        userRpcRequest.setUrlParam(null);
        userRpcRequest.setHeaderParam(headerMap);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ENRICH_DATA_RPC_EXCHANGE,
                RabbitMQProperties.ENRICH_DATA_RPC_QUEUE, RabbitMQProperties.ENRICH_DATA_RPC_KEY, userRpcRequest.toJsonString());
        LOGGER.info("checkExistObjectAnalyzed - result: " + result);
        if (result != null) {
            try {
                ResponseMessage rm = new ObjectMapper().readValue(result, ResponseMessage.class);
                if (rm.getStatus() == HttpStatus.OK.value()) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                LOGGER.error("Lỗi khi mapping response message check tồn tại ánh xạ");
            }
        }
        return null;
    }
}
