package com.elcom.abac.controller;

import com.elcom.abac.constant.Constant;
import com.elcom.abac.dto.*;
import com.elcom.abac.messaging.rabbitmq.RpcServer;
import com.elcom.abac.model.Policy;
import com.elcom.abac.model.Resource;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.service.*;
import com.elcom.abac.service.impl.ResourceServiceImpl;
import com.elcom.metacen.message.MessageContent;
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
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
public class PolicyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyController.class);

    @Autowired
    private BaseController baseController;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private ResourceServiceImpl resourceService;

    public ResponseMessage createPolicy(Map<String, String> headerParam, Map<String, Object> bodyParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy");
            bodyParamCheck.put("method", "POST");
            bodyParamCheck.put("uuid", dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", "fake");
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                String subjectValue = (String) bodyParam.get("subjectValue");
                if (subjectValue == null || subjectValue.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "SubjectValue khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "SubjectValue khong the trong", null));
                    return response;
                }
                String method = (String) bodyParam.get("method");
                if ((method == null || method.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Method khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Method khong the trong", null));
                    return response;
                }
                String effect = (String) bodyParam.get("effect");
                if ((effect == null || effect.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Effect khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Effect khong the trong", null));
                    return response;
                }
                String policyType = (String) bodyParam.get("policyType");
                if ((policyType == null || policyType.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "PolicyType khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "PolicyType khong the trong", null));
                    return response;
                }
                String resourceCode = (String) bodyParam.get("resourceCode");
                if ((resourceCode == null || resourceCode.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "ResourceCode khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "ResourceCode khong the trong", null));
                    return response;
                }
                String subjectType = (String) bodyParam.get("subjectType");
                String subjectCondition = (String) bodyParam.get("subjectCondition");
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String rules = objectMapper.writeValueAsString(bodyParam.get("rules"));
                    Policy policy = new Policy();
                    policy.setSubjectValue(subjectValue);
                    policy.setSubjectCondition(subjectCondition);
                    policy.setPolicyType(policyType);
                    policy.setEffect(effect);
                    policy.setSubjectType(subjectType);
                    if (!policyType.equals("*")) {
                        policy.setRules(rules);
                    }
                    policy.setMethod(method);
                    policy.setResourceCode(resourceCode);
                    Optional<Policy> policyOptional = policyService.findUnique(policy.getResourceCode(), policy.getSubjectValue(), policy.getSubjectCondition(), policy.getSubjectType(),
                            policy.getEffect(), policy.getMethod(), policy.getPolicyType());
                    if (policyOptional.isPresent()) {
                        return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
                                        null));
                    }
                    policy = policyService.savePolicy(policy);
                    if (policy != null) {
                        response = new ResponseMessage(new MessageContent(policy));
                    } else {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                        null));
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                    null));
                }


            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu chinh sach",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ lieu chinh sach",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage createListPolicy(Map<String, String> headerParam, Map<String, Object> mapBody) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response = null;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy/list");
            bodyParamCheck.put("method", "POST");
            bodyParamCheck.put("uuid", dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", "fake");
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                List<Map<String,Object>> bodyParams = (List<Map<String, Object>>) mapBody.get("policyList");
                List<Policy> policyList = new ArrayList<>();
                for (Map<String,Object> bodyParam: bodyParams
                ) {

                    String subjectValue = (String) bodyParam.get("subjectValue");
                    if (subjectValue == null || subjectValue.isEmpty()) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "SubjectValue khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "SubjectValue khong the trong", null));
                        return response;
                    }
                    String method = (String) bodyParam.get("method");
                    if ((method == null || method.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Method khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Method khong the trong", null));
                        return response;
                    }
                    String effect = (String) bodyParam.get("effect");
                    if ((effect == null || effect.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Effect khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Effect khong the trong", null));
                        return response;
                    }
                    String policyType = (String) bodyParam.get("policyType");
                    if ((policyType == null || policyType.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "PolicyType khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "PolicyType khong the trong", null));
                        return response;
                    }
                    String resourceCode = (String) bodyParam.get("resourceCode");
                    if ((resourceCode == null || resourceCode.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "ResourceCode khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "ResourceCode khong the trong", null));
                        return response;
                    }
                    String subjectType = (String) bodyParam.get("subjectType");
                    String subjectCondition = (String) bodyParam.get("subjectCondition");
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        String rules = objectMapper.writeValueAsString(bodyParam.get("rules"));
                        Policy policy = new Policy();
                        policy.setSubjectValue(subjectValue);
                        policy.setSubjectCondition(subjectCondition);
                        policy.setPolicyType(policyType);
                        policy.setEffect(effect);
                        policy.setSubjectType(subjectType);
                        if (!policyType.equals("*")) {
                            policy.setRules(rules);
                        }
                        policy.setMethod(method);
                        policy.setResourceCode(resourceCode);
                        Optional<Policy> policyOptional = policyService.findUnique(policy.getResourceCode(), policy.getSubjectValue(), policy.getSubjectCondition(), policy.getSubjectType(),
                                policy.getEffect(), policy.getMethod(), policy.getPolicyType());
                        if (policyOptional.isPresent()) {
                            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
                                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
                                            null));
                        }
                        policyList.add(policy);
//                        policy = policyService.savePolicy(policy);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                        null));
                    }
                }
                policyList = policyService.saveList(policyList);
                if (policyList != null) {
                    response = new ResponseMessage(new MessageContent(policyList));
                } else {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                    null));
                }

            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu chinh sach",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ lieu chinh sach",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage updateListPolicy(Map<String, String> headerParam, Map<String, Object> mapBody) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response = null;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy/list");
            bodyParamCheck.put("method", "POST");
            bodyParamCheck.put("uuid", dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", "fake");
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                List<Map<String,Object>> bodyParams = (List<Map<String, Object>>) mapBody.get("policyList");
                List<Policy> policyList = new ArrayList<>();
                List<Policy> policyCreate = new ArrayList<>();
                int index = 0;
                for (Map<String,Object> bodyParam: bodyParams
                ) {
                    Integer id = (Integer) bodyParam.get("id");
                    String subjectValue = (String) bodyParam.get("subjectValue");
                    if (subjectValue == null || subjectValue.isEmpty()) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "subjectValue khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "subjectValue khong the trong", null));
                        return response;
                    }
                    if(id!=null){
                        if(policyService.findById(id).isPresent()){
                            policyService.deleteId(id);
                        }
                    }

                    if(index==0){
                        policyService.deleteBySubjectValue(subjectValue);
                        redisRepository.removePolicyRedis(subjectValue);
                        index ++;
                    }
                    String method = (String) bodyParam.get("method");
                    if ((method == null || method.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Method khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Method khong the trong", null));
                        return response;
                    }
                    String effect = (String) bodyParam.get("effect");
                    if ((effect == null || effect.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Effect khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Effect khong the trong", null));
                        return response;
                    }
                    String policyType = (String) bodyParam.get("policyType");
                    if ((policyType == null || policyType.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "PolicyType khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "PolicyType khong the trong", null));
                        return response;
                    }
                    String resourceCode = (String) bodyParam.get("resourceCode");
                    if ((resourceCode == null || resourceCode.isEmpty())) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "ResourceCode khong the trong",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "ResourceCode khong the trong", null));
                        return response;
                    }
                    String subjectType = (String) bodyParam.get("subjectType");
                    String subjectCondition = (String) bodyParam.get("subjectCondition");
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        String rules = objectMapper.writeValueAsString(bodyParam.get("rules"));
                        Policy policy = new Policy();
                        policy.setSubjectValue(subjectValue);
                        policy.setSubjectCondition(subjectCondition);
                        policy.setPolicyType(policyType);
                        policy.setEffect(effect);
                        policy.setSubjectType(subjectType);
                        if (!policyType.equals("*")) {
                            policy.setRules(rules);
                        }
                        policy.setMethod(method);
                        policy.setResourceCode(resourceCode);
                        Optional<Policy> policyOptional = policyService.findUnique(policy.getResourceCode(), policy.getSubjectValue(), policy.getSubjectCondition(), policy.getSubjectType(),
                                policy.getEffect(), policy.getMethod(), policy.getPolicyType());
                        if (policyOptional.isPresent()) {
                            LOGGER.info("Lỗi unique {},{},{}",policy.getResourceCode(), policy.getSubjectValue(),policy.getMethod());

//                            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
//                                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
//                                            null));
                        }
                        policyList.add(policy);
//                        policy = policyService.savePolicy(policy);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                        null));
                    }
                }
                Thread.sleep(1500);
                policyList = policyService.saveList(policyList);
                if (policyList != null) {
                    response = new ResponseMessage(new MessageContent(policyList));
                } else {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                    null));
                }

            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu chinh sach",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ lieu chinh sach",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getPolicyTemplate(Map<String, String> headerParam, String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response = null;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
            return response;
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy/template");
            bodyParamCheck.put("method", "LIST");
            bodyParamCheck.put("uuid", dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                Map<String, String> paramMap = StringUtil.getUrlParamValues(urlParam);
                if (paramMap == null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "param null ",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "param null", null));
                }
                String resourceCode = paramMap.get("resourceCode");
                String method = paramMap.get("method");
                if (resourceCode == null || resourceCode.isEmpty() || method == null || method.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "resourceCode và method không thể trống",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "resourceCode và method không thể trống", null));
                } else {
                    List<PolicyTemplate> policies = policyService.findTemplatePolicyResource(resourceCode,method);
                    response = new ResponseMessage(new MessageContent(policies));
                }
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Không có quyền xem Template của resouce",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Không có quyền xem Template của resouce", null));
            }

        }
        return response;
    }

    public ResponseMessage getPolicy(Map<String, String> headerParam, String urlParam) {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
            return response;
        } else {
            Map<String, String> paramMap = StringUtil.getUrlParamValues(urlParam);
            if (paramMap == null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "param null ",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "param null", null));
            }
            String param = paramMap.get("resource");
            if (true) {
                RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(dto.getUuid());
                List<String> listRoleCode = roleCodeUuidRedis.getRoleCode();
                List<Policy> policies = policyService.findByResourceCodeAndSubjectValueIn(param, listRoleCode);
                response = new ResponseMessage(new MessageContent(policies));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Khong co quyen ",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Khong co quyen", "Bạn không có quyền xem policy của resource này"));
            }
        }
        return response;
    }

    public ResponseMessage getPolicyById(Map<String, String> headerParam, String param) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy");
            bodyParamCheck.put("method", "DETAIL");
            bodyParamCheck.put("uuid", dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                if (param == null || param.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "param không thể trống", null));
                } else {
                    Integer id = Integer.parseInt(param);
                    if (id == null || id.equals(0L)) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "id không hợp lệ", null));
                    } else {
                        Optional<Policy> policy = policyService.findById(id);
                        if (policy.isPresent()) {
                            response = new ResponseMessage(new MessageContent(policy.get()));
                        } else {
                            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "Khong tim thay ban ghi", null));
                        }
                    }
                }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getPolicyAndResourceAndMethod(Map<String, String> headerParam, Map<String, Object> bodyParam) {
        ResponseMessage response;
        String apiPath = (String) bodyParam.get("api");
        String method = (String) bodyParam.get("method");
        String uuid = (String) bodyParam.get("uuid");
        List<Resource> resources = resourceService.findAll();
        Map<String, Map<String, Boolean>> attribute = new HashMap<>();
        Map<String, Object> allow = new HashMap<>();
        Map<String, Object> disallow = new HashMap<>();
        PolicyDto policyDto = new PolicyDto();
        List<String> roleCodeResults = new ArrayList<>();
        for (Resource resource : resources) {
            if (isMatch(resource.getUrlpatterns(), apiPath) && resource.getStatus() == 1) {
                RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(uuid);
                if (roleCodeUuidRedis != null) {
                    List<String> roleCodes = roleCodeUuidRedis.getRoleCode();
                    if (roleCodes == null || roleCodes.isEmpty()) {
                        return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Khong co quyen ",
                                new MessageContent(HttpStatus.FORBIDDEN.value(), "Khong tồn tại quyền trong hệ thống", null));
                    }
                    AdminRoleCode adminRoleCode = redisRepository.findAdmin();
                    Map<String,String> roleAdmin = adminRoleCode.getRoleCodeAdmin();
                    for (String roleCode : roleCodes
                    ) {
                        if(roleAdmin.get(roleCode)!=null){
                            policyDto.setAdmin(true);
                            return new ResponseMessage(new MessageContent(policyDto));
                        }

                        PolicyAuthenticationRedis policyAuthenticationRedis = redisRepository.findPolicyRedis(roleCode);
                        Map<String, Map<String, List<Policy>>> listAuth = policyAuthenticationRedis.getPolicies();
                        if (listAuth != null) {
                            Map<String, List<Policy>> listMethod = listAuth.get(resource.getCode());
                            if (listMethod != null) {
                                List<Policy> policyList = listMethod.get(method);
                                if (policyList != null) {
                                    for (Policy policy : policyList
                                    ) {
                                        ConditionPolicy conditionPolicy = policy.getConditionPolicy();
                                        List<ConditionDetail> conditionDetails = conditionPolicy.getValue();
                                        if(policy.getPolicyType().equals("*")){
                                            return new ResponseMessage(HttpStatus.OK.value(), "Policy Loại *",
                                                    new MessageContent(HttpStatus.OK.value(), "Policy loại *", null));
                                        }
                                        if(conditionDetails==null){
                                            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Khong co quyen ",
                                                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Khong tồn tại quyền trong hệ thống", null));
                                        }
                                        for (ConditionDetail conditionDetail : conditionDetails
                                        ) {
                                            if (policy.getPolicyType().equals("param")) {
                                                if (conditionDetail.getCondition().equals("IsNotIn") || conditionDetail.getCondition().equals("Neq"))
                                                    disallow.put(conditionDetail.getParam(), conditionDetail.getValue());
                                                else
                                                    allow.put(conditionDetail.getParam(), conditionDetail.getValue());
                                            } else {
                                                String key = "$_" + policy.getPolicyType() + "_$";
                                                allow.put(key, conditionDetail.getParam());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!allow.isEmpty() || !disallow.isEmpty()) {
            policyDto.setAllow(allow);
            policyDto.setDisallow(disallow);
            response = new ResponseMessage(new MessageContent(policyDto));
        } else {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy param nào cần cho api", null));
        }
        return response;
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


    public ResponseMessage getPolicyByRole(Map<String, String> headerParam, String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
            return response;
        } else {
            Map<String, String> paramMap = StringUtil.getUrlParamValues(urlParam);
            String param = paramMap.get("roleCode");
            if (param == null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.FORBIDDEN.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy");
            bodyParamCheck.put("method", "DETAIL");
            bodyParamCheck.put("uuid", dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                List<Policy> policies = policyService.findBySubjectValue(param);
                response = new ResponseMessage(new MessageContent(policies));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Khong co quyen ",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Khong co quyen", "Bạn không có quyền xem policy trong role này"));
            }

        }
        return response;
    }

    public ResponseMessage getAllPolicy(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy");
            bodyParamCheck.put("method", "LIST");
            bodyParamCheck.put("uuid", dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                List<Policy> policies = policyService.findAll();
                response = new ResponseMessage(new MessageContent(policies));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền ",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền ",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage updatePolicy(Map<String, String> headerParam,
                                        Map<String, Object> bodyParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/policy");
            bodyParamCheck.put("method", "PUT");
            bodyParamCheck.put("uuid", dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", "fake");
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                Integer id = (Integer) bodyParam.get("id");
                String subjectValue = (String) bodyParam.get("subjectValue");
                if (subjectValue == null || subjectValue.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "subjectValue khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "subjectValue khong the trong", null));
                    return response;
                }
                String method = (String) bodyParam.get("method");
                if ((method == null || method.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "method khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "method khong the trong", null));
                    return response;
                }
                String effect = (String) bodyParam.get("effect");
                if ((effect == null || effect.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "effect khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "effect khong the trong", null));
                    return response;
                }
                String policyType = (String) bodyParam.get("policyType");
                if ((policyType == null || policyType.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "policyType khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "policyType khong the trong", null));
                    return response;
                }
                String resourceCode = (String) bodyParam.get("resourceCode");
                if ((resourceCode == null || resourceCode.isEmpty())) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "resourceCode khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "resourceCode khong the trong", null));
                    return response;
                }
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String rules = objectMapper.writeValueAsString(bodyParam.get("rules"));
                    String subjectType = (String) bodyParam.get("subjectType");
                    String subjectCondition = (String) bodyParam.get("subjectCondition");
                    Policy policy = new Policy();
                    policy.setSubjectValue(subjectValue);
                    policy.setSubjectCondition(subjectCondition);
                    policy.setPolicyType(policyType);
                    policy.setEffect(effect);
                    policy.setSubjectType(subjectType);
                    if (!policyType.equals("*")) {
                        policy.setRules(rules);
                    }
                    policy.setMethod(method);
                    policy.setResourceCode(resourceCode);
                    if (id == null || id.equals(0L)) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "id không thể null", null));
                    } else {
                        policy.setId(id);
                        Optional<Policy> policyOptional = policyService.findById(id);
                        if (policyOptional.isPresent()) {
                            Policy policyDb = policyOptional.get();
                            boolean check = false;
                            if (!policy.getResourceCode().equals(policyDb.getResourceCode())) {
                                check = true;
                            }
                            if (!policy.getSubjectValue().equals(policyDb.getSubjectValue())) {
                                check = true;
                            }
                            if (!policy.getSubjectCondition().equals(policyDb.getSubjectCondition())) {
                                check = true;
                            }
                            if (!policy.getSubjectType().equals(policyDb.getSubjectType())) {
                                check = true;
                            }
                            if (!policy.getEffect().equals(policyDb.getEffect())) {
                                check = true;
                            }
                            if (!policy.getMethod().equals(policyDb.getMethod())) {
                                check = true;
                            }
                            if (!policy.getPolicyType().equals(policyDb.getPolicyType())) {
                                check = true;
                            }
                            if (check) {
                                Optional<Policy> policyOptional1 = policyService.findUnique(policy.getResourceCode(), policy.getSubjectValue(), policy.getSubjectCondition(), policy.getSubjectType(),
                                        policy.getEffect(), policy.getMethod(), policy.getPolicyType());
                                if (policyOptional1.isPresent()) {
                                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
                                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi Unique",
                                                    null));
                                }
                            }

                        } else {
                            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "id không tồn tại",
                                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "id không tồn tại",
                                            null));
                        }
                        policy = policyService.updatePolicy(policy);
                        if (policy != null) {
                            response = new ResponseMessage(new MessageContent(policy));
                        } else {
                            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                            null));
                        }
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi parser rules",
                                    null));
                }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền cập nhập vào dữ liệu chính sách",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền cập nhập vào dữ liệu chính sách",
                                null));
            }


        }
        return response;
    }

    public ResponseMessage deletePolicy(Map<String, String> headerParam, String pathParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            if (pathParam == null || pathParam.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                Integer id = Integer.parseInt(pathParam);
                if (id == null || id.equals(0L)) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    Optional<Policy> policy = policyService.findById(id);
                    if (policy.isPresent()) {
                        Map<String, Object> bodyParamCheck = new HashMap<>();
                        bodyParamCheck.put("api", "/v1.0/abac/policy");
                        bodyParamCheck.put("method", "DELETE");
                        bodyParamCheck.put("uuid", dto.getUuid());
                        Map<String, Object> attributes = new HashMap<>();
                        attributes.put("roleCode", policy.get().getSubjectValue());
                        bodyParamCheck.put("attributes", attributes);
                        ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
                        if (resultCheckDto.getStatus()) {

                            Boolean result = policyService.deletePolicyId(policy.get());
                            if (result) {
                                response = new ResponseMessage(new MessageContent("Xóa chính sách thành công"));
                            } else {
                                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                            }
                        } else {
                            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu chinh sach",
                                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu chinh sach",
                                            null));
                        }
                    } else {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                    }
                }
            }
        }
        return response;
    }
}
