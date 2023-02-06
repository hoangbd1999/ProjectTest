package com.elcom.metacen.enrich.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableCaching
//@EnableElasticsearchRepositories(basePackages = "com.elcom.metacen.enrich.data.repository")
//@EnableScheduling
public class MetaCENEnrichDataServiceApplication {

    public static void main(String[] args) {
        // Fix lỗi "UDP failed setting ip_ttl | Method not implemented" khi start app trên Windows
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(MetaCENEnrichDataServiceApplication.class, args);
        // new SpringApplicationBuilder(VsatAisDataServiceApplication.class).web(WebApplicationType.NONE).run(args);

    }

}
