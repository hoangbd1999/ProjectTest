/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.abac.messaging.rabbitmq;

import com.elcom.abac.controller.*;
import com.elcom.abac.dto.ResultCheckDto;
import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Autowired
    private BaseController baseController;

    @Autowired
    private ResourceController resourceController;

    @Autowired
    private PolicyController policyController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private RoleUserController roleUserController;

    @Autowired
    private ValueController valueController;

    @Autowired
    private RelationResourceController relationResourceController;

    @RabbitListener(queues = "${abac.rpc.queue}")
    public String processService(String json) {
        long start = System.currentTimeMillis();
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            ResponseMessage response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null);
            if (request != null) {
                String requestPath = request.getRequestPath().replace(request.getVersion() != null
                        ? request.getVersion() : ResourcePath.VERSION, "");
                String urlParam = request.getUrlParam();
                String pathParam = request.getPathParam();
                Map<String, Object> bodyParam = request.getBodyParam();
                Map<String, String> headerParam = request.getHeaderParam();

                switch (request.getRequestMethod()) {
                    case "GET":
                        if ("/abac/role".equalsIgnoreCase(requestPath)&& pathParam != null && pathParam.length() > 0)
                            response = roleController.getRoleById(headerParam,pathParam);
                        else if ("/abac/role".equalsIgnoreCase(requestPath))
                            response = roleController.getRole(headerParam);
                        else if ("/abac/role/all".equalsIgnoreCase(requestPath)) //Role User
                            response = roleController.getAllRole(headerParam);
                        else if ("/abac/role/user".equalsIgnoreCase(requestPath) && pathParam != null && pathParam.length() > 0)
                            response = roleController.getRoleOfUser(headerParam,pathParam);
                        else if ("/abac/role/user/internal".equalsIgnoreCase(requestPath) && pathParam != null && pathParam.length() > 0)
                            response = roleController.getRoleOfUserInternal(pathParam);
                        else if ("/abac/resource".equalsIgnoreCase(requestPath)) {
                            response  = resourceController.getResource(headerParam);
                        } else if ("/abac/resource/role".equalsIgnoreCase(requestPath)&& urlParam != null && urlParam.length() > 0)
                            response = resourceController.getResourceInRole(headerParam,urlParam);
                        else if ("/abac/resource/all".equalsIgnoreCase(requestPath) )
                            response = resourceController.getAllResource(headerParam);
                        else if ("/abac/policy".equalsIgnoreCase(requestPath)&& pathParam != null && pathParam.length() > 0) {
                            response =policyController.getPolicyById(headerParam,pathParam);
                        } else if ("/abac/policy/resource".equalsIgnoreCase(requestPath)&& urlParam != null && urlParam.length() > 0) {
                            response =policyController.getPolicy(headerParam,urlParam);
                        }else if ("/abac/policy/role".equalsIgnoreCase(requestPath)&& urlParam != null && urlParam.length() > 0) {
                            response =policyController.getPolicyByRole(headerParam,urlParam);
                        } else if ("/abac/role/search".equalsIgnoreCase(requestPath)&& urlParam != null && urlParam.length() > 0) {
                            response =roleController.getSearch(headerParam,urlParam);
                        }
                        else if ("/abac/policy/all".equalsIgnoreCase(requestPath))
                            response = policyController.getAllPolicy(headerParam);
                        else if ("/abac/user".equalsIgnoreCase(requestPath) && pathParam != null && pathParam.length() > 0)
                            response = roleUserController.getListByUser(headerParam,pathParam);
                        else if ("/abac/user".equalsIgnoreCase(requestPath))
                            response = roleUserController.getListUser(headerParam);
                        else if ("/abac/role/list-user".equalsIgnoreCase(requestPath))
                            response = roleController.getRoleListUserInternal(urlParam);
                        else if ("/abac/policy/template".equalsIgnoreCase(requestPath))
                            response = policyController.getPolicyTemplate(headerParam,urlParam);
                        else if ("/abac/policy/value/event".equalsIgnoreCase(requestPath))
                            response = valueController.getRoleEvent(headerParam);
                        else if ("/abac/policy/value/state".equalsIgnoreCase(requestPath))
                            response = valueController.getRoleState(headerParam);
                        else if ("/abac/policy/value/notify".equalsIgnoreCase(requestPath))
                            response = valueController.getRoleNotify(headerParam);
                        else if ("/abac/admin-internal".equalsIgnoreCase(requestPath))
                            response = roleUserController.getListAdmin();
                        else if ("/abac/user/role/internal".equalsIgnoreCase(requestPath))
                            response = roleUserController.getListRoleCode(bodyParam);
                        else if ("/abac/resource/relation".equalsIgnoreCase(requestPath))
                            response = relationResourceController.findAllRelationResource(headerParam);
                        break;
                    case "POST":
                        if ("/abac/authorization".equalsIgnoreCase(requestPath)) {
                            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParam, headerParam);
                            response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(), resultCheckDto));
                        } else if ("/abac/role".equalsIgnoreCase(requestPath))
                            response = roleController.createRole(headerParam,bodyParam, urlParam);
                        else if ("/abac/resource".equalsIgnoreCase(requestPath))
                            response = resourceController.createResource(headerParam,bodyParam);
                        else if("/abac/policy".equalsIgnoreCase(requestPath))
                            response = policyController.createPolicy(headerParam,bodyParam);
                        else if("/abac/resource".equalsIgnoreCase(requestPath))
                            response = resourceController.createResource(headerParam,bodyParam);
                        else if("/abac/user".equalsIgnoreCase(requestPath))
                            if(urlParam!=null && !urlParam.equals("")) {
                                response = roleUserController.createRoleUser(headerParam, bodyParam, urlParam);
                            }else {
                                response = roleUserController.createRoleUserGateway(headerParam, bodyParam);
                            }
                        else if ("/abac/policy/list".equalsIgnoreCase(requestPath)) {
                            response =policyController.createListPolicy(headerParam,bodyParam);
                        }
                        else if ("/abac/policy/attribute".equalsIgnoreCase(requestPath)) {
                            response =policyController.getPolicyAndResourceAndMethod(headerParam,bodyParam);
                        }
                        break;
                    case "PUT":
                        if ("/abac/policy".equalsIgnoreCase(requestPath))
                            response = policyController.updatePolicy(headerParam,bodyParam);
                        else if ("/abac/role".equalsIgnoreCase(requestPath))
                            response = roleController.updateRole(headerParam,bodyParam);
                        else if ("/abac/user".equalsIgnoreCase(requestPath))
                            response = roleUserController.updateRoleUser(headerParam,bodyParam);
                        else if ("/abac/policy/list".equalsIgnoreCase(requestPath)) {
                            response =policyController.updateListPolicy(headerParam,bodyParam);
                        }
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
                        if ("/abac/resource".equalsIgnoreCase(requestPath))
                            response = resourceController.deleteResource(headerParam,pathParam);
                        else if ("/abac/policy".equalsIgnoreCase(requestPath))
                            response = policyController.deletePolicy(headerParam,pathParam);
                        else if ("/abac/user".equalsIgnoreCase(requestPath))
                            response = roleUserController.deleteRoleUser(headerParam,pathParam);
                        else if ("/abac/role".equalsIgnoreCase(requestPath))
                            response = roleController.deleteId(headerParam,pathParam);
                        else if ("/abac/role/multi".equalsIgnoreCase(requestPath))
                            response = roleController.deleteMulti(headerParam,bodyParam);
                        else if("/abac/user/uuid".equalsIgnoreCase(requestPath))
                            response = roleUserController.deleteUser(headerParam,bodyParam,urlParam);
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
            return null;
        }
    }
}
