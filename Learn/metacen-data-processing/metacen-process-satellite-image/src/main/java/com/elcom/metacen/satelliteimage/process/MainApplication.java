package com.elcom.metacen.satelliteimage.process;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
    Xử lý phân tích ảnh vệ tinh ( yêu cầu cắt ra các ảnh đối tượng con bên trong ảnh khu vực )
*/
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
