package com.elcom.metacen.vsat.media.process.messaging.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class RabbitMQProperties {

    @Value("${user.rpc.exchange}")
    public static String USER_RPC_EXCHANGE;

    @Value("${user.rpc.queue}")
    public static String USER_RPC_QUEUE;

    @Value("${user.rpc.key}")
    public static String USER_RPC_KEY;

    @Value("${user.rpc.authen.url}")
    public static String USER_RPC_AUTHEN_URL;

    @Value("${data.process.logging.worker.queue}")
    public static String DATA_PROCESS_LOGGING_WORKER_QUEUE;

    // contact
    @Value("${contact.rpc.exchange}")
    public static String CONTACT_RPC_EXCHANGE;

    @Value("${contact.rpc.queue}")
    public static String CONTACT_RPC_QUEUE;

    @Value("${contact.rpc.key}")
    public static String CONTACT_RPC_KEY;

    @Value("${contact.rpc.internal}")
    public static String CONTACT_RPC_INTERNAL;

    @Value("${contact.rpc.internal1}")
    public static String CONTACT_RPC_INTERNAL1;

    @Autowired
    public RabbitMQProperties(@Value("${user.rpc.exchange}") String userRpcExchange,
            @Value("${user.rpc.queue}") String userRpcQueue,
            @Value("${user.rpc.key}") String userRpcKey,
            @Value("${user.rpc.authen.url}") String userRpcAuthenUrl,
            @Value("${data.process.logging.worker.queue}") String dataProcessLoggingWorkerQueue,
            @Value("${contact.rpc.exchange}") String contactRpcExchange,
            @Value("${contact.rpc.queue}") String contactRpcQueue,
            @Value("${contact.rpc.key}") String contactRpcKey,
            @Value("${contact.rpc.internal}") String contactRpcInternal,
            @Value("${contact.rpc.internal1}") String contactRpcInternal1

    ) {

        USER_RPC_EXCHANGE = userRpcExchange;
        USER_RPC_QUEUE = userRpcQueue;
        USER_RPC_KEY = userRpcKey;
        USER_RPC_AUTHEN_URL = userRpcAuthenUrl;
        DATA_PROCESS_LOGGING_WORKER_QUEUE = dataProcessLoggingWorkerQueue;

        // Contact
        CONTACT_RPC_EXCHANGE = contactRpcExchange;
        CONTACT_RPC_QUEUE = contactRpcQueue;
        CONTACT_RPC_KEY = contactRpcKey;
        CONTACT_RPC_INTERNAL = contactRpcInternal;
        CONTACT_RPC_INTERNAL1 = contactRpcInternal1;
    }
}
