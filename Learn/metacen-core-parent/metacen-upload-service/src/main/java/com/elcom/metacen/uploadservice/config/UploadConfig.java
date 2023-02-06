/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.uploadservice.config;

import com.elcom.metacen.uploadservice.model.dto.UploadDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class UploadConfig {
    //Typesafe config
    public static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config" + File.separator;

    //Constant
    public static List<String> UPLOAD_SERVICE_LIST = new ArrayList<>();
    public static final Map<String, String> UPLOAD_SERVICE_MAP = new HashMap<>();
    public static final Map<String, List<String>> UPLOAD_SERVICE_PATH_MAP = new HashMap<>();
    public static final Map<String, UploadDTO> UPLOAD_DEFINE_MAP = new HashMap<>();
    public static boolean CHECK_CONTENT_BY_MAGIC = false;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            System.out.println("Loading UploadConfig config...");
            Properties properties = new Properties();
            properties.load(new InputStreamReader(new FileInputStream(CONFIG_DIR + "upload.properties"), "UTF-8"));
            Enumeration e = properties.propertyNames();

            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = properties.getProperty(key);
                if (key.equalsIgnoreCase("upload.service")) {
                    String[] arrDomain = value.split(",");
                    UPLOAD_SERVICE_LIST = Arrays.asList(arrDomain);
                } else if(key.equalsIgnoreCase("upload.file")){
                    //Load upload dto json file
                    loadUploadDTOJson(value);
                } else if(key.equalsIgnoreCase("checkby.magic")){
                    //Check content type by magic lib
                    CHECK_CONTENT_BY_MAGIC = Boolean.parseBoolean(value != null ? value.trim() : value);
                } else {
                    UPLOAD_SERVICE_MAP.put(key, value);
                    if (key.contains(".path")) {
                        String[] arrPath = value.split(",");
                        List<String> pathList = Arrays.asList(arrPath);
                        UPLOAD_SERVICE_PATH_MAP.put(key.replace(".path", ""), pathList);
                    }
                }
            }
            System.out.println("Load UploadConfig config successfull!!!");
        } catch (IOException ex) {
            Logger.getLogger(UploadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadUploadDTOJson(String jsonFile) {
        try {
            FileReader reader = new FileReader(CONFIG_DIR + jsonFile);
            ObjectMapper mapper = new ObjectMapper();
            List<UploadDTO> rabbitMqTypeList = mapper.readValue(reader, new TypeReference<List<UploadDTO>>() {
            });
            if (rabbitMqTypeList != null && rabbitMqTypeList.size() > 0) {
                rabbitMqTypeList.forEach((tmp) -> {
                    UPLOAD_DEFINE_MAP.put(tmp.getPath(), tmp);
                });
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UploadConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UploadConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//    public static void main(String[] args) {
//        List<String> userPathList = UPLOAD_SERVICE_PATH_MAP.get("user");
//        userPathList.forEach((path) -> {
//            System.out.println(path);
//        });
//        String requestPath = "/upload/user/avatar";
//        System.out.println("Contain path " + requestPath + " ==> " + userPathList.contains(requestPath));
//        UploadDTO dto = UPLOAD_DEFINE_MAP.get("/upload/user/avatar");
//        System.out.println("Upload define DTO for /upload/user/avatar: " + dto.toJsonString());
//    }
}
