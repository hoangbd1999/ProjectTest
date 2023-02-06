package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.contact.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.contact.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.contact.service.MappingDataService;
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
public class MappingDataServiceImpl implements MappingDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MappingDataServiceImpl.class);

    @Autowired private RabbitMQClient rabbitMQClient;

    @Override
    public Boolean isExistMappingRelation(Map<String, String> headerMap, Map<String, Object> bodyMap) {
        bodyMap.put("objectUuid", bodyMap.getOrDefault("uuid", null));

        RequestMessage userRpcRequest = new RequestMessage();
        userRpcRequest.setRequestMethod("POST");
        userRpcRequest.setRequestPath(RabbitMQProperties.MAPPING_DATA_VSAT_METACEN_CHECK_MAPPING_URL);
        userRpcRequest.setVersion(ResourcePath.VERSION);
        userRpcRequest.setBodyParam(bodyMap);
        userRpcRequest.setUrlParam(null);
        userRpcRequest.setHeaderParam(headerMap);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.MAPPING_DATA_RPC_EXCHANGE,
                RabbitMQProperties.MAPPING_DATA_RPC_QUEUE, RabbitMQProperties.MAPPING_DATA_RPC_KEY, userRpcRequest.toJsonString());
        LOGGER.info("checkExistMappingRelation - result: " + result);
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
