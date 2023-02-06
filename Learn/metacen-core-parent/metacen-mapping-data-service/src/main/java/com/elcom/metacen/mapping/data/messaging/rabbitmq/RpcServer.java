package com.elcom.metacen.mapping.data.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.mapping.data.controller.MappingAisMetacenController;
import com.elcom.metacen.mapping.data.controller.MappingVsatMetacenController;
import com.elcom.metacen.mapping.data.exception.ValidationException;
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
    MappingVsatMetacenController mappingVsatMetacenController;

    @Autowired
    private MappingAisMetacenController mappingAisMetacenController;

    @RabbitListener(queues = "${mapping-data.rpc.queue}")
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
                        if ("/mapping-data/vsat-metacen".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.getById(requestPath, headerParam, pathParam);
                        else if ("/mapping-data/ais-metacen".equalsIgnoreCase(requestPath))
                            response = mappingAisMetacenController.getById(requestPath, headerParam, pathParam);
                        break;

                    case "POST":
                        if ("/mapping-data/vsat-metacen".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.insert(headerParam, bodyParam, requestPath);

                            //filter mapping-data-vsat-metacen
                        else if ("/mapping-data/vsat-metacen/filter".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.filterMappingVsat(headerParam, bodyParam, requestPath);

                        else if ("/mapping-data/vsat-metacen/check-mapping".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.isMappingExist(headerParam, bodyParam, requestPath);

                        else if ("/mapping-data/vsat-metacen/check-mapping-by-object-uuid".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.isMappingExistByObjectUuid(headerParam, bodyParam, requestPath);

                        else if ("/mapping-data/ais-metacen".equalsIgnoreCase(requestPath))
                            response = mappingAisMetacenController.insert(headerParam, bodyParam, requestPath);

                            //filter mapping-data-ais-metacen
                        else if ("/mapping-data/ais-metacen/filter".equalsIgnoreCase(requestPath))
                            response = mappingAisMetacenController.filterMappingAis(headerParam, bodyParam, requestPath);
                        break;
                    case "PUT":
                        if ("/mapping-data/vsat-metacen".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.update(headerParam, bodyParam, pathParam, requestPath);
                        else if ("/mapping-data/ais-metacen".equalsIgnoreCase(requestPath))
                            response = mappingAisMetacenController.update(headerParam, bodyParam, pathParam, requestPath);
                        else if ("/mapping-data/vsat-metacen/change-name".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.updateNameObjectInternal(bodyParam);
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
                        if ("/mapping-data/vsat-metacen".equalsIgnoreCase(requestPath))
                            response = mappingVsatMetacenController.delete(requestPath, headerParam, pathParam);
                        else if ("/mapping-data/ais-metacen".equalsIgnoreCase(requestPath))
                            response = mappingAisMetacenController.delete(requestPath, headerParam, pathParam);
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
