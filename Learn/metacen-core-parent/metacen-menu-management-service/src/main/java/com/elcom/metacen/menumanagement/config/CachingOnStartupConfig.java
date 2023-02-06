/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.config;

import com.elcom.metacen.menumanagement.model.Menu;
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


    @EventListener(classes = {ApplicationStartedEvent.class, Menu.class})
    public void initCachingOnAppStartup(Object event) {
//        if (event instanceof ApplicationStartedEvent) {
//            //Remove cache
//            tokenService.removeAccessToken();
//            //Get new token and cache
//            String dbmToken = tokenService.getAccessToken();
//            LOGGER.info("DBM Token on startup: {}", dbmToken);
//            //Resource
//            resourcesService.findAll();
//            //Menu
//            menuService.findAll();
//            //violation_types
//            commonService.findAllViolationTypes();
//            //violation status
//            commonService.findAllViolationInspectStatus();
//            commonService.findAllViolationProcessStatus();
//            //Load all user role
//            List<RoleUser> roleUserList = roleUserService.findAll();
//            if (roleUserList != null && !roleUserList.isEmpty()) {
//                roleUserList.forEach((roleUser) -> {
//                    roleUserService.findByUuidUser(roleUser.getUuidUser());
//                });
//            }
//        } else {
//            RoleUser role = (RoleUser) event;
//            LOGGER.info("Receive event reload cache roleUserDetail for user: {}", role.getUuidUser());
//            roleUserService.findByUuidUser(role.getUuidUser());
//        }

    }
}
