/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.messaging.rabbitmq;

import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.utils.DateUtil;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 *
 * @author admin
 */
public class WorkerServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerServer.class);
    
    public WorkerServer() {
    }

    @RabbitListener(queues = "${user.worker.queue}")
    public void workerRecevie(String json) throws InterruptedException, IOException {
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);
            //Process here
            if (request != null) {
                String requestPath = request.getRequestPath();
                String urlParam = request.getUrlParam();
                String pathParam = request.getPathParam();
                Map<String, Object> bodyParam = request.getBodyParam();
                Map<String, String> headerParam = request.getHeaderParam();
                LOGGER.info("requestPath: " + requestPath + ", urlParam: " + urlParam
                        + ", pathParam: " + pathParam);
                if (bodyParam != null && !bodyParam.isEmpty()) {
                    LOGGER.info("bodyParam");
                    Iterator<Map.Entry<String, Object>> iterator = bodyParam.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> entry = iterator.next();
                        LOGGER.info(entry.getKey() + " => " + entry.getValue());
                    }
                }
                if (headerParam != null && !headerParam.isEmpty()) {
                    LOGGER.info("headerParam");
                    Iterator<Map.Entry<String, String>> iterator = headerParam.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> entry = iterator.next();
                        LOGGER.info(entry.getKey() + " => " + entry.getValue());
                    }
                }

                switch (request.getRequestMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        if ("/user/mail".equalsIgnoreCase(requestPath)) {
                            String title = null;
                            String content = null;
                            String emailTo = null;
                            if (bodyParam != null && !bodyParam.isEmpty()) {
                                title = (String) bodyParam.get("title");
                                content = (String) bodyParam.get("content");
                                emailTo = (String) bodyParam.get("emailTo");
                            }
                            //Send mail
                            LOGGER.info("Send email to : " + emailTo + " with title : " + title);
                            //Send mail service
                        }else if ("/v1.0/user/upload".equalsIgnoreCase(requestPath)) {
                            String data = null;
                            if (bodyParam != null && !bodyParam.isEmpty()) {
                                data = (String) bodyParam.get("data");
                            }
                            if (!StringUtil.isNullOrEmpty(data)) {
                                String ddmmyyyy = DateUtil.today("ddMMyyyy");
                                String fileName = UUID.randomUUID().toString();
                                LOGGER.info("InterviewConstant.ROOT_DIR: " + System.getProperty("user.dir") + ", file: " + fileName);
                                createUploadFolderForVideo(ddmmyyyy);

                                String filePath = "upload" + File.separator + ddmmyyyy + File.separator + "video" + File.separator + fileName;
                                String uploadedFileLocation = System.getProperty("user.dir") + File.separator + filePath;

                                String[] strings = data.split(",");
                                byte[] decodedByte = Base64.getMimeDecoder().decode(strings[1]);
                                ByteArrayInputStream bis = new ByteArrayInputStream(decodedByte);
                                String mimeType = strings[0];
                                String fileExtension = null;
                                if(mimeType != null){   
                                    mimeType = mimeType.replace("data:", "").replace(";base64", "");
                                    String delimiter = "[/]";
                                    String[] tokens = mimeType.split(delimiter);
                                    fileExtension = tokens[1];
                                    LOGGER.info("mimeType: " + mimeType + " | fileExtension: " + fileExtension);
                                }
                                if (!StringUtil.isNullOrEmpty(fileExtension)) {
                                    filePath += "." + fileExtension;
                                    uploadedFileLocation += "." + fileExtension;
                                }
                                boolean uploaded = writeToFile(bis, uploadedFileLocation);
                                LOGGER.info("Upload file status: " + uploaded + "| filePath: " + filePath);
                            }
                        }
                        break;
                    case "DELETE":
                        break;
                    default:
                        break;
                }
            }
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    private void createUploadFolderForVideo(String ddmmyyyy) {
        //Create upload dir if not exist
        File uploadDir = new File(System.getProperty("user.dir") + File.separator + "upload");
        if (!uploadDir.exists()) {
            LOGGER.info("Tao folder upload: " + uploadDir.mkdir());
        }

        //Create dir if not exist
        File ddmmyyyyDir = new File(System.getProperty("user.dir") + File.separator + "upload" + File.separator + ddmmyyyy);
        if (!ddmmyyyyDir.exists()) {
            LOGGER.info("Tao folder " + ddmmyyyy + ": " + ddmmyyyyDir.mkdir());
        }

        //Create dir if not exist
        File fileDir = new File(System.getProperty("user.dir") + File.separator + "upload" + File.separator + ddmmyyyy + File.separator + "video");
        if (!fileDir.exists()) {
            LOGGER.info("Tao folder file: " + fileDir.mkdir());
        }
    }

    public boolean writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
        try {
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
