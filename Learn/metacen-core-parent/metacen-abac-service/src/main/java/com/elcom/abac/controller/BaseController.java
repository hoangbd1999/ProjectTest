/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.abac.controller;

import com.elcom.abac.dto.*;
import com.elcom.abac.messaging.rabbitmq.RabbitMQClient;
import com.elcom.abac.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.abac.model.Policy;
import com.elcom.abac.model.Resource;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.repository.ResourceRepository;
import com.elcom.abac.service.PolicyService;
import com.elcom.abac.service.RoleService;
import com.elcom.abac.service.impl.CheckAuthenticationService;
import com.elcom.abac.service.impl.ResourceServiceImpl;
import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.utils.UrlPatternUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Controller
public class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private CheckAuthenticationService checkAuthenticationService;

    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Autowired
    private RoleService roleGroupService;

    @Autowired
    private ResourceServiceImpl resourceService;

    /**
     * Check token qua id service => Trả về detail user
     *
     * @param headerMap header chứa jwt token
     * @return detail user
     */
    public AuthorizationResponseDTO authenToken(Map<String, String> headerMap) {
        RequestMessage userRpcRequest = new RequestMessage();
        userRpcRequest.setRequestMethod("POST");
        userRpcRequest.setRequestPath(RabbitMQProperties.USER_RPC_AUTHEN_URL);
      //  userRpcRequest.setVersion(ResourcePath.VERSION);
        userRpcRequest.setBodyParam(null);
        userRpcRequest.setUrlParam(null);
        userRpcRequest.setHeaderParam(headerMap);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY, userRpcRequest.toJsonString());
        LOGGER.info("authenToken - result: " + result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //mapper.setDateFormat(df);
            ResponseMessage response = null;
            try {
                response = mapper.readValue(result, ResponseMessage.class);
            } catch (JsonProcessingException ex) {
                LOGGER.info("Lỗi parse json khi gọi user service verify: " + ex.toString());
                return null;
            } catch (IOException e) {
                e.printStackTrace();
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

    public AuthorizationResponseDTO GetUrlParam(String urlParam){
        AuthorizationResponseDTO dto;
        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
        String dtoUuid = params.get("dto");
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        try {
            dto = mapper.readValue(dtoUuid, AuthorizationResponseDTO.class);
            return dto;
        } catch (Exception ex) {
            LOGGER.info(ex.toString());
            return null;
        }
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

    public ResultCheckDto authorizeABAC(Map<String, Object> bodyParam, Map<String, String> headerMap) throws ExecutionException, InterruptedException, TimeoutException {
        //Set body param
        ResultCheckDto resultCheckDto = new ResultCheckDto(false);
        String apiPath = (String) bodyParam.get("api");
        String method = (String) bodyParam.get("method");
        String uuid = (String) bodyParam.get("uuid");
        List<Resource> resources = resourceService.findAll();
        List<Policy> policies = new ArrayList<>();

        for (Resource resource : resources) {
            if (isMatch(resource.getUrlpatterns(), apiPath) && resource.getStatus() == 1) {
                RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(uuid);
                if (roleCodeUuidRedis != null) {
                    List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
                    if (roleCodes == null || roleCodes.isEmpty()) {
                        return resultCheckDto;
                    }
                    AdminRoleCode adminRoleCode = redisRepository.findAdmin();
                    Map<String,String> roleAdmin = adminRoleCode.getRoleCodeAdmin();
                    for (String roleCode : roleCodes)
                    {
                        if(roleAdmin.get(roleCode)!=null){
                            resultCheckDto.setStatus(true);
                            resultCheckDto.setAdmin(true);
                            return resultCheckDto;
                        }
//                        if(roleCodes.contains("SYSTEM-CONFIG") && roleCodes.contains("ADMIN")){
//                            resultCheckDto.setStatus(true);
//                            resultCheckDto.setAdmin(true);
//                            resultCheckDto.setSystemConfig(true);
//                            return resultCheckDto;
//                        }
//                        else if(roleCodes.contains("SYSTEM-CONFIG") && !roleCodes.contains("ADMIN")){
//                            resultCheckDto.setStatus(true);
//                            resultCheckDto.setAdmin(false);
//                            resultCheckDto.setSystemConfig(true);
//                            return resultCheckDto;
//                        }else if(!roleCodes.contains("SYSTEM-CONFIG") && roleCodes.contains("ADMIN")){
//                            resultCheckDto.setStatus(true);
//                            resultCheckDto.setAdmin(true);
//                            resultCheckDto.setSystemConfig(false);
//                            return resultCheckDto;
//                        }
                        PolicyAuthenticationRedis policyAuthenticationRedis = redisRepository.findPolicyRedis(roleCode);
                        if(policyAuthenticationRedis!=null) {
                            Map<String, Map<String, List<Policy>>> listAuth = policyAuthenticationRedis.getPolicies();
                            if (listAuth != null) {
                                Map<String, List<Policy>> listMethod = listAuth.get(resource.getCode());
                                if (listMethod != null) {
                                    List<Policy> policyList = listMethod.get(method);
                                    if (policyList != null) {
                                        policies.addAll(policyList);
                                    }
                                }
                            }
                        }
                    }
                    if (policies.isEmpty()) {
                        break;
                    }

                    if (method.equals("POST")) {
                        if (resource.getCreatePolicyType().equals("*")) {
                            resultCheckDto.setStatus(true);
                            break;
                        }
                    } else if (method.equals("PUT")) {
                        if (resource.getUpdatePolicyType().equals("*")) {
                            resultCheckDto.setStatus(true);
                            break;
                        }
                    } else if (method.equals("DETAIL")) {
                        if (resource.getDetailPolicyType().equals("*")) {
                            resultCheckDto.setStatus(true);
                            break;
                        }
                    } else if (method.equals("LIST")) {
                        if (resource.getListPolicyType().equals("*")) {
                            resultCheckDto.setStatus(true);
                            break;
                        }
                    } else if (method.equals("DELETE")) {
                        if (resource.getDeletePolicyType().equals("*")) {
                            resultCheckDto.setStatus(true);
                            break;
                        }
                    }
                    List<CompletableFuture<ResultCheckDto>> listResult = new ArrayList<>();
                    for (Policy policy : policies) {
                        if (policy.getPolicyType().equals("*")) {
                            resultCheckDto.setStatus(true);
                        } else {
                            CompletableFuture<ResultCheckDto> result = checkAuthenticationService.Authentication(resource, policy, bodyParam, headerMap);
                            listResult.add(result);
                        }
                    }
                    CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
                            .thenApply(v -> listResult.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList())
                            );
                    LOGGER.info("Nhận kết quả");
                    Condition condition;
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        switch (method) {
                            case "POST":
                                condition = mapper.readValue(resource.getCreatePolicyType(), Condition.class);
                                break;
                            case "PUT":
                                condition = mapper.readValue(resource.getUpdatePolicyType(), Condition.class);
                                break;
                            case "DETAIL":
                                condition = mapper.readValue(resource.getDetailPolicyType(), Condition.class);
                                break;
                            case "LIST":
                                condition = mapper.readValue(resource.getListPolicyType(), Condition.class);
                                break;
                            case "DELETE":
                                condition = mapper.readValue(resource.getDeletePolicyType(), Condition.class);
                                break;
                            default:
                                condition = new Condition();
                                break;
                        }
                        for (CompletableFuture<ResultCheckDto> result : listResult) {
                            ResultCheckDto resultCheckDto1 = result.get();
                            if (condition.getCondition().equals("Equals")) {
                                if (resultCheckDto1.getStatus() && resultCheckDto1.getType().equals("DENY")) {
                                    resultCheckDto.setStatus(false);
                                    resultCheckDto.setDescription("Policy Deny");
                                    break;
                                }
                                if (resultCheckDto1.getStatus()) {
                                    resultCheckDto.setStatus(true);
                                    resultCheckDto.setType(resultCheckDto1.getType());
                                }
                                if(!(resultCheckDto1.getStatus())){
                                    resultCheckDto.setDescription(resultCheckDto1.getDescription());
                                }
                            } else if (condition.getCondition().equals("AllOf")) {
                                if (resultCheckDto1.getStatus() && resultCheckDto1.getType().equals("DENY")) {
                                    resultCheckDto.setStatus(false);
                                    break;
                                }
                                if (resultCheckDto1.getStatus()) {
                                    resultCheckDto.setStatus(true);
                                } else {
                                    resultCheckDto.setStatus(false);
                                    resultCheckDto.setDescription(resultCheckDto1.getDescription());
                                    break;
                                }
                            } else if (condition.getCondition().equals("AnyOf")) {
                                if (resultCheckDto1.getStatus() && resultCheckDto1.getType().equals("DENY")) {
                                    resultCheckDto.setStatus(false);
                                    break;
                                }
                                if (resultCheckDto1.getStatus()) {
                                    resultCheckDto.setStatus(true);
                                    break;
                                }
                                if(!(resultCheckDto1.getStatus())){
                                    resultCheckDto.setDescription(resultCheckDto1.getDescription());
                                }
                            }

                        }
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                        break;
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        break;
                    }
                    break;
                }
            }
        }

        return resultCheckDto;
    }

    @Async("threadPoolCheckPolicy")
    public CompletableFuture<ResultCheckDto> authorizeABACInternal(String uuid, String methodId) throws ExecutionException, InterruptedException, TimeoutException {
        //Set body param
        Map<String, Object> bodyParam = new HashMap<>();
        bodyParam.put("uuid", uuid);
        PolicyAuthenticationRedis policyAuthenticationRedis = redisRepository.findPolicyRedis(uuid);
        List<Policy> policies;
        ResultCheckDto resultCheckDto = new ResultCheckDto(false);
        if (policyAuthenticationRedis == null) {
            resultCheckDto.setDescription("Không tìm thấy chính sách nào!");
            resultCheckDto.setStatus(false);
            return CompletableFuture.completedFuture(resultCheckDto);
        }
//        Map<String, List<Policy>> policies1 = policyAuthenticationRedis.getPolicies();
//        policies = policies1.get(methodId);
//        List<CompletableFuture<ResultCheckDto>> listResult = new ArrayList<>();
//        if(policies!=null) {
//            for (Policy policy : policies
//            ) {
////                    CompletableFuture<ResultCheckDto> result = checkAuthenticationService.Authentication();
////                    listResult.add(result);
//                }
//            CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
//                    .thenApply(v -> listResult.stream()
//                            .map(CompletableFuture::join)
//                            .collect(Collectors.toList())
//                    );
//            for (CompletableFuture<ResultCheckDto> result : listResult
//            ) {
//                ResultCheckDto resultCheckDto1 = result.get();
//                if (resultCheckDto1.getStatus() && resultCheckDto1.getType().equals("DENY")) {
//                    resultCheckDto.setType(resultCheckDto1.getType());
//                    resultCheckDto.setDescription(resultCheckDto1.getDescription());
//                    break;
//                }
//                if (resultCheckDto1.getStatus()) {
//                    resultCheckDto.setStatus(true);
//                    resultCheckDto.setType(resultCheckDto1.getType());
//                    resultCheckDto.setDescription(resultCheckDto1.getDescription());
//                }
//            }
//        }else {
//            resultCheckDto.setDescription("Không tìm thấy chính sách nào!");
//            resultCheckDto.setStatus(false);
//            return CompletableFuture.completedFuture(resultCheckDto);
//        }
        return CompletableFuture.completedFuture(resultCheckDto);

    }

}
