package elcom.com.neo4j.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import elcom.com.neo4j.bussiness.Neo4jController;
import elcom.com.neo4j.exception.ValidationException;
import elcom.com.neo4j.message.RequestMessage;
import elcom.com.neo4j.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 *
 * @author Admin
 */

public class Neo4jRpcServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jRpcServer.class);

    @Autowired
    private Neo4jController topologyController;


    @RabbitListener(queues = "${topology.rpc.queue}")
    public String processService(String json) throws ValidationException {
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            ResponseMessage response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null);
            if (request != null) {
                String requestPath = request.getRequestPath();
                Map<String, Object> bodyParam = request.getBodyParam();
                Map<String, String> headerParam = request.getHeaderParam();

                switch (request.getRequestMethod()) {
                    
                    case "GET":
                        if("/v1.0/topology/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.getTopology( headerParam,request.getUrlParam());
                        else if("/v1.0/topology/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.getTopologyNode( headerParam,request.getUrlParam());
                        break;
                        
                    case "POST":
                        if("/v1.0/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm danh sách media
                            response = topologyController.addNode(bodyParam,headerParam);
                        else if("/v1.0/topology/graph".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.getGraph(bodyParam, headerParam);
                        else if("/v1.0/topology/graph/deep".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.getGraphDeep(bodyParam, headerParam);
                        else if("/v1.0/topology/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.addTopology(bodyParam, headerParam);
                        else if("/v1.0/topology/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.addNodeInfo(bodyParam, headerParam);

                        break;
                        
                    case "PUT":
                        if("/v1.0/topology/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.updateTopology(bodyParam, headerParam);
                        else  if("/v1.0/topology/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.updateTopologyNode(bodyParam, headerParam);
                        break;
                        
                    case "PATCH":
                        response = null;
                        break;
                        
                    case "DELETE":
                        if("/v1.0/topology/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.deleteTopo(bodyParam, headerParam);
                        else  if("/v1.0/topology/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
                            response = topologyController.deleteTopoNode(bodyParam, headerParam);
                        break;
                        
                    default:
                        break;
                }
            }
            return response != null ? response.toJsonString() : null;
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
}
