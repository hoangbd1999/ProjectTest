package com.elcom.metacen.comment.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.comment.exception.ValidationException;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.comment.business.CommentBusiness;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
    
    @Autowired
    private CommentBusiness commentBusiness;

    @RabbitListener(queues = "${comment.rpc.queue}")
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
                        if("/comment".equalsIgnoreCase(requestPath))
                            response = this.commentBusiness.getComment(requestPath, headerParam, urlParam);
                        break;
                    case "POST":
                        if ("/comment".equalsIgnoreCase(requestPath))
                            response = this.commentBusiness.insert(headerParam, bodyParam, requestPath);
                        break;
                    case "PUT":
                        if ("/comment".equalsIgnoreCase(requestPath))
                        response = this.commentBusiness.update(headerParam, bodyParam, pathParam, requestPath);
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
                        if ("/comment".equalsIgnoreCase(requestPath))
                            response = this.commentBusiness.delete(requestPath, headerParam, pathParam);
                        break;
                    default:
                        break;
                }
            }

            LOGGER.info(" [<--] Server returned status {}", (response != null ? response.getStatus() : "null"));
            return response != null ? response.toJsonString() : null;
        } catch (Exception ex) {
            LOGGER.error("Error to processService >>> " + StringUtil.printException(ex));
            ex.printStackTrace();
        }

        return null;
    }
}
