/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.messaging.rabbitmq;

import com.elcom.metacen.menumanagement.controller.MenuManagementController;
import com.elcom.metacen.constant.ResourcePath;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 *
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
    
    
    @Autowired
    private MenuManagementController menuManagementController;

    
    @RabbitListener(queues = "${menumanagement.rpc.queue}")
    public String processService(String json) {
        long start = System.currentTimeMillis();
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            //Process here
            //Test call user rpc
            //RequestMessage userRequest = new RequestMessage("GET", "/user", null, "1", null, null);
            //String result = rabbitMQClient.callRpcService("user.rpc.exchange", "user_rpc_queue", "user_rpc", userRequest.toJsonString());
            ResponseMessage response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null);
            if (request != null) {
                String requestPath = request.getRequestPath().replace(request.getVersion() != null
                        ? request.getVersion() : ResourcePath.VERSION, "");
                String urlParam = request.getUrlParam();
                String pathParam = request.getPathParam();
                Map<String, Object> bodyParam = request.getBodyParam();
                Map<String, String> headerParam = request.getHeaderParam();
                //GatewayDebugUtil.debug(requestPath, urlParam, pathParam, bodyParam, headerParam);
                
                switch (request.getRequestMethod()) {
                    case "GET":
                       
                         if("/menu-management/menu".equalsIgnoreCase(requestPath)) 
                            response = menuManagementController.findAllMenu(request.getRequestPath(), request.getRequestMethod(), pathParam, headerParam); 
                         else if("/menu-management/menu/role-menu-by-role".equalsIgnoreCase(requestPath)) 
                            response = menuManagementController.findRoleMenuByRoleCode(headerParam, request.getRequestPath(), request.getRequestMethod(), request.getUrlParam());
                         else if("/menu-management/menu/role-menu-by-user".equalsIgnoreCase(requestPath)) 
                            response = menuManagementController.findRoleMenuByUser(request.getRequestPath(), request.getRequestMethod(), pathParam, headerParam);
                         else if("/menu-management/menu/role-menu".equalsIgnoreCase(requestPath)) 
                            response = menuManagementController.findRoleMenu(request.getRequestPath(), request.getRequestMethod(), pathParam, headerParam); 
                         else if("/menu-management/menu/relation-resources".equalsIgnoreCase(requestPath)) 
                            response = menuManagementController.findAllRelationResource(request.getRequestPath(), request.getRequestMethod(), pathParam, headerParam); 
                         
                        break;
                    case "POST":  
                         if("/menu-management/menu/role-menu".equalsIgnoreCase(requestPath)) 
                         response = menuManagementController.createRoleMenu(request.getRequestPath(), headerParam, bodyParam, request.getRequestMethod(), pathParam, urlParam);
                        break;
                    case "PUT":       
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":                     
                        break;
                    default:
                        break;
                }
            }
            //Response
            LOGGER.info(" [<--] Server returned " + response.toJsonString());
            long end = System.currentTimeMillis();
            LOGGER.info("[RpcServer] ================> Time to process data : " + (end - start) + " miliseconds");
            return response.toJsonString();
        } catch (Exception ex) {
            LOGGER.error("Error to process request >>> " + ex.toString());
            ex.printStackTrace();
        }
        return null;
    }
}
