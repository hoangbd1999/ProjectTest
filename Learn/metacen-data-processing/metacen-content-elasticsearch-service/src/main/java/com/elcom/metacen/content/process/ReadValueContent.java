package com.elcom.metacen.content.process;
import com.elcom.metacen.content.dto.*;
import com.elcom.metacen.content.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.content.service.UploadFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tomcat.util.json.JSONParser;
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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import scala.util.parsing.json.JSONArray;

@Component
public class ReadValueContent implements FlatMapFunction<List<ContentDTO>, Tuple3<String,Boolean,Object>> {
    private static final Logger logger = LoggerFactory.getLogger(ReadValueContent.class);

    @Autowired
    private UploadFileService uploadFileService;

    @Override
    public void flatMap(List<ContentDTO> value, Collector< Tuple3<String,Boolean,Object>> out) {
        for (ContentDTO tmp: value
             ) {
            String id = tmp.getMediaUuidKey();
            Boolean status = true;
            Object data = new Object();
            try {
                URL url = new URL(tmp.getMediaFileUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                String path = tmp.getMediaFileUrl().substring(tmp.getMediaFileUrl().lastIndexOf("/"));
                InputStream inStream = conn.getInputStream();
                File file = new File("config"+path);
                FileUtils.writeByteArrayToFile(file, conn.getInputStream().readAllBytes());
                String mimeType = Files.probeContentType(file.toPath());
                if(mimeType!=null){
                    if(mimeType.contains("text")){
                        if(mimeType.contains("html")){
                            TextDTO textDTO= new TextDTO();
                            String content = Files.readString(file.toPath(), Charsets.toCharset("UTF-8"));
                            Document doc = Jsoup.parse(content);
                            String text = doc.text();
                            textDTO.setContentUTF8(text);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                        }else {
                            String  dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            TextDTO textDTO= new TextDTO();
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                        }
                    }else if(mimeType.contains("document")){
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
                        TextDTO textDTO= new TextDTO();
                        textDTO.setContentUTF8(extractor.getText());
                        data=textDTO;
                    }else if(mimeType.contains("json")){
//                        String test = "ⴴ֓챁�≯尭㿯驉�닜蟃캵\uF003郎וֹ\u1AD4玢\uF63C\uF05C竣棭갔\u2061뱚য়괅澪⏝䑥㩖贈\uE7AF畀徯쩾✃獛쾸钽䫂삊㱯퍫\uE1FA⛬騒Ƚ谯ᗨ墱अ궐뙣嵲쨢珰\uF7D4ꦶ䦰岱蓨רּ氝䞐ᕥ괢쇃ቱ��녝嬱䬴醍\uF64A耫ᶅ㭴鞰넪揥삹憪㳸ꎶ㈋솭顋竑譳ૃ\uE435쬀㼬뵮緝Ὢ즋ᠵ㚺突䔘꠸\u1776⨤恺슒쳫꒕�\uEE7Eᛧ럎腶滂ᵃ쵑圲뚢蚙効鄚坌ᖥ\uE405ꀈⳀ떲\uE338\uF694罴褦푣歱㴖꾿�ꀳ彣黵\uF4E6\uE32A\uE48F㿁兴嬨뫵ﯜ⤑\uE3C6莩\uE792ᡖ\uF705䆢싘焕칌骿\uEC19뀕葉巶\u2069짣㤾ꔼ\uF4FF\uF44E㯞ᐰ脓\uEE2Dꃟ늕웹賫骧\uE004淽⮎ು勒\uF4B4\uF7C5桅솤쥊ꖍ䷤\uF549\uE87Eந汉\uEEB8蓖Ꮕ爬";
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader)
                                .lines().collect(Collectors.joining(""));
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        TextDTO textDTO= new TextDTO();
                        textDTO.setContentUTF8(dataUTF8);
                        textDTO.setContentUTF8(dataGB18030);
                        data=textDTO;
                    }else {
                        status=false;
                    }
                }else {
                    if(path.endsWith(".eml")){
                        List<AttachmentsDTO> attachmentsDTOS = new ArrayList<>();
                        Email email = EmailConverter.emlToEmail(file);
                        if(email.getAttachments()!=null&&!email.getAttachments().isEmpty()){
                            List<AttachmentResource> attachmentResources = email.getAttachments();
                            for (AttachmentResource attach: attachmentResources
                                 ) {
                                FileUtils.writeByteArrayToFile(new File(attach.getName()), attach.getDataSource().getInputStream().readAllBytes());
                                String urlUpload = uploadFile(new File(attach.getName()));
                                AttachmentsDTO attachmentsDTO = new AttachmentsDTO();
                                attachmentsDTO.setName(attach.getName());
                                attachmentsDTO.setUrl(urlUpload);
                                attachmentsDTOS.add(attachmentsDTO);
                            }
                        }
                        EmailDTO emailDTO = new EmailDTO();
                        emailDTO.setReplyTo(email.getReplyToRecipient());
                        if(email.getHTMLText().contains("<html>")){
                            emailDTO.setContents(email.getPlainText());
                        }else {
                            emailDTO.setContents(email.getHTMLText());
                        }
                        emailDTO.setSubject(email.getSubject());
                        emailDTO.setFrom(email.getFromRecipient());
                        List<Recipient> to = new ArrayList<>();
                        to.addAll(email.getRecipients());
                        emailDTO.setTo(to);
                        emailDTO.setAttachments(attachmentsDTOS);
                        data=emailDTO;
                        status=true;
                    }else if(path.endsWith(".octet-stream")){
                        String content = Files.readString(file.toPath(), Charsets.toCharset("ISO-8859-1"));
                        TextDTO textDTO= new TextDTO();
                        textDTO.setContentUTF8(content);
                        data=textDTO;
                    }else if(path.endsWith(".ocsp-response")){
                        String content = Files.readString(file.toPath(), Charsets.toCharset("ISO-8859-1"));
                        TextDTO textDTO= new TextDTO();
                        textDTO.setContentUTF8(content);
                        data=textDTO;

                    }else if(path.endsWith(".form-data")){
                        String  content = FileUtils.readFileToString(file, "UTF-8");
                        TextDTO textDTO= new TextDTO();
                        textDTO.setContentUTF8(content);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data=textDTO;
                    }else {
                        status=false;
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
                status=false;
            }
            Tuple3<String,Boolean,Object> result = new Tuple3<>(id,status,data);
            out.collect(result);
        }

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
        ResponseEntity<ResponseDto> response = restTemplate. postForEntity(RabbitMQProperties.UPLOAD_URL, requestEntity, ResponseDto.class);
        if (response != null && response.getBody() != null && response.getBody().getData() != null) {
            path.delete();
            return response.getBody().getData().getFileDownloadUri();
        }
        return null;
    }
}
