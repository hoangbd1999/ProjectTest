/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.controller;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.menumanagement.dto.ABACResponseDTO;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.menumanagement.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.menumanagement.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.menumanagement.dto.AuthorizationResponseDTO;
import com.elcom.metacen.menumanagement.model.Resources;

import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.utils.UrlPatternUtil;
import com.elcom.metacen.menumanagement.dto.GetRoleResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Admin
 */
public class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private RabbitMQClient rabbitMQClient;

    /**
     * Check token qua id service => Trả về detail user
     *
     * @param headerMap header chứa jwt token
     * @return detail user
     */
    public AuthorizationResponseDTO authenToken(Map<String, String> headerMap) {
        //Authen -> call rpc authen headerMap
        RequestMessage userRpcRequest = new RequestMessage();
        userRpcRequest.setRequestMethod("POST");
        userRpcRequest.setRequestPath(RabbitMQProperties.USER_RPC_AUTHEN_URL);
        userRpcRequest.setVersion(ResourcePath.VERSION);
        userRpcRequest.setBodyParam(null);
        userRpcRequest.setUrlParam(null);
        userRpcRequest.setHeaderParam(headerMap);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY, userRpcRequest.toJsonString());
        LOGGER.info("authenToken - result: " + result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            ResponseMessage response = null;
            try {
                response = mapper.readValue(result, ResponseMessage.class);
            } catch (JsonProcessingException ex) {
                LOGGER.info("Lỗi parse json khi gọi user service verify: " + ex.toString());
                return null;
            }

            if (response != null && response.getStatus() == HttpStatus.OK.value()) {
                try {
                    //Process
                    MessageContent content = response.getData();
                    Object data = content.getData();
                    if (data != null) {
                        AuthorizationResponseDTO dto = null;
                        if (data.getClass() == LinkedHashMap.class) {
                            dto = new AuthorizationResponseDTO((Map<String, Object>) data);
                        } else if (data.getClass() == AuthorizationResponseDTO.class) {
                            dto = (AuthorizationResponseDTO) data;
                        }
                        if (dto != null && !StringUtil.isNullOrEmpty(dto.getUuid())) {
                            return dto;
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.info("Lỗi giải mã AuthorizationResponseDTO khi gọi user service verify: " + ex.toString());
                    return null;
                }
            } else {
                //Forbidden
                return null;
            }
        } else {
            //Forbidden
            return null;
        }
        return null;
    }

    /**
     * Get list user from ID service with user uuid list
     *
     * @param uuidList
     * @param headerMap map contains jwt token to authen
     * @return
     */
    public Map<String, AuthorizationResponseDTO> getUserMap(List<String> uuidList, Map<String, String> headerMap) {
        if (uuidList != null && !uuidList.isEmpty()) {
            Map<String, Object> requestIdBodyParam = new HashMap<>();
            requestIdBodyParam.put("uuids", uuidList);
            RequestMessage request = new RequestMessage("POST", RabbitMQProperties.USER_RPC_UUIDLIST_URL,
                    ResourcePath.VERSION, null, null, requestIdBodyParam, headerMap);
            String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                    RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY,
                    request.toJsonString());
            LOGGER.info("getUserMap - call ID service result: " + result);
            if (!StringUtil.isNullOrEmpty(result)) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mapper.setDateFormat(df);
                    ResponseMessage resultResponse = mapper.readValue(result, ResponseMessage.class);
                    if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value()
                            && resultResponse.getData() != null) {
                        JsonNode jsonNode = mapper.readTree(result);
                        List<AuthorizationResponseDTO> dtoList = Arrays.asList(mapper.treeToValue(jsonNode.get("data").get("data"),
                                AuthorizationResponseDTO[].class));
                        if (dtoList != null && !dtoList.isEmpty()) {
                            Map<String, AuthorizationResponseDTO> dtoMap = new HashMap<>();
                            for (AuthorizationResponseDTO tmpDto : dtoList) {
                                dtoMap.put(tmpDto.getUuid(), tmpDto);
                            }
                            return dtoMap;
                        }
                    }
                } catch (JsonProcessingException ex) {
                    LOGGER.error("Error to parse json >>> " + ex.toString());
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    private boolean isMatch(String urlPatterns, String apiPath) {
        if (StringUtil.isNullOrEmpty(urlPatterns) || StringUtil.isNullOrEmpty(apiPath)) {
            return false;
        }
        String[] urlPatternArr = urlPatterns.split(",");
        for (String urlPattern : urlPatternArr) {
            if (UrlPatternUtil.matchPattern(urlPattern, apiPath)) {
                return true;
            }
        }
        return false;
    }

    public ABACResponseDTO authorizeABAC(Map<String, Object> bodyParam, String requestMethod, String userUuid, String apiPath) throws ExecutionException, InterruptedException {
        Map<String, Object> bodyParamSend = new HashMap<>();
        if (!bodyParam.isEmpty()) {
            bodyParamSend.putAll(bodyParam);
        }
        bodyParamSend.put("uuid", userUuid);
        bodyParamSend.put("api", apiPath);
        bodyParamSend.put("method", requestMethod);
        RequestMessage abacRpcRequest = new RequestMessage();
        abacRpcRequest.setRequestMethod("POST");
        abacRpcRequest.setRequestPath(RabbitMQProperties.ABAC_RPC_AUTHOR_URL);
        abacRpcRequest.setBodyParam(bodyParamSend);
        abacRpcRequest.setUrlParam(null);
        abacRpcRequest.setHeaderParam(null);
        LOGGER.info("REQUEST" + abacRpcRequest.toJsonString());
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ABAC_RPC_EXCHANGE,
                RabbitMQProperties.ABAC_RPC_QUEUE, RabbitMQProperties.ABAC_RPC_KEY,
                abacRpcRequest.toJsonString());
        LOGGER.info("RESULT" + result.toString());
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    JsonNode jsonNode = mapper.readTree(result);
                    ABACResponseDTO resultCheckDto = mapper.treeToValue(jsonNode.get("data").get("data"), ABACResponseDTO.class);
                    return resultCheckDto;
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<String> getListRoleString(String userId) throws ExecutionException, InterruptedException {
        //Get list site id -> call systemconfig service

        LOGGER.info("USERID " + userId);
        RequestMessage getRoleRpcRequest = new RequestMessage();
        getRoleRpcRequest.setRequestMethod("GET");
        getRoleRpcRequest.setRequestPath(RabbitMQProperties.ABAC_RPC_GET_ROLE_BY_USER_URL);
        getRoleRpcRequest.setVersion(ResourcePath.VERSION);
        getRoleRpcRequest.setPathParam(userId);
        LOGGER.info("REQUEST" + getRoleRpcRequest.toJsonString());
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ABAC_RPC_EXCHANGE,
                RabbitMQProperties.ABAC_RPC_QUEUE, RabbitMQProperties.ABAC_RPC_KEY, getRoleRpcRequest.toJsonString());
        LOGGER.info(" --- result: " + result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    JsonNode jsonNode = mapper.readTree(result);
                    List<String> listRoleResponse = mapper.readerFor(new TypeReference<List<String>>() {
                    }).readValue(jsonNode.get("data").get("data"));

                    return listRoleResponse;
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<GetRoleResponse> getListRole(Map<String, String> headerMap) throws ExecutionException, InterruptedException {
        RequestMessage getRoleRpcRequest = new RequestMessage();
        getRoleRpcRequest.setRequestMethod("GET");
        getRoleRpcRequest.setRequestPath(RabbitMQProperties.ABAC_RPC_GET_ROLE_URL);
        getRoleRpcRequest.setVersion(ResourcePath.VERSION);
        getRoleRpcRequest.setBodyParam(null);
        getRoleRpcRequest.setUrlParam(null);
        getRoleRpcRequest.setHeaderParam(headerMap);
        LOGGER.info("REQUEST" + getRoleRpcRequest.toJsonString());
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ABAC_RPC_EXCHANGE,
                RabbitMQProperties.ABAC_RPC_QUEUE, RabbitMQProperties.ABAC_RPC_KEY, getRoleRpcRequest.toJsonString());
        LOGGER.info(" --- result: " + result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    JsonNode jsonNode = mapper.readTree(result);
                    List<GetRoleResponse> listRoleResponse = mapper.readerFor(new TypeReference<List<GetRoleResponse>>() {
                    }).readValue(jsonNode.get("data").get("data"));

                    return listRoleResponse;
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }
    }
}
