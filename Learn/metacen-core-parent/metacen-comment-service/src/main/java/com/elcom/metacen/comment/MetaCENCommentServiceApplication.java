package com.elcom.metacen.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
//@EnableScheduling
public class MetaCENCommentServiceApplication {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(MetaCENCommentServiceApplication.class, args);

    }

}
