/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.uploadservice.controller;

import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.uploadservice.exception.ValidationException;
import com.elcom.metacen.uploadservice.config.PropertiesConfig;
import com.elcom.metacen.uploadservice.config.UploadConfig;
import com.elcom.metacen.uploadservice.constant.Constant;
import com.elcom.metacen.uploadservice.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.uploadservice.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.uploadservice.model.dto.ResponseMessageDTO;
import com.elcom.metacen.uploadservice.model.dto.MetadataFileDTO;
import com.elcom.metacen.uploadservice.model.dto.UploadDTO;
import com.elcom.metacen.uploadservice.service.impl.FileStorageServiceImpl;
import com.elcom.metacen.uploadservice.upload.UploadFileResponse;
import com.elcom.metacen.uploadservice.utils.DateUtil;
import com.elcom.metacen.uploadservice.utils.StringUtil;
import com.elcom.metacen.uploadservice.validation.UploadValidation;
import com.elcom.metacen.utils.JSONConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 * @author Admin
 */
@RestController
@RequestMapping("/v1.0")
public class UploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private FileStorageServiceImpl fileStorageService;

    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Value("${user.authen.use}")
    private String authenUse;

    @Value("${user.authen.http.url}")
    private String authenHttpUrl;

    /**
     * Upload file
     *
     * @param files
     * @param keepFileName
     * @param localUpload
     * @param headerMap
     * @param request
     * @return image upload link
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    @RequestMapping(value = "/upload/**", method = RequestMethod.POST)
    public ResponseEntity<Object> uploadFile(@RequestParam(value = "file", required = false) MultipartFile[] files,
            @RequestParam(value = "keepFileName", required = false) Boolean keepFileName,
            @RequestParam(value = "localUpload", required = false) Boolean localUpload,
            @RequestHeader Map<String, String> headerMap, HttpServletRequest request) throws JsonProcessingException {
        //Request path
        String requestPath = request.getRequestURI();
        if (requestPath != null && requestPath.contains(Constant.API_ROOT_PATH)) {
            requestPath = requestPath.replace(Constant.API_ROOT_PATH, "/");
        }
        //Service
        int index = requestPath.indexOf("/", "/upload/".length());
        String service = null;
        if (index != -1) {
            service = requestPath.substring("/upload/".length(), index);
        } else {
            service = requestPath.replace("/upload/", "");
        }
        LOGGER.info("requestPath: {}, service: {}", requestPath, service);
        UploadDTO dto = UploadConfig.UPLOAD_DEFINE_MAP.get(requestPath);
        //Validation
        new UploadValidation().validate(requestPath, service, files, dto);

        //Authen 
        ResponseMessage response = null;
        if ("http".equalsIgnoreCase(authenUse)) {
            LOGGER.info("Http authen - authorization : {}", headerMap.get("authorization"));
            // Http -> Call api authen
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", headerMap.get("authorization"));
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Dữ liệu đính kèm theo yêu cầu.
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ResponseMessage> result = restTemplate.exchange(authenHttpUrl, HttpMethod.GET, requestEntity, ResponseMessage.class);
            if (result != null && result.getStatusCode() == HttpStatus.OK) {
                response = result.getBody();
            }
            LOGGER.info("Http authen response : {}", response != null ? response.toJsonString() : null);
        } else {
            //Authen -> call rpc authen headerMap
            RequestMessage userRpcRequest = new RequestMessage();
            userRpcRequest.setRequestMethod("POST");
            userRpcRequest.setRequestPath(RabbitMQProperties.USER_RPC_AUTHEN_URL);
            userRpcRequest.setBodyParam(null);
            userRpcRequest.setUrlParam(null);
            userRpcRequest.setHeaderParam(headerMap);
            LOGGER.info("Call RPC authen - authorization: {}", headerMap.get("authorization"));
            LOGGER.info("RequestMessage userRpcRequest : {}", userRpcRequest.toJsonString());
            String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                    RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY, userRpcRequest.toJsonString());
            LOGGER.info("RPC authen response : {}", result);
            if (result != null) {
                ObjectMapper mapper = new ObjectMapper();
                //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //mapper.setDateFormat(df);
                try {
                    response = mapper.readValue(result, ResponseMessage.class);
                } catch (JsonProcessingException ex) {
                    LOGGER.info("Lỗi parse json khi gọi user service verify: {}", ex.toString());
                }
            }
        }

        if ((localUpload != null && localUpload == true) || (response != null && response.getStatus() == HttpStatus.OK.value())) {
            //Process upload
            String ddmmyyyy = DateUtil.today("ddMMyyyy");
            String uploadDir = dto.getFolder();

            List<UploadFileResponse> list = new ArrayList<>();
            UploadFileResponse uploadFileResponse = null;
            for (MultipartFile file : files) {
                String fileName = fileStorageService.storeFile(file, uploadDir, keepFileName);
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(Constant.API_ROOT_PATH + uploadDir.replace("{ddmmyyyy}", ddmmyyyy) + "/")
                        .path(fileName)
                        .toUriString();
                LOGGER.info("Upload file url: " + fileDownloadUri);
                uploadFileResponse = new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
                list.add(uploadFileResponse);
            }
            //if (list != null && list.size() == 1) {
            //    return new ResponseEntity(new ResponseMessageDTO(list), HttpStatus.OK);
            //}
            return new ResponseEntity(new ResponseMessageDTO(list), HttpStatus.OK);
        } else {
            return new ResponseEntity(new ResponseMessageDTO("Token đăng nhập hết hạn"), HttpStatus.FORBIDDEN);
        }
    }

    /**
     * View file upload
     *
     * @param request
     * @return file
     * @throws IOException
     */
    @RequestMapping(value = "/upload/**", method = RequestMethod.GET)
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Resource> viewFile(HttpServletRequest request) throws IOException {
        String filePath = request.getRequestURI();
        String cacheable = request.getParameter("cache");
        LOGGER.info("view file: {}", filePath);
        if (filePath != null && filePath.contains("/v1.0/")) {
            filePath = filePath.replace("/v1.0/", "");
        }
        int lastIndex = filePath != null ? filePath.lastIndexOf("/") : -1;
        String fileName = lastIndex != -1 ? filePath.substring(lastIndex + 1) : filePath;

        // Fallback to the default content type if type could not be determined
        ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(fileName).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(filePath);
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.info("Không nhận dạng được kiểu file.ex: " + ex.toString());
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        if ("false".equalsIgnoreCase(cacheable)) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(resource.contentLength())
                    .headers(headers)
                    .body(resource);
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(resource.contentLength())
                .headers(headers)
                .body(resource);
    }

    /**
     * Response media file Trả file trực tiếp để web dùng
     *
     * @param request
     * @return file
     * @throws IOException
     */
    @RequestMapping(value = "/media/**", method = RequestMethod.GET)
    public ResponseEntity<Resource> responseMedia(HttpServletRequest request) throws IOException {
        String filePath = request.getRequestURI();
        if (filePath == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String localPath = PropertiesConfig.ROOT_FOLDER_FILE_PATH_INTERNAL + "/" + filePath.substring(filePath.indexOf("media/") + 6);
        LOGGER.info("localPath: [{}]", localPath);

        long startTime = System.currentTimeMillis();

        Resource resource;
        try {
            resource = fileStorageService.loadFileAsResource(localPath);
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return ResponseEntity.badRequest().body(null);
        }
        if (resource == null) {
            String errMsg = "{ \"description\": \"Media file is not available!\""
                    + ", \"fileName\": \"" + localPath.substring(localPath.lastIndexOf("/") + 1) + "\" }";
            return ResponseEntity.status(500).body(
                    new ByteArrayResource(errMsg.getBytes())
            );
        }

        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                .filename(resource.getFilename()).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            LOGGER.info("contentType-origin:     {}", contentType);
            if (StringUtil.isNullOrEmpty(contentType)) {
                if (localPath.endsWith(".ts")) {
                    contentType = "video/MP2T";
                } else if (localPath.endsWith(".m3u8")) {
                    contentType = "application/x-mpegURL"; // hoặc application/vnd.apple.mpegURL
                } else if (localPath.endsWith(".eml")) {
                    contentType = "message/rfc822"; // hoặc application/octet-stream
                } else if (localPath.endsWith(".pcap")) {
                    contentType = "application/vnd.tcpdump.pcap";
                } else if (localPath.endsWith(".h264")) {
                    contentType = "video/h264"; // hoặc video/webm;codecs=h264
                } else {
                    contentType = "application/octet-stream";
                    LOGGER.info("set default contentType is 'application/octet-stream'");
                }
                LOGGER.info("contentType-progressed: {}", contentType);
            }
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
            contentType = "application/octet-stream";
        }

        LOGGER.info("Finish all, elapsed time: {}", getElapsedTime(System.currentTimeMillis() - startTime));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                //                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(resource.contentLength())
                .headers(headers)
                .body(resource);
    }

    /**
     * Response satellite media file
     *
     * @param request
     * @return file
     * @throws IOException
     */
    @RequestMapping(value = "/satellite-files/**", method = RequestMethod.GET)
    public ResponseEntity<Resource> responseSatelliteFiles(HttpServletRequest request) throws IOException {
        String filePath = request.getRequestURI();
        if (filePath == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String localPath = PropertiesConfig.SATELLITE_ROOT_FOLDER_INTERNAL + "/" + filePath.substring(filePath.indexOf("satellite-files/") + 16);
        LOGGER.info("localPath: [{}]", localPath);

        long startTime = System.currentTimeMillis();

        Resource resource;
        try {
            resource = fileStorageService.loadFileAsResource(localPath);
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return ResponseEntity.badRequest().body(null);
        }
        if (resource == null) {
            String errMsg = "{ \"description\": \"Media file is not available!\""
                    + ", \"fileName\": \"" + localPath.substring(localPath.lastIndexOf("/") + 1) + "\" }";
            return ResponseEntity.status(500).body(
                    new ByteArrayResource(errMsg.getBytes())
            );
        }

        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                .filename(resource.getFilename()).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            LOGGER.info("contentType-origin:     {}", contentType);
            if (StringUtil.isNullOrEmpty(contentType)) {
                if (localPath.endsWith(".tiff")) {
                    contentType = "image/tiff";
                } else {
                    contentType = "application/octet-stream";
                    LOGGER.info("set default contentType is 'application/octet-stream'");
                }
                LOGGER.info("contentType-progressed: {}", contentType);
            }
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
            contentType = "application/octet-stream";
        }

        LOGGER.info("Finish all, elapsed time: {}", getElapsedTime(System.currentTimeMillis() - startTime));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                //                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(resource.contentLength())
                .headers(headers)
                .body(resource);
    }

    /**
     * Trả metadata file cho client cần download
     *
     * @param requestBody
     * @return file .zip chứa (n) file metadata cần tải
     */
    @RequestMapping(value = "/download-metadata-files", method = RequestMethod.POST, produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> downloadMetadataFiles(@RequestBody(required = true) Map<String, Object> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            return ResponseEntity.badRequest().body((StreamingResponseBody) (OutputStream out) -> {
                throw new ValidationException("PayLoad body is missing!");
            });
        }

        List<MetadataFileDTO> metadataFilesInput;
        try {
            String dataLstStr = JSONConverter.toJSON(requestBody.get("metadataFiles"));
            if (StringUtil.isNullOrEmpty(dataLstStr)) {
                String msgErr = "PayLoad body is invalid!";
                return ResponseEntity.badRequest().body((StreamingResponseBody) (OutputStream out) -> {
                    LOGGER.error(msgErr);
                    throw new ValidationException(msgErr);
                });
            }
            metadataFilesInput = new ObjectMapper().readValue(dataLstStr, new TypeReference<List<MetadataFileDTO>>() {
            });
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return ResponseEntity.badRequest().body((StreamingResponseBody) (OutputStream out) -> {
                throw new ValidationException("PayLoad body is invalid!");
            });
        }

        LOGGER.info("==> metadataFilesInput.size: {}", metadataFilesInput != null && !metadataFilesInput.isEmpty() ? metadataFilesInput.size() : 0);
        if (metadataFilesInput == null || metadataFilesInput.isEmpty()) {
            return ResponseEntity.badRequest().body((StreamingResponseBody) (OutputStream out) -> {
                throw new ValidationException("PayLoad body is invalid!");
            });
        }

        String zipFileName = "metadata-files.zip";
        ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(zipFileName).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity.ok()
                .headers(headers)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(out -> {
                    try (ZipOutputStream zipOutputStream = new ZipOutputStream(out)) {
                        List<File> metadataFilesToZip = new ArrayList<>();
                        // Thêm các file media
                        long startTime = System.currentTimeMillis();
                        for (MetadataFileDTO media : metadataFilesInput) {
                            try {
                                LOGGER.info("mediaFile begin add file {} to zip ...", media.getFilePathLocal());
                                File file = ResourceUtils.getFile(media.getFilePathLocal());
                                if (file.exists()) {
                                    metadataFilesToZip.add(file);
                                }
                            } catch (Exception e) {
                                LOGGER.error("ex: ", e);
                            }
                        }
                        LOGGER.info("Finish add files to zip file, elapsed time: {}",
                                getElapsedTime(System.currentTimeMillis() - startTime));

                        long startTime2 = System.currentTimeMillis();
                        for (File file : metadataFilesToZip) {
                            String fileNameToPutInZip = file.getName();
                            if (file.getParent() != null) {
                                String arr[] = file.getParent().split("/");
                                if (arr != null && arr.length > 3) {
                                    String prefixOfFileName = arr[arr.length - 4] + "_" + arr[arr.length - 3] + "_" + arr[arr.length - 2] + "_" + arr[arr.length - 1];
                                    fileNameToPutInZip = prefixOfFileName + "_" + file.getName();
                                }
                            }

                            zipOutputStream.putNextEntry(new ZipEntry(fileNameToPutInZip));
                            try ( FileInputStream fileInputStream = new FileInputStream(file)) {
                                IOUtils.copy(fileInputStream, zipOutputStream);
                            } catch (Exception e) {
                                LOGGER.error("ex: ", e);
                            }
                            zipOutputStream.closeEntry();
                        }
                        LOGGER.info("Finish generate zip file, elapsed time: {}", getElapsedTime(System.currentTimeMillis() - startTime2));
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                });
    }

    private static String getElapsedTime(long miliseconds) {
        return miliseconds + " (ms)";
    }

}
