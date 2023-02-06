/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.messaging.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author Admin
 */
@Component
public class RabbitMQProperties {

    // User service
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

    // Menu Management Service
    @Value("${menu-management.role.menu}")
    public static String MANAGEMENT_ROLE_MENU;

    @Value("${menu-management.rpc.queue}")
    public static String MENU_MANAGEMENT_RPC_QUEUE;

    @Value("${menu-management.rpc.exchange}")
    public static String MENU_MANAGEMENT_RPC_EXCHANGE;

    @Value("${menu-management.rpc.key}")
    public static String MENU_MANAGEMENT_RPC_KEY;

    // ABAC
    @Value("${abac.rpc.exchange}")
    public static String ABAC_RPC_EXCHANGE;

    @Value("${abac.rpc.queue}")
    public static String ABAC_RPC_QUEUE;

    @Value("${abac.rpc.key}")
    public static String ABAC_RPC_KEY;

    @Value("${abac.rpc.role.url}")
    public static String ABAC_RPC_ROLE_URL;

    @Value("${abac.rpc.author.url}")
    public static String ABAC_RPC_AUTHOR_URL;

    @Value("${abac.rpc.attribute.url}")
    public static String ABAC_RPC_ATTRIBUTE_URL;

    //ENRICH DATA
    @Value("${enrich-data.rpc.queue}")
    public static String ENRICH_DATA_RPC_QUEUE;

    @Value("${enrich-data.rpc.exchange}")
    public static String ENRICH_DATA_RPC_EXCHANGE;

    @Value("${enrich-data.rpc.key}")
    public static String ENRICH_DATA_RPC_KEY;

    @Value("${enrich-data.analyzed-object.url}")
    public static String ENRICH_DATA_ANALYZED_OBJECT_URL;

    @Value("${enrich-data.vsat-metacen.change-name.url}")
    public static String ENRICH_DATA_VSAT_METACEN_CHANGE_NAME_URL;

    @Value("${mapping-data.rpc.queue}")
    public static String MAPPING_DATA_RPC_QUEUE;

    @Value("${mapping-data.rpc.exchange}")
    public static String MAPPING_DATA_RPC_EXCHANGE;

    @Value("${mapping-data.rpc.key}")
    public static String MAPPING_DATA_RPC_KEY;

    @Value("${mapping-data.vsat-metacen.check-mapping.url}")
    public static String MAPPING_DATA_VSAT_METACEN_CHECK_MAPPING_URL;

    @Value("${mapping-data.vsat-metacen.change-name.url}")
    public static String MAPPING_DATA_VSAT_METACEN_CHANGE_NAME_URL;

    @Value("${link.object.update.note.rpc.internal}")
    public static String LINK_OBJECT_UPDATE_NOTE_URL;

    @Value("${link.object.worker.queue.contains}")
    public static String LINK_OBJECT_WORKER_QUEUE_CONTAINS;

    @Value("${link.object.worker.queue.delete.node}")
    public static String LINK_OBJECT_WORKER_QUEUE_DELETE_NODE;

    @Autowired
    public RabbitMQProperties(@Value("${user.rpc.exchange}") String userRpcExchange,
                              @Value("${user.rpc.queue}") String userRpcQueue,
                              @Value("${user.rpc.key}") String userRpcKey,
                              @Value("${user.rpc.authen.url}") String userRpcAuthenUrl,
                              @Value("${user.rpc.uuidLst.url}") String userRpcUuidLstUrl,
                              @Value("${user.rpc.internal.list.url}") String userRpcInternalLstUrl,
                              @Value("${menu-management.role.menu}") String managementRoleMenu,
                              @Value("${menu-management.rpc.queue}") String menumanagementRpcQueue,
                              @Value("${menu-management.rpc.exchange}") String menumanagementRpcExchange,
                              @Value("${menu-management.rpc.key}") String menumanagementRpcKey,
                              @Value("${abac.rpc.exchange}") String abacRpcExchange,
                              @Value("${abac.rpc.queue}") String abacRpcQueue,
                              @Value("${abac.rpc.key}") String abacRpcKey,
                              @Value("${abac.rpc.role.url}") String abacRpcRoleUrl,
                              @Value("${abac.rpc.author.url}") String abacRpcAuthorUrl,
                              @Value("${abac.rpc.attribute.url}") String abacRpcAttribute,
                              @Value("${enrich-data.rpc.queue}") String enrichDatRpcQueue,
                              @Value("${enrich-data.rpc.exchange}") String enrichDataRpcExchange,
                              @Value("${enrich-data.rpc.key}") String enrichDataRpcKey,
                              @Value("${enrich-data.analyzed-object.url}") String enrichDataAnalyzedObjectUrl,
                              @Value("${enrich-data.vsat-metacen.change-name.url}") String enrichDataVsatMetacenChangeNameUrl,
                              @Value("${mapping-data.rpc.queue}") String mappingDataRpcQueue,
                              @Value("${mapping-data.rpc.exchange}") String mappingDataRpcExchange,
                              @Value("${mapping-data.rpc.key}") String mappingDataRpcKey,
                              @Value("${mapping-data.vsat-metacen.check-mapping.url}") String mappingDataVsatMetacenCheckMappingUrl,
                              @Value("${mapping-data.vsat-metacen.change-name.url}") String mappingDataVsatMetacenChangeNameUrl,
                              @Value("${link.object.update.note.rpc.internal}") String linkObjectUpdateNoteUrl,
                              @Value("${link.object.worker.queue.contains}") String linkObjectWorkerQueueContains,
                              @Value("${link.object.worker.queue.delete.node}") String linkObjectWorkerQueueDeleteNode
    ) {
        //USER
        USER_RPC_EXCHANGE = userRpcExchange;
        USER_RPC_QUEUE = userRpcQueue;
        USER_RPC_KEY = userRpcKey;
        USER_RPC_AUTHEN_URL = userRpcAuthenUrl;
        USER_RPC_UUIDLIST_URL = userRpcUuidLstUrl;
        USER_RPC_INTERNAL_LIST_URL = userRpcInternalLstUrl;

        //MENU MANAGEMENT
        MANAGEMENT_ROLE_MENU = managementRoleMenu;
        MENU_MANAGEMENT_RPC_QUEUE = menumanagementRpcQueue;
        MENU_MANAGEMENT_RPC_EXCHANGE = menumanagementRpcExchange;
        MENU_MANAGEMENT_RPC_KEY = menumanagementRpcKey;

        //ABAC
        ABAC_RPC_EXCHANGE = abacRpcExchange;
        ABAC_RPC_QUEUE = abacRpcQueue;
        ABAC_RPC_KEY = abacRpcKey;
        ABAC_RPC_ROLE_URL = abacRpcRoleUrl;
        ABAC_RPC_AUTHOR_URL = abacRpcAuthorUrl;
        ABAC_RPC_ATTRIBUTE_URL = abacRpcAttribute;

        //ENRICH
        ENRICH_DATA_RPC_QUEUE = enrichDatRpcQueue;
        ENRICH_DATA_RPC_EXCHANGE = enrichDataRpcExchange;
        ENRICH_DATA_RPC_KEY = enrichDataRpcKey;
        ENRICH_DATA_ANALYZED_OBJECT_URL = enrichDataAnalyzedObjectUrl;
        ENRICH_DATA_VSAT_METACEN_CHANGE_NAME_URL = enrichDataVsatMetacenChangeNameUrl;

        //MAPPING
        MAPPING_DATA_RPC_QUEUE = mappingDataRpcQueue;
        MAPPING_DATA_RPC_EXCHANGE = mappingDataRpcExchange;
        MAPPING_DATA_RPC_KEY = mappingDataRpcKey;
        MAPPING_DATA_VSAT_METACEN_CHECK_MAPPING_URL = mappingDataVsatMetacenCheckMappingUrl;
        MAPPING_DATA_VSAT_METACEN_CHANGE_NAME_URL = mappingDataVsatMetacenChangeNameUrl;

        //Link Object Update Note
        LINK_OBJECT_UPDATE_NOTE_URL = linkObjectUpdateNoteUrl;
        LINK_OBJECT_WORKER_QUEUE_CONTAINS = linkObjectWorkerQueueContains;
        LINK_OBJECT_WORKER_QUEUE_DELETE_NODE = linkObjectWorkerQueueDeleteNode;

    }
}
