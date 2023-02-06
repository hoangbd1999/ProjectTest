package com.elcom.metacen.satelliteimage.process.messaging.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class RabbitMQProperties {

    public static String USER_RPC_EXCHANGE;

    public static String USER_RPC_QUEUE;

    public static String USER_RPC_KEY;

    public static String USER_RPC_AUTHEN_URL;
    
    public static String DATA_PROCESS_LOGGING_WORKER_QUEUE;
    
    @Autowired
    public RabbitMQProperties(@Value("${user.rpc.exchange}") String userRpcExchange,
            @Value("${user.rpc.queue}") String userRpcQueue,
            @Value("${user.rpc.key}") String userRpcKey,
            @Value("${user.rpc.authen.url}") String userRpcAuthenUrl,
            @Value("${data.process.logging.worker.queue}") String dataProcessLoggingWorkerQueue) {
        
        USER_RPC_EXCHANGE = userRpcExchange;
        USER_RPC_QUEUE = userRpcQueue;
        USER_RPC_KEY = userRpcKey;
        USER_RPC_AUTHEN_URL = userRpcAuthenUrl;
        DATA_PROCESS_LOGGING_WORKER_QUEUE = dataProcessLoggingWorkerQueue;
    }
}
