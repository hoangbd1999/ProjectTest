package com.elcom.metacen.notify.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.notify.controller.NotifyController;
import com.elcom.metacen.notify.exception.ValidationException;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Autowired
    private NotifyController notifyController;

    @RabbitListener(queues = "${notify.rpc.queue}")
    public String processService(String json) throws ValidationException {
        try {
            LOGGER.info(" [-->] Server received request for " + json);

            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);

            //Process here
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
                        if ("/notify".equalsIgnoreCase(requestPath)) {
                            if (StringUtils.isNotBlank(pathParam)) {
                            } else {
                                response = notifyController.getNotifyByUser(headerParam, requestPath, request.getRequestMethod(), urlParam);
                            }
                        } else if ("/notify/number-notify".equalsIgnoreCase(requestPath)) {
                            response = notifyController.getNumberNotifyByUser(headerParam, requestPath, request.getRequestMethod(), urlParam);
                        }
                        break;
                    case "POST":
                        if ("/notify/send-notify".equalsIgnoreCase(requestPath)) { // Send notify for service to service
                            response = notifyController.sendNotify(bodyParam);
                        } else if ("/notify/read-all-notify".equalsIgnoreCase(requestPath)) {
                            response = notifyController.readAllNotify(requestPath, headerParam, bodyParam, request.getRequestMethod());
                        } else if ("/notify/read-notify".equalsIgnoreCase(requestPath)) {
                            response = notifyController.readOneNotify(headerParam, requestPath, request.getRequestMethod(), bodyParam);
                        }
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
            LOGGER.info(" [<--] Server returned " + response != null ? response.toJsonString() : "null");
            return response != null ? response.toJsonString() : null;
        } catch (Exception ex) {
            LOGGER.error("Error to processService >>> " + StringUtil.printException(ex));
            ex.printStackTrace();
        }

        return null;
    }
}
