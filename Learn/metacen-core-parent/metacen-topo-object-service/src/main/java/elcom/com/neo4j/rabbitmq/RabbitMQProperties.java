/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class RabbitMQProperties {

    //RBAC service
    @Value("${contact.rpc.exchange}")
    public static String CONTACT_RPC_EXCHANGE;

    @Value("${contact.rpc.queue}")
    public static String CONTACT_RPC_QUEUE;

    @Value("${contact.rpc.key}")
    public static String CONTACT_RPC_KEY;
    
    @Value("${rbac.rpc.default.role.url}")
    public static String RBAC_RPC_DEFAULT_ROLE_URL;
    
    @Value("${CONTACT.rpc.author.url}")
    public static String RBAC_RPC_AUTHOR_URL;
    
    @Value("${user.rpc.exchange}")
    public static String USER_RPC_EXCHANGE;

    @Value("${user.rpc.queue}")
    public static String USER_RPC_QUEUE;

    @Value("${user.rpc.key}")
    public static String USER_RPC_KEY;

    @Value("${user.rpc.authen.url}")
    public static String USER_RPC_AUTHEN_URL;

    @Value("${contact.url}")
    public static String CONTACT_URL;
    
    /** ------------------ */

    @Autowired
    public RabbitMQProperties(@Value("${contact.rpc.exchange}") String rbacRpcExchange,
            @Value("${contact.rpc.queue}") String rbacRpcQueue,
            @Value("${contact.rpc.key}") String rbacRpcKey,
            @Value("${rbac.rpc.default.role.url}") String rbacRpcDefaultRoleUrl,
            @Value("${rbac.rpc.author.url}") String rbacRpcAuthorUrl,
            @Value("${user.rpc.exchange}") String userRpcExchange,
            @Value("${user.rpc.queue}") String userRpcQueue,
            @Value("${user.rpc.key}") String userRpcKey,
                              @Value("${contact.url}") String contactUrl,
            @Value("${user.rpc.authen.url}") String userRpcAuthenUrl) {
        //RBAC
        CONTACT_RPC_EXCHANGE = rbacRpcExchange;
        CONTACT_RPC_QUEUE = rbacRpcQueue;
        CONTACT_RPC_KEY = rbacRpcKey;
        RBAC_RPC_DEFAULT_ROLE_URL = rbacRpcDefaultRoleUrl;
        RBAC_RPC_AUTHOR_URL = rbacRpcAuthorUrl;
        USER_RPC_EXCHANGE = userRpcExchange;
        USER_RPC_QUEUE = userRpcQueue;
        USER_RPC_KEY = userRpcKey;
        USER_RPC_AUTHEN_URL = userRpcAuthenUrl;
        CONTACT_URL=contactUrl;
    }
}
