package com.elcom.metacen.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan({"com.elcom.metacen"})
@EnableJpaRepositories(basePackages = {"com.elcom.metacen.saga.repository"})
@EntityScan("com.elcom.metacen.saga.model")
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SagaServiceApplication {

    public static void main(String[] args) {
        // Fix lỗi "UDP failed setting ip_ttl | Method not implemented" khi start app trên Windows
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(SagaServiceApplication.class, args);
    }
}
