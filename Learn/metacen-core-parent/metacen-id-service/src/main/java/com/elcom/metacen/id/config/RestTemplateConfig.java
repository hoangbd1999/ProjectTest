/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.config;

import com.elcom.metacen.id.interceptor.RequestResponseHandlerInterceptor;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Admin
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RequestResponseHandlerInterceptor requestResponseHandlerInterceptor() {
        return new RequestResponseHandlerInterceptor();
    }
    
    @Bean
    public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory(){
        return new SimpleClientHttpRequestFactory();
    }
    
    @Bean
    public BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory(){
        return new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory());
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(bufferingClientHttpRequestFactory());
        restTemplate.setInterceptors(Collections.singletonList(requestResponseHandlerInterceptor()));
        return restTemplate;
    }
}
