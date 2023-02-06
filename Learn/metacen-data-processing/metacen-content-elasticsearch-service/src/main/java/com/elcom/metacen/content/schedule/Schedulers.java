package com.elcom.metacen.content.schedule;

//import com.elcom.itscore.recognition.flink.clickhouse.service.RecognitionPlateClickHouseService;

import com.elcom.metacen.content.dto.*;
import com.elcom.metacen.content.process.ReadValueTest;
import com.elcom.metacen.content.redis.RedisRepository;
import com.elcom.metacen.content.service.ProcessContentService;
import com.elcom.metacen.content.service.UploadFileService;
import com.elcom.metacen.content.service.impl.ProcessContentServiceImpl;
import com.elcom.metacen.content.service.impl.ProcessService;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.converter.EmailConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

//import com.elcom.itscore.recognition.flink.repository.FlinkReportRepository;
import org.apache.commons.io.IOUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.tuple.*;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.awt.geom.Path2D;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

/**
 *
 * @author anhdv
 */
@Configuration
@Service
public class Schedulers {

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ProcessContentService processContentService;

    @Autowired
    private ProcessService processContents1;

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedulers.class);

//    @Scheduled( fixedDelayString = "1000")
    public void testVoice() throws Exception{

       edu.cmu.sphinx.api.Configuration configuration = new edu.cmu.sphinx.api.Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
        InputStream stream = new FileInputStream(new File("config/test.wav"));

        recognizer.startRecognition(stream);
        SpeechResult result;
        while ((result = recognizer.getResult()) != null) {
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
        }
        recognizer.stopRecognition();
        LOGGER.info("oke");
    }

//    @Scheduled( fixedDelayString = "1000")
    public void testMid() throws Exception {

//        String upload = uploadFileService.uploadFile(new File("config/d.html"));
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
//        String a1 = "http://103.21.151.166:8683/v1.0/upload/export/a1.form-data";
//        String a2 = "http://103.21.151.166:8683/v1.0/upload/export/a2.json";
//        String a3 = "http://103.21.151.166:8683/v1.0/upload/export/a3.ocsp-response";
//        String a4 = "http://103.21.151.166:8683/v1.0/upload/export/a4.octet-stream";
//        String a5 = "http://103.21.151.166:8683/v1.0/upload/export/pom.xml";
//        String a6 = "http://103.21.151.166:8683/v1.0/upload/export/doctest.docx";
//        String a7 = "http://103.21.151.166:8683/v1.0/upload/export/test1.docx";
//        String a8 = "http://103.21.151.166:8683/v1.0/upload/export/a5.txt";
        String a9 = "http://103.21.151.166:8683/v1.0/upload/export/abc.eml";
//        String a10 ="http://192.168.61.106:9683/v1.0/upload/object/file/26102022/c.html";
//        String a11 = "http://192.168.61.106:9683/v1.0/upload/object/file/27102022/d.html";
//        ContentDTO contentDTO1 = new ContentDTO();
//        contentDTO1.setId(UUID.randomUUID().toString());
//        contentDTO1.setUrl(a1);
//        ContentDTO contentDTO2 = new ContentDTO();
//        contentDTO2.setId(UUID.randomUUID().toString());
//        contentDTO2.setUrl(a2);
//        ContentDTO contentDTO3 = new ContentDTO();
//        contentDTO3.setId(UUID.randomUUID().toString());
//        contentDTO3.setUrl(a3);
//        ContentDTO contentDTO4 = new ContentDTO();
//        contentDTO4.setId(UUID.randomUUID().toString());
//        contentDTO4.setUrl(a4);
//        ContentDTO contentDTO5 = new ContentDTO();
//        contentDTO5.setId(UUID.randomUUID().toString());
//        contentDTO5.setUrl(a5);
//        ContentDTO contentDTO6 = new ContentDTO();
//        contentDTO6.setId(UUID.randomUUID().toString());
//        contentDTO6.setUrl(a6);
//        ContentDTO contentDTO7 = new ContentDTO();
//        contentDTO7.setId(UUID.randomUUID().toString());
//        contentDTO7.setUrl(a7);
//        ContentDTO contentDTO8 = new ContentDTO();
//        contentDTO8.setId(UUID.randomUUID().toString());
//        contentDTO8.setUrl(a8);
//        ContentDTO contentDTO9 = new ContentDTO();
//        contentDTO9.setId(UUID.randomUUID().toString());
//        contentDTO9.setUrl(a9);
        ContentDTO contentDTO10 = new ContentDTO();
        contentDTO10.setMediaUuidKey(UUID.randomUUID().toString());
        contentDTO10.setMediaFileUrl(a9);
        List<ContentDTO> contentDTOS = new ArrayList<>();
//        contentDTOS.add(contentDTO1);
//        contentDTOS.add(contentDTO2);
//        contentDTOS.add(contentDTO3);
//        contentDTOS.add(contentDTO4);
//        contentDTOS.add(contentDTO5);
//        contentDTOS.add(contentDTO6);
//        contentDTOS.add(contentDTO7);
//        contentDTOS.add(contentDTO8);
//        contentDTOS.add(contentDTO9);
        contentDTOS.add(contentDTO10);
        Long start = System.currentTimeMillis();
        List<CompletableFuture<ResponseContentsDTO>> listResult = new ArrayList<>();
        for (ContentDTO contentFile : contentDTOS) {
                CompletableFuture<ResponseContentsDTO> result = processContents1.processContent(contentFile);
//                listResult.add(result);
        }
//        CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
//                .thenApply(v -> listResult.stream()
//                        .map(CompletableFuture::join)
//                        .collect(Collectors.toList())
//                );
//        List<ResponseContentsDTO> result1 = new ArrayList<>();
//        for (CompletableFuture<ResponseContentsDTO> result : listResult) {
//            ResponseContentsDTO resultCheckDto1 = result.get();
//            result1.add(resultCheckDto1);
//
//
//        }
        Long end = System.currentTimeMillis();
        LOGGER.info("{}-{} = {}",end,start,end-start);


        LOGGER.info("adasd");
    }

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
