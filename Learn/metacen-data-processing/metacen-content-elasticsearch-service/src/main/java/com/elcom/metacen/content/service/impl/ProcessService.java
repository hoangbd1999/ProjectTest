package com.elcom.metacen.content.service.impl;

import com.elcom.metacen.content.dto.*;
import com.elcom.metacen.content.kafka.KafkaClient;
import com.elcom.metacen.content.kafka.KafkaProperties;
import com.elcom.metacen.content.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.content.schedule.Schedulers;
import io.netty.handler.codec.base64.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.converter.EmailConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.crypto.OctetStreamData;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Service
public class ProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);
    @Autowired
    private KafkaClient kafkaClient;

    @Async("processContent")
    public CompletableFuture<ResponseContentsDTO> processContent(ContentDTO tmp) throws ExecutionException, InterruptedException, TimeoutException {
        ResponseContentsDTO result = new ResponseContentsDTO();
        String id = tmp.getMediaUuidKey();
        Boolean status = true;
        Object data = new Object();
        Integer type = 0;
        try {
            URL url = new URL(tmp.getMediaFileUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            String path = tmp.getMediaFileUrl().substring(tmp.getMediaFileUrl().lastIndexOf("/"));
            File file = new File(path);
            FileUtils.writeByteArrayToFile(file, conn.getInputStream().readAllBytes());
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType != null) {
                if (mimeType.contains("text")) {
                    if (mimeType.contains("html")) {
                        TextDTO textDTO = new TextDTO();
                        String content = Files.readString(file.toPath(), Charsets.toCharset("UTF-8"));
                        Document doc = Jsoup.parse(content);
                        String text = doc.text();
                        textDTO.setContentUTF8(text);
//                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
//                        String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
//                        textDTO.setContentGB18030(dataGB18030);
                        data = textDTO;
                    } else {
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        TextDTO textDTO = new TextDTO();
                        textDTO.setContentUTF8(dataUTF8);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                        String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data = textDTO;
                    }
                } else if (mimeType.contains("document")) {
                    FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                    POITextExtractor extractor;
                    if (path.endsWith(".docx")) {
                        XWPFDocument document = new XWPFDocument(fis);
                        extractor = new XWPFWordExtractor(document);
                    } else {
                        // if doc
                        POIFSFileSystem fileSystem = new POIFSFileSystem(fis);
                        extractor = ExtractorFactory.createExtractor(fileSystem);
                    }
                    TextDTO textDTO = new TextDTO();
                    textDTO.setContentUTF8(extractor.getText());
                    data = textDTO;
                } else if (mimeType.contains("json")) {
//                        String test = "ⴴ֓챁�≯尭㿯驉�닜蟃캵\uF003郎וֹ\u1AD4玢\uF63C\uF05C竣棭갔\u2061뱚য়괅澪⏝䑥㩖贈\uE7AF畀徯쩾✃獛쾸钽䫂삊㱯퍫\uE1FA⛬騒Ƚ谯ᗨ墱अ궐뙣嵲쨢珰\uF7D4ꦶ䦰岱蓨רּ氝䞐ᕥ괢쇃ቱ��녝嬱䬴醍\uF64A耫ᶅ㭴鞰넪揥삹憪㳸ꎶ㈋솭顋竑譳ૃ\uE435쬀㼬뵮緝Ὢ즋ᠵ㚺突䔘꠸\u1776⨤恺슒쳫꒕�\uEE7Eᛧ럎腶滂ᵃ쵑圲뚢蚙効鄚坌ᖥ\uE405ꀈⳀ떲\uE338\uF694罴褦푣歱㴖꾿�ꀳ彣黵\uF4E6\uE32A\uE48F㿁兴嬨뫵ﯜ⤑\uE3C6莩\uE792ᡖ\uF705䆢싘焕칌骿\uEC19뀕葉巶\u2069짣㤾ꔼ\uF4FF\uF44E㯞ᐰ脓\uEE2Dꃟ늕웹賫骧\uE004淽⮎ು勒\uF4B4\uF7C5桅솤쥊ꖍ䷤\uF549\uE87Eந汉\uEEB8蓖Ꮕ爬";
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                    String dataGB18030 = new BufferedReader(reader)
                            .lines().collect(Collectors.joining(""));
                    String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                    TextDTO textDTO = new TextDTO();
                    textDTO.setContentUTF8(dataUTF8);
                    textDTO.setContentUTF8(dataGB18030);
                    data = textDTO;
                } else {
                    status = false;
                    data = null;
                }
            } else {
                if (path.endsWith(".eml")) {
                    List<AttachmentsDTO> attachmentsDTOS = new ArrayList<>();
                    Email email = EmailConverter.emlToEmail(file);
                    if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
                        List<AttachmentResource> attachmentResources = email.getAttachments();
                        for (AttachmentResource attach : attachmentResources) {
                            DateFormat timeff = new SimpleDateFormat("HH-mm-ss");
                            DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                            Date now = new Date();
                            String nameUpload = "upload" + date.format(now) + "-" + timeff.format(now) + attach.getName();
                            nameUpload = nameUpload.replaceAll("=", "");
                            nameUpload = nameUpload.replaceAll("\\?", "");
                            FileUtils.writeByteArrayToFile(new File(nameUpload), attach.getDataSource().getInputStream().readAllBytes());
                            String urlUpload = uploadFile(new File(nameUpload));
                            AttachmentsDTO attachmentsDTO = new AttachmentsDTO();
                            attachmentsDTO.setName(attach.getName());
                            attachmentsDTO.setUrl(urlUpload);
                            attachmentsDTOS.add(attachmentsDTO);
                        }
                    }
                    EmailDTO emailDTO = new EmailDTO();
                    emailDTO.setReplyTo(email.getReplyToRecipient());
                    if (email.getHTMLText().contains("<html")) {
                        Document doc = Jsoup.parse(email.getHTMLText());
                        String text = doc.text();
                        emailDTO.setContents(text);
                    } else {
                        emailDTO.setContents(email.getHTMLText());
                    }
                    if (email.getHeaders().get("X-scanvirus") != null) {
                        emailDTO.setScanVirus(email.getHeaders().get("X-scanvirus").toString());
                    }
                    if (email.getHeaders().get("X-scanresult") != null) {
                        emailDTO.setScanResult(email.getHeaders().get("X-scanresult").toString());
                    }
                    if (email.getHeaders().get("User-Agent") != null) {
                        emailDTO.setUserAgent(email.getHeaders().get("User-Agent").toString());
                    }
                    if (email.getHeaders().get("Content-Language") != null) {
                        emailDTO.setContentLanguage(email.getHeaders().get("Content-Language").toString());
                    }
                    if (email.getHeaders().get("X-Mailer") != null) {
                        emailDTO.setXMail(email.getHeaders().get("X-Mailer").toString());
                    }
                    emailDTO.setRaw(email.getHeaders());
                    emailDTO.setSubject(email.getSubject());
                    emailDTO.setFrom(email.getFromRecipient());
                    List<Recipient> to = new ArrayList<>();
                    to.addAll(email.getRecipients());
                    emailDTO.setTo(to);
                    emailDTO.setAttachments(attachmentsDTOS);
                    data = emailDTO;
                    status = true;
                    type = 1;
                } else if (path.endsWith(".octet-stream")) {
                    status = false;
                    data = null;
//                    File file1 = new File("abc.txt");
//                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
//                    FileUtils.writeByteArrayToFile(file1, java.nio.file.Files.readAllBytes(file.toPath()));
//                    String content = Files.readString(file1.toPath(), Charsets.toCharset("ISO-8859-1"));
//                    TextDTO textDTO= new TextDTO();
//                    textDTO.setContentUTF8(content);
//                    data=textDTO;
                } else if (path.endsWith(".ocsp-response")) {
                    status = false;
                    data = null;
//                    String content = Files.readString(file.toPath(), Charsets.toCharset("ISO-8859-1"));
//                    TextDTO textDTO= new TextDTO();
//                    textDTO.setContentUTF8(content);
//                    data=textDTO;

                } else if (path.endsWith(".form-data")) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    TextDTO textDTO = new TextDTO();
                    textDTO.setContentUTF8(content);
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                    String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                    textDTO.setContentGB18030(dataGB18030);
                    data = textDTO;
                } else if (path.endsWith(".x-www-form-urlencoded")) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    TextDTO textDTO = new TextDTO();
                    textDTO.setContentUTF8(content);
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                    String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                    textDTO.setContentGB18030(dataGB18030);
                    data = textDTO;
                } else {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    TextDTO textDTO = new TextDTO();
                    textDTO.setContentUTF8(content);
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                    String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                    textDTO.setContentGB18030(dataGB18030);
                    data = textDTO;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
            data = null;
        }

        result.setUuidKey(tmp.getUuidKey());
        result.setMediaUuidKey(id);
        result.setMediaTypeId(tmp.getMediaTypeId());
        result.setMediaTypeName(tmp.getMediaTypeName());
        result.setSourceId(tmp.getSourceId());
        result.setSourceName(tmp.getSourceName());
        result.setSourceIp(tmp.getSourceIp());
        result.setSourcePort(tmp.getSourcePort());
        result.setDestId(tmp.getDestId());
        result.setDestName(tmp.getDestName());
        result.setDestIp(tmp.getDestIp());
        result.setDestPort(tmp.getDestPort());
        result.setFilePath(tmp.getFilePath());
        result.setMediaFileUrl(tmp.getMediaFileUrl());
        result.setFileType(tmp.getFileType());
        result.setFileSize(tmp.getFileSize());
        result.setDataSourceId(tmp.getDataSourceId());
        result.setDataSourceName(tmp.getDataSourceName());
        result.setDirection(tmp.getDirection());
        result.setDataVendor(tmp.getDataVendor());
        result.setAnalyzedEngine(tmp.getAnalyzedEngine());
        result.setProcessType(tmp.getProcessType());
        result.setEventTime(tmp.getEventTime());
        result.setStatus(status);
        result.setType(type);
        result.setData(data);

        sendReplyTopic(result);
        return CompletableFuture.completedFuture(result);
    }

    private String uploadFile(File path) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(path));
        body.add("keepFileName", true);
        body.add("localUpload ", true);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ResponseDto> response = restTemplate.postForEntity(RabbitMQProperties.UPLOAD_URL, requestEntity, ResponseDto.class);
        if (response != null && response.getBody() != null && response.getBody().getData() != null) {
            path.delete();
            return response.getBody().getData().getFileDownloadUri();
        }
        return null;
    }

    void sendReplyTopic(ResponseContentsDTO responseContentsDTO) {
        long start = System.currentTimeMillis();
        long end = start;
        kafkaClient.callKafkaServerWorker(KafkaProperties.ELASTIC_TOPIC_REQUEST, responseContentsDTO.toJsonString());
        end = System.currentTimeMillis();
//        LOGGER.info("Reply content - Push to {} msg: {} - total: {} ms => {}",
//                KafkaProperties.CONTENT_REPLY_TOPIC, responseContentsDTO.toJsonString(),
//                (end - start), true);
    }
}
