/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.abac.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class CachingOnStartupConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingOnStartupConfig.class);

//    @Autowired
//    private TokenService tokenService;
//
//    @Autowired
//    private ResourcesService resourcesService;
//
//    @Autowired
//    private MenuService menuService;
//
//    @Autowired
//    private DBMCommonService commonService;
//
//    @Autowired
//    private RoleUserService roleUserService;
//
//    @EventListener(classes = ApplicationStartedEvent.class )
//    public void initCachingOnAppStartup(ApplicationStartedEvent event) {
//        //Remove cache
//        tokenService.removeAccessToken();
//        //Get new token and cache
//        String dbmToken = tokenService.getAccessToken();
//        LOGGER.info("DBM Token on startup: {}", dbmToken);
//        //Resource
//        resourcesService.findAll();
//        //Menu
//        menuService.findAll();
//        //violation_types
//        commonService.findAllViolationTypes();
//        //violation status
//        commonService.findAllViolationInspectStatus();
//        commonService.findAllViolationProcessStatus();
//        //Load all user role
//        List<RoleUser> roleUserList = roleUserService.findAll();
//        if(roleUserList != null && !roleUserList.isEmpty()){
//            roleUserList.forEach((roleUser) -> {
//                roleUserService.findByUuidUser(roleUser.getUuidUser());
//            });
//        }
//    }
}
