/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package com.elcom.dbm.data.elastic.config;
package com.elcom.metacen.content.config;


import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.time.Duration;

/**
 *
 * @author Admin
 */
@Configuration
public class ElasticSearchConfig {

    @Value("${spring.elasticsearch.host:103.21.151.167}")
    private String host;

    @Value("${spring.elasticsearch.port:9200}")
    private int port;

    @Value("${spring.elasticsearch.connectTimeout:60}")
    private int connectTimeout;

    @Value("${spring.elasticsearch.socketTimeout:60}")
    private int socketTimeout;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(host + ":" + port)
                .withConnectTimeout(Duration.ofSeconds(connectTimeout))                            
                .withSocketTimeout(Duration.ofSeconds(socketTimeout))
                .build();
        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(restHighLevelClient());
    }

    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        return restHighLevelClient().getLowLevelClient();
    }
}
