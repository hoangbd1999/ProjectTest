package com.elcom.metacen.group.detect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@ComponentScan("com.elcom.metacen")
public class MetacenGroupDetectApplication {
    public static void main(String[] args) {
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(MetacenGroupDetectApplication.class, args);
    }
}
