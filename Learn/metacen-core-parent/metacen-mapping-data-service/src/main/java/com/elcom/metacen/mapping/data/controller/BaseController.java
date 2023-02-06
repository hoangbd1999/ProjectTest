package com.elcom.metacen.mapping.data.controller;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.mapping.data.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.mapping.data.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.mapping.data.model.dto.ABACResponseDTO;
import com.elcom.metacen.mapping.data.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.mapping.data.model.dto.RoleMenuDTO;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
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
            //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //mapper.setDateFormat(df);
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

    public List<RoleMenuDTO> authorizeMenu(String userUuid) {
        RequestMessage abacRpcRequest = new RequestMessage();
        abacRpcRequest.setRequestMethod("GET");
        abacRpcRequest.setRequestPath(RabbitMQProperties.MANAGEMENT_ROLE_MENU);
        abacRpcRequest.setPathParam(userUuid);
        abacRpcRequest.setUrlParam(null);
        abacRpcRequest.setHeaderParam(null);
        LOGGER.info("------>REQUEST" + abacRpcRequest.toString());
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.MENU_MANAGEMENT_RPC_EXCHANGE,
                RabbitMQProperties.MENU_MANAGEMENT_RPC_QUEUE, RabbitMQProperties.MENU_MANAGEMENT_RPC_KEY,
                abacRpcRequest.toJsonString());
        LOGGER.info("------>RESULT" + result.toString());
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    JsonNode jsonNode = mapper.readTree(result);
                    List<RoleMenuDTO> resultCheckDto = mapper.readerFor(new TypeReference<List<RoleMenuDTO>>() {
                    }).readValue(jsonNode.get("data").get("data"));
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

    public ABACResponseDTO authorizeABAC(Map<String, Object> bodyParam, String requestMethod, String userUuid, String apiPath) {
        Map<String, Object> bodyParamSend = new HashMap<>(bodyParam);
        bodyParamSend.put("uuid", userUuid);
        bodyParamSend.put("api", ResourcePath.VERSION + apiPath);
        bodyParamSend.put("method", requestMethod);
        RequestMessage abacRpcRequest = new RequestMessage();
        abacRpcRequest.setRequestMethod("POST");
        abacRpcRequest.setRequestPath(RabbitMQProperties.ABAC_RPC_AUTHOR_URL);
        abacRpcRequest.setBodyParam(bodyParamSend);
        abacRpcRequest.setUrlParam(null);
        abacRpcRequest.setHeaderParam(null);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ABAC_RPC_EXCHANGE,
                RabbitMQProperties.ABAC_RPC_QUEUE, RabbitMQProperties.ABAC_RPC_KEY,
                abacRpcRequest.toJsonString());
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

    public Map<String, Object> requestABACAttribute(String requestMethod, String userUuid, String apiPath) {
        Map<String, Object> bodyParamSend = new HashMap<>();
        bodyParamSend.put("uuid", userUuid);
        bodyParamSend.put("api", ResourcePath.VERSION + apiPath);
        bodyParamSend.put("method", requestMethod);
        RequestMessage abacRpcRequest = new RequestMessage();
        abacRpcRequest.setRequestMethod("POST");
        abacRpcRequest.setRequestPath(RabbitMQProperties.ABAC_RPC_ATTRIBUTE_URL);
        abacRpcRequest.setBodyParam(bodyParamSend);
        abacRpcRequest.setUrlParam(null);
        abacRpcRequest.setHeaderParam(null);

        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ABAC_RPC_EXCHANGE,
                RabbitMQProperties.ABAC_RPC_QUEUE, RabbitMQProperties.ABAC_RPC_KEY,
                abacRpcRequest.toJsonString());

        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    JsonNode jsonNode = mapper.readTree(result);
                    Map<String, Object> resultCheckDto = mapper.treeToValue(jsonNode.get("data").get("data"), HashMap.class);
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

    public ResponseMessage unauthorizedResponse() {
        return new ResponseMessage(
                HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
    }

    public ResponseMessage forbiddenResponse(String message) {
        return new ResponseMessage(
                HttpStatus.FORBIDDEN.value(), message,
                new MessageContent(HttpStatus.FORBIDDEN.value(), message, null));
    }

    public ResponseMessage badRequestResponse(String message) {
        return new ResponseMessage(
                HttpStatus.BAD_REQUEST.value(), message,
                new MessageContent(HttpStatus.BAD_REQUEST.value(), message, null));
    }

    public ResponseMessage successResponse(Object data) {
        return new ResponseMessage(
                HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data));
    }
}
