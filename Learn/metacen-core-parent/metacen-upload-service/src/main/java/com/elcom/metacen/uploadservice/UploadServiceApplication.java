package com.elcom.metacen.uploadservice;

import com.elcom.metacen.uploadservice.upload.FileStorageConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({
    FileStorageConfig.class
})
@EnableCaching
@EnableScheduling
public class UploadServiceApplication {

    public static void main(String[] args) {
        // Fix lỗi "UDP failed setting ip_ttl | Method not implemented" khi start app trên Windows
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(UploadServiceApplication.class, args);
    }
}
