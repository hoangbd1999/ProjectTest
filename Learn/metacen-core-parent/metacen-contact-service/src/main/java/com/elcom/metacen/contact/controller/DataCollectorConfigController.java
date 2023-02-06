package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.DataCollectorConfig;
import com.elcom.metacen.contact.model.dto.ABACResponseDTO;
import com.elcom.metacen.contact.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.contact.service.impl.DataCollectorConfigImpl;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DataCollectorConfigController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCollectorConfigController.class);

    @Autowired
    DataCollectorConfigImpl service;

    public ResponseMessage updateConfigValue(String requestPath, Map<String, Object> bodyParam,
                                             Map<String, String> headerMap, String urlParam) throws JsonProcessingException {
        ResponseMessage response = null;
        AuthorizationResponseDTO auth = authenToken(headerMap);
        if (auth == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", auth.getUuid(), requestPath);

            if (abacStatus != null && abacStatus.getStatus()) {
                if (bodyParam == null || bodyParam.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    HashMap map = (HashMap) bodyParam.get("configValue");
                    ObjectMapper objectMapper = new ObjectMapper();
                    String configValue = objectMapper.writeValueAsString(map);

                    String collectType = StringUtil.objectToString(bodyParam.get("collectType"));
                    DataCollectorConfig dataCollectorConfig = service
                            .findByCollectType(collectType);
                    if (dataCollectorConfig == null)
                        return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Data-Collector-Config không tồn tại", null);

                    try {
                        boolean check = service.updateConfigValue(collectType, configValue);
                        if (check) {
                            response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                                    new MessageContent(HttpStatus.OK.value(),
                                            "Update dữ liệu thành công", ""));
                        } else {
                            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Update failed", null));
                        }
                    } catch (Exception e) {
                        String message = String.format("Error: %s", e.getMessage());
                        LOGGER.error(message, e);
                        return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                                new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                    }
                }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền sửa Data-Collector-Config",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền sửa Data-Collector-Config", null));
            }
        }
        return response;
    }


    public ResponseMessage findById(Map<String, String> headerParam, String urlParam, String requestPath) {

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<>();
        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
        ABACResponseDTO abacStatus = authorizeABAC(body, "GET", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String collectType = params.get("collectType");
            DataCollectorConfig dataCollectorConfig = service.findByCollectType(collectType);
            if (dataCollectorConfig == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Data-collector-config không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Data-collector-config không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(dataCollectorConfig));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }


    public ResponseMessage findAll(Map<String, String> headerParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "GET", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            List<DataCollectorConfig> dataCollectorConfig = service.findAll();
            if (dataCollectorConfig != null && dataCollectorConfig.size() > 0) {
                return new ResponseMessage(new MessageContent(dataCollectorConfig));
            }
            return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Không có dữ liệu",
                    new MessageContent(HttpStatus.NOT_FOUND.value(), "Không có dữ liệu", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateIsRunningProgress(String requestPath, Map<String, Object> bodyParam,
                                                   Map<String, String> headerMap, String urlParam) throws JsonProcessingException {
        ResponseMessage response = null;
        AuthorizationResponseDTO auth = authenToken(headerMap);
        if (auth == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", auth.getUuid(), requestPath);

            if (abacStatus != null && abacStatus.getStatus()) {
                if (bodyParam == null || bodyParam.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    String collectType = StringUtil.objectToString(bodyParam.get("collectType"));

                    boolean isRunningProgress = (Boolean) bodyParam.get("isRunning");
                    DataCollectorConfig dataCollectorConfig = service
                            .findByCollectType(collectType);
                    if (dataCollectorConfig == null)
                        return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Data-Collector-Config không tồn tại", null);

                    try {
                        boolean check = service.updateIsRunningProcess(collectType, isRunningProgress);
                        if (check) {
                            response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                                    new MessageContent(HttpStatus.OK.value(),
                                            "Update dữ liệu thành công", ""));
                        } else {
                            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Update failed", null));
                        }
                    } catch (Exception e) {
                        String message = String.format("Error: %s", e.getMessage());
                        LOGGER.error(message, e);
                        return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                                new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                    }
                }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền sửa Data-Collector-Config",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền sửa Data-Collector-Config", null));
            }
        }
        return response;
    }


}
