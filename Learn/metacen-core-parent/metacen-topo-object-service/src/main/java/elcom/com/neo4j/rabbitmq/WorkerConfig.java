/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 *
 * @author admin
 */
@Configuration
public class WorkerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerConfig.class);

    @Value("${link.object.worker.queue}")
    private String workerQueue;

    @Bean("workerQueue")
    public Queue initWorkerQueue() {
        return new Queue(workerQueue);
    }

    @Value("${link.object.worker.queue.updatenode}")
    private String workerQueueUpdate;

    @Value("${link.object.worker.queue.contains}")
    private String workerQueueContains;

    @Value("${link.object.worker.queue.delete.node}")
    private String workerQueueDelete;

    @Bean("initWorkerQueueUpdate")
    public Queue initWorkerQueueUpdate() {
        return new Queue(workerQueueUpdate);
    }
    @Bean("initWorkerQueueContains")
    public Queue initWorkerQueueContains() {
        return new Queue(workerQueueContains);
    }
    @Bean("initworkerQueueDelete")
    public Queue initworkerQueueDelete() {
        return new Queue(workerQueueDelete);
    }

}
