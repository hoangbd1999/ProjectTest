package com.elcom.metacen.notify;

//import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
//@EnableScheduling
public class NotifyServiceApplication {
    
    public static void main(String[] args) {
        // Fix lỗi "UDP failed setting ip_ttl | Method not implemented" khi start app trên Windows
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        SpringApplication.run(NotifyServiceApplication.class, args);
        // new SpringApplicationBuilder(NotifyServiceApplication.class).web(WebApplicationType.NONE).run(args);
    }
}
