/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.saga.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class CachingOnStartupConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingOnStartupConfig.class);

    @EventListener(classes = ApplicationStartedEvent.class)
    public void initCachingOnAppStartup(ApplicationStartedEvent event) {
        LOGGER.info("Start up ");

    }
}
