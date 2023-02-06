package com.elcom.abac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class AbacSericeApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbacSericeApplication.class);

    public static void main(String[] args) {
        // Fix lỗi "UDP failed setting ip_ttl | Method not implemented" khi start app trên Windows
        LOGGER.info(System.getProperty("java.io.tmpdir"));
//        System.out.println(System.getProperty("java.io.tmpdir"));
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(AbacSericeApplication.class, args);
//        new SpringApplicationBuilder(AbacSericeApplication.class).web(WebApplicationType.NONE).run(args);
    }
}
