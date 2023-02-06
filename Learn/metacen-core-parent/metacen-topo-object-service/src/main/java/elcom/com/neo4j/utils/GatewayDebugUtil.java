/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class GatewayDebugUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayDebugUtil.class);

    public static void debug(String requestPath, String urlParam, String pathParam,
            Map<String, Object> bodyParam, Map<String, String> headerParam) {
        LOGGER.info("requestPath: " + requestPath + ", urlParam: " + urlParam + ", pathParam: " + pathParam);
        if (bodyParam != null && !bodyParam.isEmpty()) {
            LOGGER.info("bodyParam");
            Iterator<Map.Entry<String, Object>> iterator = bodyParam.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                LOGGER.info(entry.getKey() + " => " + entry.getValue());
            }
        }
        if (headerParam != null && !headerParam.isEmpty()) {
            LOGGER.info("headerParam");
            Iterator<Map.Entry<String, String>> iterator = headerParam.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                LOGGER.info(entry.getKey() + " => " + entry.getValue());
            }
        }
    }
}
