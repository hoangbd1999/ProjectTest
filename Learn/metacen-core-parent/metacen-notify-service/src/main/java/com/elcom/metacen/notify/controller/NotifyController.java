/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.controller;

import com.elcom.metacen.notify.model.dto.UserReceiverDTO;
import com.elcom.metacen.notify.model.dto.SocketNotifyListIdDTO;
import com.elcom.metacen.notify.model.dto.SocketNotifyDataDTO;
import com.elcom.metacen.notify.model.dto.NotifyDTO;
import com.elcom.metacen.notify.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.notify.model.dto.ABACResponseDTO;
import com.elcom.metacen.notify.model.dto.SocketNotifyMessageDTO;
import com.elcom.metacen.notify.model.dto.SocketNotifyRequestDTO;
import com.elcom.metacen.notify.model.dto.NotifyRequestDTO;
import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.notify.enums.StatusView;
import com.elcom.metacen.notify.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.notify.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.notify.model.Notify;
import com.elcom.metacen.notify.model.NotifyPK;
import com.elcom.metacen.notify.service.NotifyService;
import com.elcom.metacen.notify.validation.NotifyValidation;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Controller
public class NotifyController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyController.class);

    @Value("${socket.worker.queue}")
    private String workerQueue;

    @Value("${socket.service.name}")
    private String serviceName;
    @Value("${socket.emit.name}")
    private String emitName;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private RabbitMQClient rabbitMQClient;

    public ResponseMessage getNotifyByUser(Map<String, String> headerParam, String requestPath, String method, String urlParam) {
        LOGGER.info("getNotifyByUser - urlParam: {}", urlParam);
        
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            long now = System.currentTimeMillis() / 1000;

            Integer page = 0;
            Integer size = 20;
            String keyword = "";
            String typeIcon = "";
            Integer type = 3;
            long fromDate = now - (30 * 24 * 60 * 60);
            long toDate = now;
            if (StringUtils.isNotBlank(urlParam)) {
                Map<String, Object> query = getQueryMap(urlParam);
                page = query.get("page") != null ? Integer.parseInt((String) query.get("page")) : page;
                size = query.get("size") != null ? Integer.parseInt((String) query.get("size")) : size;
                keyword = query.get("keyword") != null ? (String) query.get("keyword") : "";
                type = query.get("type") != null ? Integer.parseInt((String) query.get("type")) : type;

                if (!StringUtil.isNullOrEmpty(keyword)) {
                    try {
                        keyword = URLDecoder.decode(keyword, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        LOGGER.error(ex.toString());
                    }
                }
                fromDate = query.get("fromDate") != null ? Long.parseLong((String) query.get("fromDate")) : fromDate;
                toDate = query.get("toDate") != null ? Long.parseLong((String) query.get("toDate")) : toDate;
                typeIcon = query.get("typeIcon") != null ? (String) query.get("typeIcon") : "";
            }
            
            List<String> typeIconList = StringUtils.isNotBlank(typeIcon) ? Arrays.asList(typeIcon.split("-")) : null;
            Map<String, Object> bodyCheck = new HashMap<>();
            Map<String, Object> attributesCheck = new HashMap<>();
            attributesCheck.put("typeIcon", typeIconList);
            bodyCheck.put("attributes", attributesCheck);
            ABACResponseDTO resultCheckABAC = authorizeABAC(bodyCheck, "LIST", dto.getUuid(), requestPath);
            if (resultCheckABAC == null || !resultCheckABAC.getStatus()) {
                return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem danh sách thông báo",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem danh sách thông báo", null));
            } else {
                Page<Notify> pagedResult = notifyService.findNotifyByUser(page, size, keyword, dto.getUuid(), fromDate, toDate, typeIconList, type);
                if (pagedResult != null && !pagedResult.isEmpty()) {
                    List<NotifyDTO> results = pagedResult.getContent()
                            .parallelStream()
                            .map(notifyService::transform)
                            .collect(Collectors.toList());

                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), results, pagedResult.getTotalElements()));
                } else {
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
                }
            }
        }

        return response;
    }

    public ResponseMessage getNumberNotifyByUser(Map<String, String> headerParam, String requestPath, String method, String urlParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            // Check ABAC
            Map<String, Object> body = new HashMap<>();
            ABACResponseDTO resultCheckABAC = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);
            if (resultCheckABAC == null) {
                return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền gửi đọc thông báo",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền gửi đọc thông báo", null));
            } else {
                long now = System.currentTimeMillis() / 1000;
                long fromDate = now - (30 * 24 * 60 * 60);
                long toDate = now;
                long countNotify = notifyService.countNotifyNotRead(dto.getUuid(), StatusView.NOT_SEEN.code(), fromDate, toDate, 3);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("countNotiNotRead", countNotify);

                response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), map));
            }
        }

        return response;
    }

    public ResponseMessage readAllNotify(String requestPath, Map<String, String> headerParam, Map<String, Object> bodyParam, String requestMethod) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            // Check ABAC
            Map<String, Object> body = new HashMap<>();
            ABACResponseDTO resultCheckABAC = authorizeABAC(body, requestMethod, dto.getUuid(), requestPath);
            if (resultCheckABAC == null || !resultCheckABAC.getStatus()) {
                return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền gửi đọc thông báo",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền gửi đọc thông báo", null));
            } else {
                String userId = (String) bodyParam.get("userId");
                boolean result = notifyService.updateStatusView(userId, StatusView.SEEN.code());
                if (result) {
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
                } else {
                    response = new ResponseMessage(new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            }
        }

        return response;
    }

    public ResponseMessage readOneNotify(Map<String, String> headerParam, String requestPath, String method, Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            // Check ABAC
            Map<String, Object> body = new HashMap<>();
            ABACResponseDTO resultCheckABAC = authorizeABAC(body, method, dto.getUuid(), requestPath);
            if (resultCheckABAC == null || !resultCheckABAC.getStatus()) {
                return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền gửi đọc thông báo",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền gửi đọc thông báo", null));
            } else {

                String userId = (String) bodyParam.get("userId");
                String notiId = (String) bodyParam.get("notiId");
                Notify result = notifyService.updateStatusViewOne(userId, notiId, StatusView.SEEN.code());
                if (result != null) {
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), result));
                } else {
                    response = new ResponseMessage(new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            }
        }

        return response;
    }

    public ResponseMessage sendNotify(Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        int type = (int) bodyParam.get("type");
        String title = (String) bodyParam.get("title");
        String content = (String) bodyParam.get("content");
        String url = (String) bodyParam.get("url");
        String objectType = (String) bodyParam.get("objectType");
        String objectUuid = (String) bodyParam.get("objectUuid");
        String userId = (String) bodyParam.get("userId");
        String siteId = (String) bodyParam.get("siteId");

        NotifyRequestDTO notifyDto = new NotifyRequestDTO(type, title, content, url, objectType, objectUuid, userId, siteId);
        String invalidData = new NotifyValidation().validate(notifyDto);
        if (invalidData != null) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
        } else {
            switch (type) {
                case 1:
                    // SMS
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
                    break;
                case 2:
                    // Email
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
                    break;
                case 3:
                    // Notify-App
                    response = sendNotifyInApp(notifyDto, userId);
                    break;
                case 4:
                    // MobileApp
                    response = notifyService.sendToMobileApp(notifyDto);
                    break;
                case 5:
                    // Export file
                    response = notifyService.notifyExportFile(notifyDto);
                    break;
                default:
                    response = new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(), "type is not support", null));
            }
        }

        return response;
    }

    private ResponseMessage sendNotifyInApp(NotifyRequestDTO notifyDto, String userId) {
        ResponseMessage response = null;
        
        ObjectMapper mapper = new ObjectMapper();
        List<UserReceiverDTO> usersList = getUserListFromIDService();
        usersList = mapper.convertValue(usersList, new TypeReference<List<UserReceiverDTO>>() {
        });
        if (!StringUtils.isBlank(userId)) {
            usersList = usersList.stream().filter(user -> user.getId().equals(userId)).collect(Collectors.toList());
        }
        if (usersList != null && !usersList.isEmpty()) {
            List<String> userIds = usersList.stream().map(item -> item.getId()).collect(Collectors.toList());

            // Push msg to socket work queue
            SocketNotifyRequestDTO socketNotifyRequest = buildSocketNotifyRequest(notifyDto, userIds);
            boolean result = rabbitMQClient.callWorkerService2(workerQueue, socketNotifyRequest.toJsonString());
            LOGGER.info("Socket work queue - Push to {} msg: {} => {}", workerQueue, socketNotifyRequest.toJsonString(), result);
            if (result) {
                long timestamp = System.currentTimeMillis() / 1000;
                // message
                SocketNotifyMessageDTO socketMessage = socketNotifyRequest.getData().getMessage();
                Notify entity = toEntity(notifyDto);
                entity.setUpdatedAt(timestamp);
                entity.setIcon(socketMessage.getIcon());
                entity.setTimeSendNotify(socketMessage.getTimeRequest());
                entity.setStatusView(StatusView.NOT_SEEN.code());

                // listId
                List<Notify> notifyList = new ArrayList<>();
                List<SocketNotifyListIdDTO> socketListId = socketNotifyRequest.getData().getListId();
                for (SocketNotifyListIdDTO item : socketListId) {
                    NotifyPK notifyPK = new NotifyPK(item.getNotifyId(), timestamp);
                    Notify notifyNew = toEntity(notifyDto);
                    notifyNew.setUpdatedAt(timestamp);
                    notifyNew.setIcon(socketMessage.getIcon());
                    notifyNew.setTimeSendNotify(socketMessage.getTimeRequest());
                    notifyNew.setStatusView(StatusView.NOT_SEEN.code());
                    notifyNew.setNotifyPK(notifyPK);
                    notifyNew.setUserId(item.getUserId());
                    notifyNew.setObjectId(entity.getObjectId());
                    notifyList.add(notifyNew);
                }
                // Save data to DB
                try {
                    notifyService.saveAll(notifyList);
                } catch (Exception ex) {
                    LOGGER.error("Error save notify >>> {}", ex.toString());
                }

                response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
            } else {
                LOGGER.error("sendNotifyInApp >>> push msg to socket is failed!");
                response = new ResponseMessage(new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            LOGGER.error("sendNotifyInApp >>> usersList is null");
            response = new ResponseMessage(new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
        
        return response;
    }

    private SocketNotifyRequestDTO buildSocketNotifyRequest(NotifyRequestDTO notifyDto, List<String> userIds) {
        long timestamp = System.currentTimeMillis() / 1000;

        // message
        String objectType = notifyDto.getObjectType();
        SocketNotifyMessageDTO notifyMsg = new SocketNotifyMessageDTO();
        notifyMsg.setType(notifyDto.getType());
        notifyMsg.setTitle(notifyDto.getTitle());
        notifyMsg.setContent(notifyDto.getContent());
        notifyMsg.setUrl(notifyDto.getUrl());
        notifyMsg.setTimeRequest(timestamp);
        notifyMsg.setObjectType(objectType);
        notifyMsg.setObjectUuid(notifyDto.getObjectUuid());
        notifyMsg.setEmitName(emitName);

        String icon = "";
        switch (objectType) {
            case "1": // Mất kết nối
                icon = "icon1";
                break;
            case "2": // Kết nối lại
                icon = "icon2";
                break;
            case "3": // Xâm nhập vùng cấm
                icon = "icon3";
                break;
            case "4": // Vượt rào
                icon = "icon4";
                break;
            case "5": // Phát hiện đối tượng theo dõi
                icon = "icon5";
                break;
        }
        notifyMsg.setIcon(icon);

        // listId
        List<SocketNotifyListIdDTO> notifyListIds = new ArrayList<>();
        userIds.forEach((item) -> {
            SocketNotifyListIdDTO notifyListId = new SocketNotifyListIdDTO();
            notifyListId.setNotifyId(UUID.randomUUID().toString());
            notifyListId.setUserId(item);

            notifyListIds.add(notifyListId);
        });

        // data
        SocketNotifyDataDTO socketNotifyDataDTO = new SocketNotifyDataDTO();
        socketNotifyDataDTO.setMessage(notifyMsg);
        socketNotifyDataDTO.setListId(notifyListIds);

        // notify request
        SocketNotifyRequestDTO socketNotifyRequestDto = new SocketNotifyRequestDTO();
        socketNotifyRequestDto.setServiceName(serviceName);
        socketNotifyRequestDto.setData(socketNotifyDataDTO);

        return socketNotifyRequestDto;
    }

    private Notify toEntity(NotifyRequestDTO notifyDto) {
        Notify notify = new Notify();
        notify.setType(notifyDto.getType());
        notify.setTitle(notifyDto.getTitle());
        notify.setContent(notifyDto.getContent());
        notify.setUrl(notifyDto.getUrl());
        notify.setObjectId(notifyDto.getObjectUuid());
        return notify;
    }

    public Map<String, Object> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, Object> map = new HashMap<String, Object>();
        String[] paramsList = params;
        for (String param : paramsList) {
            List<String> paramSplit = Arrays.asList(param.split("="));
            if (paramSplit.size() > 1) {
                String name = paramSplit.get(0);
                Object value = paramSplit.get(1);
                map.put(name, value);
            } else {
                String name = paramSplit.get(0);
                map.put(name, null);
            }

        }
        return map;
    }

    private List<UserReceiverDTO> getUserListFromIDService() {
        RequestMessage rbacRpcRequest = new RequestMessage();
        rbacRpcRequest.setRequestMethod("GET");
        rbacRpcRequest.setRequestPath(RabbitMQProperties.USER_RPC_INTERNAL_LIST_URL);
        rbacRpcRequest.setBodyParam(null);
        rbacRpcRequest.setUrlParam(null);
        rbacRpcRequest.setHeaderParam(null);
        rbacRpcRequest.setVersion(ResourcePath.VERSION);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY, rbacRpcRequest.toJsonString());
        LOGGER.info("getUserListFromIDService - result: " + result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    //OK
                    JsonNode jsonNode = mapper.readTree(result);
                    List<UserReceiverDTO> userReceiverDTOList = mapper.treeToValue(jsonNode.get("data").get("data"), List.class);
                    return userReceiverDTOList;
                }
            } catch (Exception ex) {
                LOGGER.info("Error parse json in getUserListFromIDService from ID service: " + ex.toString());
                return null;
            }
        }
        return null;
    }
}
