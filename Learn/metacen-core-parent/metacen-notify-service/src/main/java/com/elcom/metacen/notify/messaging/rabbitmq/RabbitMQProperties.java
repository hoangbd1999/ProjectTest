/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.messaging.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class RabbitMQProperties {

    //User service
    @Value("${user.rpc.exchange}")
    public static String USER_RPC_EXCHANGE;

    @Value("${user.rpc.queue}")
    public static String USER_RPC_QUEUE;

    @Value("${user.rpc.key}")
    public static String USER_RPC_KEY;

    @Value("${user.rpc.authen.url}")
    public static String USER_RPC_AUTHEN_URL;

    @Value("${user.rpc.uuidLst.url}")
    public static String USER_RPC_UUIDLIST_URL;

    @Value("${user.rpc.internal.list.url}")
    public static String USER_RPC_INTERNAL_LIST_URL;

    @Value("${group.rpc.internal.list.url}")
    public static String GROUP_RPC_INTERNAL_LIST_URL;

    @Value("${user.rpc.internal.list.bygroup.url}")
    public static String USER_RPC_INTERNAL_LIST_BYGROUP_URL;

    //Config
    @Value("${systemconfig.rpc.queue}")
    public static String SYSTEMCONFIG_RPC_QUEUE;

    @Value("${systemconfig.rpc.exchange}")
    public static String SYSTEMCONFIG_RPC_EXCHANGE;

    @Value("${systemconfig.rpc.key}")
    public static String SYSTEMCONFIG_RPC_KEY;

    @Value("${systemconfig.rpc.sites.list}")
    public static String SYSTEMCONFIG_RPC_SITES_LIST;

    @Value("${systemconfig.rpc.camera.list}")
    public static String SYSTEMCONFIG_RPC_CAMERA_LIST;

    @Value("${notify.worker.message.queue}")
    public static String WORKER_QUEUE_MESSAGE;

    //Config
    @Value("${socket.rpc.queue.name}")
    public static String SOCKET_RPC_QUEUE;

    @Value("${socket.rpc.exchange.name}")
    public static String SOCKET_RPC_EXCHANGE;

    @Value("${socket.rpc.routing.key}")
    public static String SOCKET_RPC_KEY;

    @Value("${management.role.menu}")
    public static String MANAGEMENT_ROLE_MENU;

    @Value("${menumanagement.rpc.queue}")
    public static String MENU_MANAGEMENT_RPC_QUEUE;

    @Value("${menumanagement.rpc.exchange}")
    public static String MENU_MANAGEMENT_RPC_EXCHANGE;

    @Value("${menumanagement.rpc.key}")
    public static String MENU_MANAGEMENT_RPC_KEY;

    //ABAC service
    @Value("${abac.rpc.exchange}")
    public static String ABAC_RPC_EXCHANGE;

    @Value("${abac.rpc.queue}")
    public static String ABAC_RPC_QUEUE;

    @Value("${abac.rpc.key}")
    public static String ABAC_RPC_KEY;

    @Value("${abac.rpc.author.url}")
    public static String ABAC_RPC_AUTHOR_URL;

    @Value("${abac.rpc.attribute}")
    public static String ABAC_RPC_ATTRIBUTE_URL;

    @Autowired
    public RabbitMQProperties(@Value("${user.rpc.exchange}") String userRpcExchange,
            @Value("${user.rpc.queue}") String userRpcQueue,
            @Value("${user.rpc.key}") String userRpcKey,
            @Value("${user.rpc.authen.url}") String userRpcAuthenUrl,
            @Value("${user.rpc.uuidLst.url}") String userRpcUuidLstUrl,
            @Value("${user.rpc.internal.list.url}") String userRpcInternalLstUrl,
            @Value("${group.rpc.internal.list.url}") String groupRpcInternalLstUrl,
            @Value("${user.rpc.internal.list.bygroup.url}") String userRpcInternalLstByGroupUrl,
            @Value("${systemconfig.rpc.queue}") String systemconfigRpcQueue,
            @Value("${systemconfig.rpc.exchange}") String systemconfigRpcExchange,
            @Value("${systemconfig.rpc.key}") String systemconfigRpcKey,
            @Value("${socket.rpc.queue.name}") String socketRpcQueue,
            @Value("${socket.rpc.exchange.name}") String socketRpcExchange,
            @Value("${socket.rpc.routing.key}") String socketRpcKey,
            @Value("${systemconfig.rpc.sites.list}") String systemconfigRpcSitesList,
            @Value("${systemconfig.rpc.camera.list}") String systemconfigRpcCameraList,
            @Value("${notify.worker.message.queue}") String messageQueueWorker,
            @Value("${management.role.menu}") String managementRoleMenu,
            @Value("${menumanagement.rpc.queue}") String menumanagementRpcQueue,
            @Value("${menumanagement.rpc.exchange}") String menumanagementRpcExchange,
            @Value("${menumanagement.rpc.key}") String menumanagementRpcKey,
            @Value("${abac.rpc.exchange}") String abacRpcExchange,
            @Value("${abac.rpc.queue}") String abacRpcQueue,
            @Value("${abac.rpc.key}") String abacRpcKey,
            @Value("${abac.rpc.author.url}") String abacRpcAuthorUrl,
            @Value("${abac.rpc.attribute}") String abacRpcAttribute
    ) {
        //User
        USER_RPC_EXCHANGE = userRpcExchange;
        USER_RPC_QUEUE = userRpcQueue;
        USER_RPC_KEY = userRpcKey;
        USER_RPC_AUTHEN_URL = userRpcAuthenUrl;
        USER_RPC_UUIDLIST_URL = userRpcUuidLstUrl;
        USER_RPC_INTERNAL_LIST_URL = userRpcInternalLstUrl;
        GROUP_RPC_INTERNAL_LIST_URL = groupRpcInternalLstUrl;
        USER_RPC_INTERNAL_LIST_BYGROUP_URL = userRpcInternalLstByGroupUrl;

        //CONFIG
        SYSTEMCONFIG_RPC_EXCHANGE = systemconfigRpcExchange;
        SYSTEMCONFIG_RPC_QUEUE = systemconfigRpcQueue;
        SYSTEMCONFIG_RPC_KEY = systemconfigRpcKey;
        SYSTEMCONFIG_RPC_SITES_LIST = systemconfigRpcSitesList;
        SYSTEMCONFIG_RPC_CAMERA_LIST = systemconfigRpcCameraList;
        WORKER_QUEUE_MESSAGE = messageQueueWorker;

        //SOCKET
        SOCKET_RPC_QUEUE = socketRpcQueue;

        SOCKET_RPC_EXCHANGE = socketRpcExchange;

        SOCKET_RPC_KEY = socketRpcKey;

        MANAGEMENT_ROLE_MENU = managementRoleMenu;
        MENU_MANAGEMENT_RPC_QUEUE = menumanagementRpcQueue;
        MENU_MANAGEMENT_RPC_EXCHANGE = menumanagementRpcExchange;
        MENU_MANAGEMENT_RPC_KEY = menumanagementRpcKey;

        //ABAC
        ABAC_RPC_EXCHANGE = abacRpcExchange;
        ABAC_RPC_QUEUE = abacRpcQueue;
        ABAC_RPC_KEY = abacRpcKey;
        ABAC_RPC_ATTRIBUTE_URL = abacRpcAttribute;
        ABAC_RPC_AUTHOR_URL = abacRpcAuthorUrl;
    }
}
