package com.elcom.metacen.menumanagement;

//import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
//@EnableScheduling
public class MenuManagementServiceApplication {
    
    public static void main(String[] args) {
        // Fix lỗi "UDP failed setting ip_ttl | Method not implemented" khi start app trên Windows
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(MenuManagementServiceApplication.class, args);
//        new SpringApplicationBuilder(MenuManagementServiceApplication.class).web(WebApplicationType.NONE).run(args);
    }
}
