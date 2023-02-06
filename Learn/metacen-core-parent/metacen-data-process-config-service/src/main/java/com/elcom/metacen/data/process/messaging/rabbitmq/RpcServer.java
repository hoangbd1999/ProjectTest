package com.elcom.metacen.data.process.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.data.process.controller.DataProcessConfigController;
import com.elcom.metacen.data.process.controller.ObjectGroupConfigController;
import com.elcom.metacen.data.process.exception.ValidationException;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
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
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Autowired
    DataProcessConfigController dataProcessConfigController;

    @Autowired
    private ObjectGroupConfigController objectGroupConfigController;

    @RabbitListener(queues = "${data-process-config.rpc.queue}")
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
                        // call service to service
                        if ("/data-process-config/data-process/internal".equalsIgnoreCase(requestPath))
                            response = dataProcessConfigController.getListDataProcessConfig(requestPath, headerParam);
                        else if ("/data-process-config/object-group-general-config".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.getById(headerParam, requestPath);
                        break;
                    case "POST":
                        if ("/data-process-config/data-process".equalsIgnoreCase(requestPath))
                            response = dataProcessConfigController.insert(headerParam, bodyParam, requestPath);
                        else if ("/data-process-config/data-process/filter".equalsIgnoreCase(requestPath))
                            response = dataProcessConfigController.filterDataProcessConfig(headerParam, bodyParam, requestPath);
                        else if ("/data-process-config/data-process/status-change".equalsIgnoreCase(requestPath))
                            response = dataProcessConfigController.statusChange(headerParam, bodyParam, requestPath);
                            // Object group config
                        else if ("/data-process-config/object-group-config/object-detection".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.insert(headerParam, bodyParam, requestPath);
                        else if ("/data-process-config/object-group-config/object-detection/filter".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.filterObjectGroupConfig(headerParam, bodyParam, requestPath);
                        else if ("/data-process-config/object-group-config/object-detection/status-change".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.statusChange(headerParam, bodyParam, requestPath);
                        break;
                    case "PUT":
                        if ("/data-process-config/data-process".equalsIgnoreCase(requestPath))
                            response = dataProcessConfigController.update(headerParam, bodyParam, pathParam, requestPath);
                            // update object group config
                        else if ("/data-process-config/object-group-config/object-detection".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.update(headerParam, bodyParam, pathParam, requestPath);
                        // update t/g and khoảng cách
                        else if ("/data-process-config/object-group-general-config".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.updateTimeAndDistance(headerParam, bodyParam, requestPath);
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
                        if ("/data-process-config/data-process".equalsIgnoreCase(requestPath))
                            response = dataProcessConfigController.delete(requestPath, headerParam, pathParam);
                            // delete object group config
                        else if ("/data-process-config/object-group-config/object-detection".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.delete(requestPath, headerParam, pathParam);
                        else if ("/data-process-config/object-group-config/check-exist-object-group".equalsIgnoreCase(requestPath))
                            response = objectGroupConfigController.checkExistObjectGroup(requestPath, headerParam, pathParam);
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
