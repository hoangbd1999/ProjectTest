/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Admin
 */
@Configuration
public class ExecutorServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceConfig.class);
    
    @Value("${fixed.thread.pool:1}")
    private int fixedSize;

    @Bean("fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        LOGGER.info("Init {} threads for ExecutorService fixedThreadPool !!!", fixedSize);
        return Executors.newFixedThreadPool(fixedSize);
    }

    @Bean("singleThreaded")
    public ExecutorService singleThreadedExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean("cachedThreadPool")
    public ExecutorService cachedThreadPool() {
        return Executors.newCachedThreadPool();
    }
}
