/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.config;

import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 *
 * @author Admin
 */
@Configuration
public class CachingConfig extends CachingConfigurerSupport {

    @Bean
    public CacheManager cacheManager() {
        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("resources"),
                new ConcurrentMapCache("menu"),
                new ConcurrentMapCache("menuResources"),
                new ConcurrentMapCache("roleUserDetail"),
                new ConcurrentMapCache("violation_types"),
                new ConcurrentMapCache("violation_inspect_status"),
                new ConcurrentMapCache("violation_process_status"),
                new ConcurrentMapCache("BearerToken"),
                new ConcurrentMapCache("roleUserByListUserId"),
                new ConcurrentMapCache("roleListSearch"),
                new ConcurrentMapCache("roleDetail")
        ));

        return cacheManager;
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params)
                -> target.getClass().getSimpleName()
                + "_" + method.getName()
                + "_" + StringUtils.arrayToDelimitedString(params, "_");
    }
}
