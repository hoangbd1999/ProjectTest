/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Admin
 */
@Configuration
public class RpcConfig {

    @Value("${topology.rpc.exchange}")
    private String exchange;
    @Value("${topology.rpc.queue}")
    private String queue;
    @Value("${topology.rpc.key}")
    private String key;

    @Bean("rpcQueue")
    public Queue rpcQueue() {
        return new Queue(queue);
    }

    @Bean("rpcExchange")
    public DirectExchange rpcExchange() {
        return new DirectExchange(exchange);
    }

    @Bean("rpcBinding")
    public Binding binding(DirectExchange rpcExchange, Queue rpcQueue) {
        return BindingBuilder.bind(rpcQueue).to(rpcExchange).with(key);
    }

    @Bean
    public Neo4jRpcServer rpcServer() {
        return new Neo4jRpcServer();
    }
}
