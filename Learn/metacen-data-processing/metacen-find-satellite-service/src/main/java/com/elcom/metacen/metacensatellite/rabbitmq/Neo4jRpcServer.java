package com.elcom.metacen.metacensatellite.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 *
 * @author Admin
 */

public class Neo4jRpcServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jRpcServer.class);


    @RabbitListener(queues = "${satellite.rpc.queue}")
    public String processService(String json) throws Exception {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            mapper.setDateFormat(df);
//            RequestMessage request = mapper.readValue(json, RequestMessage.class);
//
//            ResponseMessage response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null);
//            if (request != null) {
//                String requestPath = request.getRequestPath();
//                Map<String, Object> bodyParam = request.getBodyParam();
//                Map<String, String> headerParam = request.getHeaderParam();
//
//                switch (request.getRequestMethod()) {
//
//                    case "GET":
//                        if("/v1.0/neo4j/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.getTopology( headerParam,request.getUrlParam());
//                        else if("/v1.0/neo4j/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.getTopologyNode( headerParam,request.getUrlParam());
//                        break;
//
//                    case "POST":
//                        if("/v1.0/neo4j/node".equalsIgnoreCase(requestPath)) // Tìm kiếm danh sách media
//                            response = neo4jController.addNode(bodyParam,headerParam);
//                        else if("/v1.0/neo4j/graph".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.getGraph(bodyParam, headerParam);
//                        else if("/v1.0/neo4j/graph/deep".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.getGraphDeep(bodyParam, headerParam);
//                        else if("/v1.0/neo4j/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.addTopology(bodyParam, headerParam);
//                        else if("/v1.0/neo4j/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.addNodeInfo(bodyParam, headerParam);
//
//                        break;
//
//                    case "PUT":
//                        if("/v1.0/neo4j/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.updateTopology(bodyParam, headerParam);
//                        else  if("/v1.0/neo4j/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.updateTopologyNode(bodyParam, headerParam);
//                        break;
//
//                    case "PATCH":
//                        response = null;
//                        break;
//
//                    case "DELETE":
//                        if("/v1.0/neo4j/topology".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.deleteTopo(bodyParam, headerParam);
//                        else  if("/v1.0/neo4j/topology/node".equalsIgnoreCase(requestPath)) // Tìm kiếm chi tiết một media cụ thể
//                            response = neo4jController.deleteTopoNode(bodyParam, headerParam);
//                        break;
//
//                    default:
//                        break;
//                }
//            }
//            return response != null ? response.toJsonString() : null;
//        } catch (Exception ex) {
//            LOGGER.error("ex: ", ex);
//        }
        return null;
    }
}
