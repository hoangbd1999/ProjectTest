///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.elcom.abac.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
///**
// *
// * @author Admin
// */
//@Component
//public class ApplicationConfig {
//
//    //DBM Account
//    @Value("${its.core.root.url}")
//    public static String ITS_CORE_ROOT_URL;
//
//    @Value("${its.core.authen.url}")
//    public static String ITS_CORE_AUTHEN_URL;
//
//    @Value("${its.core.username}")
//    public static String ITS_CORE_USERNAME;
//
//    @Value("${its.core.password}")
//    public static String ITS_CORE_PASSWORD;
//
//    @Autowired
//    public ApplicationConfig(@Value("${its.core.root.url}") String itsCoreRootUrl,
//            @Value("${its.core.authen.url}") String itsCoreAuthenUrl,
//            @Value("${its.core.username}") String itsCoreUsername,
//            @Value("${its.core.password}") String itsCorePassword) {
//        //DBM Account
//        ITS_CORE_ROOT_URL = itsCoreRootUrl;
//        ITS_CORE_AUTHEN_URL = itsCoreAuthenUrl;
//        ITS_CORE_USERNAME = itsCoreUsername;
//        ITS_CORE_PASSWORD = itsCorePassword;
//    }
//}
