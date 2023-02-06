/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.controller;

import com.elcom.metacen.config.GatewayConfig;
import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.exception.ValidationException;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.validation.GatewayValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author Admin
 */
public class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private RabbitMQClient rabbitMQClient;

    public ResponseEntity<String> processRequest(String requestMethod, Map<String, String> urlParamMap,
            Map<String, Object> bodyParamMap, Map<String, String> headerParamMap,
            HttpServletRequest req) throws JsonProcessingException {
        long startTime = System.currentTimeMillis();
        //Init platform, ip address in header param
        headerParamMap = initHeaderMap(headerParamMap, req);

        //Get all value
        String requestPath = req.getRequestURI();
        String urlParam = StringUtil.generateMapString(urlParamMap);
        String pathParam = null;

        //Service
        int index = requestPath.indexOf("/", GatewayConfig.API_ROOT_PATH.length());
        String service = null;
        if (index != -1) {
            service = requestPath.substring(GatewayConfig.API_ROOT_PATH.length(), index);
        } else {
            service = requestPath.replace(GatewayConfig.API_ROOT_PATH, "");
        }

        //Check has path param
        int lastIndex = requestPath.lastIndexOf("/");
        if (lastIndex != -1) {
            String lastStr = requestPath.substring(lastIndex + 1);
            if (StringUtil.isNumberic(lastStr) || StringUtil.isUUID(lastStr)) {
                requestPath = requestPath.substring(0, lastIndex);
                pathParam = lastStr;
            }
        }

        //Log request info
        LOGGER.info("[{}] to requestPath: {} - urlParam: {} - pathParm: {} - bodyParam: {} - headerParam: {}",
                requestMethod, requestPath, urlParam, pathParam, bodyParamMap, StringUtil.generateMapString(headerParamMap));
        //Validate
        String invalidData = new GatewayValidation().validate(requestPath, service);

        if (invalidData != null) {
            ResponseMessage responseMessage = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, null);
            String result = responseMessage.toJsonString();
            return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
        } else {

            RequestMessage request = new RequestMessage(requestMethod, requestPath, ResourcePath.VERSION,
                    urlParam, pathParam, bodyParamMap, headerParamMap);
            String result = null;

            //Get rabbit type
            String rabbitType = GatewayConfig.RABBIT_TYPE_MAP.get(requestMethod + " "
                    + requestPath.replace(GatewayConfig.API_ROOT_PATH, "/"));
            LOGGER.info("Get Rabbit type for {} {} ==> Rabbit: {}", requestMethod,
                    requestPath.replace(GatewayConfig.API_ROOT_PATH, "/"), rabbitType);
            if ("rpc".equalsIgnoreCase(rabbitType)) {
                String rpcQueue = GatewayConfig.SERVICE_MAP.get(service + ".rpc.queue");
                String rpcExchange = GatewayConfig.SERVICE_MAP.get(service + ".rpc.exchange");
                String rpcKey = GatewayConfig.SERVICE_MAP.get(service + ".rpc.key");
                if (StringUtil.isNullOrEmpty(rpcQueue) || StringUtil.isNullOrEmpty(rpcExchange) || StringUtil.isNullOrEmpty(rpcKey)) {
                    throw new ValidationException("Không tìm thấy rabbit mq cho service " + service);
                }
                result = rabbitMQClient.callRpcService(rpcExchange, rpcQueue, rpcKey, request.toJsonString());
                LOGGER.info("Elapsed [{}] for requestUri: [{}], requestSession: [{}]",
                        getElapsedTime(System.currentTimeMillis() - startTime),
                        req.getRequestURI(), req.getSession().getId());
                // LOGGER.info("result: " + result);
            } else if ("worker".equalsIgnoreCase(rabbitType)) {
                String workerQueue = GatewayConfig.SERVICE_MAP.get(service + ".worker.queue");
                if (StringUtil.isNullOrEmpty(workerQueue)) {
                    throw new ValidationException("Không tìm thấy rabbit mq cho service " + service);
                }
                //Call worker
                if (rabbitMQClient.callWorkerService(workerQueue, request.toJsonString())) {
                    MessageContent mc = new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), "OK");
                    ResponseMessage responseMessage = new ResponseMessage(mc);
                    result = responseMessage.toJsonString();
                } else {
                    MessageContent mc = new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.toString(), null);
                    ResponseMessage responseMessage = new ResponseMessage(mc);
                    result = responseMessage.toJsonString();
                }
            } else if ("publish".equalsIgnoreCase(rabbitType)) {
                String directExchange = GatewayConfig.SERVICE_MAP.get(service + ".direct.exchange");
                String directKey = GatewayConfig.SERVICE_MAP.get(service + ".direct.key");
                if (StringUtil.isNullOrEmpty(directExchange) || StringUtil.isNullOrEmpty(directKey)) {
                    throw new ValidationException("Không tìm thấy rabbit mq cho service " + service);
                }
                //Call publisher
                if (rabbitMQClient.callPublishService(directExchange, directKey, request.toJsonString())) {
                    MessageContent mc = new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), "OK");
                    ResponseMessage responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), mc);
                    result = responseMessage.toJsonString();
                } else {
                    MessageContent mc = new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null);
                    ResponseMessage responseMessage = new ResponseMessage(mc);
                    result = responseMessage.toJsonString();
                }
            } else {
                MessageContent mc = new MessageContent(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(), null);
                ResponseMessage responseMessage = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(), mc);
                result = responseMessage.toJsonString();
                throw new ValidationException("Không tìm thấy xử lý cho kiểu rabbit " + rabbitType);
            }
            if (result != null) {
                ObjectMapper mapper = new ObjectMapper();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                mapper.setDateFormat(df);
                ResponseMessage response = mapper.readValue(result, ResponseMessage.class);
                return new ResponseEntity(response.getData(), HttpStatus.valueOf(response.getStatus()));
            }
            ResponseMessage responseMessage = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null);
            result = responseMessage.toJsonString();
            return new ResponseEntity(result, HttpStatus.OK);
        }
    }

    private String getElapsedTime(long miliseconds) {
        //return (miliseconds / 1000.0) + "(s)";
        return miliseconds + " (ms)";
    }

    private Map<String, String> initHeaderMap(Map<String, String> headerParamMap, HttpServletRequest request) {
        Map<String, String> resultMap = headerParamMap;
        if (resultMap == null) {
            resultMap = new HashMap<>();
        }
        //Platform
        resultMap.put("platform", "WEB");
        //Ip address
        String ipAddress = null;
        if (request.getHeader("X-Forwarded-For") != null) {
            ipAddress = request.getHeader("X-Forwarded-For");
        } else {
            ipAddress = request.getRemoteAddr();
        }
        resultMap.put("ip-address", ipAddress);
        return resultMap;
    }
}
