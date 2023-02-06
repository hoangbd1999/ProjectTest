package com.elcom.metacen.content.schedule;
import com.elcom.metacen.content.service.UploadFileService;
import com.elcom.metacen.content.service.impl.ProcessService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

//import com.elcom.itscore.recognition.flink.repository.FlinkReportRepository;
import org.apache.commons.io.IOUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.awt.geom.Path2D;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 * @author anhdv
 */
@Configuration
@Service
public class Schedulers {

    @Value("${linux}")
    private Boolean linux;
    @Value("${dir.extract}")
    private String output;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ProcessService processContents1;

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedulers.class);

    public InputStream getImageFromNetByUrls(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();
            return  inStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }






    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        return scheduler;
    }
}
