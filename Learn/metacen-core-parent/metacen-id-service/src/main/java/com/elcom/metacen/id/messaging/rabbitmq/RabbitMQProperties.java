/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.messaging.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class RabbitMQProperties {

    @Value("${user.rpc.authen.url}")
    public static String USER_RPC_AUTHEN_URL;

    @Value("${user.rpc.exchange}")
    public static String USER_RPC_EXCHANGE;

    @Value("${user.rpc.queue}")
    public static String USER_RPC_QUEUE;

    @Value("${user.rpc.key}")
    public static String USER_RPC_KEY;

    @Value("${abac.rpc.exchange}")
    public static String ABAC_RPC_EXCHANGE;

    @Value("${abac.rpc.queue}")
    public static String ABAC_RPC_QUEUE;

    @Value("${abac.rpc.key}")
    public static String ABAC_RPC_KEY;

    @Value("${abac.rpc.author.url}")
    public static String ABAC_RPC_AUTHOR_URL;

    @Value("${delete.data.publish.exchange}")
    public static String DELETE_DATA_PUBLISH_EXCHANGE;

    @Value("${delete.data.publish.queue}")
    public static String DELETE_DATA_PUBLISH_QUEUE;

    @Value("${delete.data.publish.key}")
    public static String DELETE_DATA_PUBLISH_KEY;

    @Autowired
    public RabbitMQProperties(@Value("${user.rpc.exchange}") String userRpcExchange,
            @Value("${user.rpc.queue}") String userRpcQueue,
            @Value("${user.rpc.key}") String userRpcKey,
            @Value("${user.rpc.authen.url}") String userRpcAuthenUrl,
            @Value("${abac.rpc.exchange}") String abacRpcExchange,
            @Value("${abac.rpc.queue}") String abacRpcQueue,
            @Value("${abac.rpc.key}") String abacRpcKey,
            @Value("${abac.rpc.author.url}") String abacRpcAuthorUrl,
            @Value("${delete.data.publish.exchange}") String deleteDataPublishExchange,
            @Value("${delete.data.publish.queue}") String deleteDataPublishQueue,
            @Value("${delete.data.publish.key}") String deleteDataPublishKey
    ) {
        USER_RPC_EXCHANGE = userRpcExchange;
        USER_RPC_QUEUE = userRpcQueue;
        USER_RPC_KEY = userRpcKey;
        USER_RPC_AUTHEN_URL = userRpcAuthenUrl;

        ABAC_RPC_EXCHANGE = abacRpcExchange;
        ABAC_RPC_QUEUE = abacRpcQueue;
        ABAC_RPC_KEY = abacRpcKey;
        ABAC_RPC_AUTHOR_URL = abacRpcAuthorUrl;

        DELETE_DATA_PUBLISH_EXCHANGE = deleteDataPublishExchange;
        DELETE_DATA_PUBLISH_QUEUE = deleteDataPublishQueue;
        DELETE_DATA_PUBLISH_KEY = deleteDataPublishKey;
    }
}
