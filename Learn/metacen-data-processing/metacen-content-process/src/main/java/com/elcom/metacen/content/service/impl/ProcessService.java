package com.elcom.metacen.content.service.impl;

import com.aspose.email.MailAddress;
import com.aspose.email.MailMessage;
import com.elcom.metacen.content.dto.*;
import com.elcom.metacen.content.enums.RecipientType;
import com.elcom.metacen.content.kafka.KafkaClient;
import com.elcom.metacen.content.kafka.KafkaProperties;
import com.elcom.metacen.content.schedule.ExtractExample;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//import org.pcap4j.core.NotOpenException;
//import org.pcap4j.core.PcapHandle;
//import org.pcap4j.core.PcapNativeException;
//import org.pcap4j.core.Pcaps;
//import org.pcap4j.packet.Packet;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.converter.internal.mimemessage.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Service
public class ProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);
    @Autowired
    private KafkaClient kafkaClient;
    @Value("${linux}")
    private Boolean linux;
    @Value("${dir.extract}")
    private String output;

    @Value("${upload.url}")
    private String upload;

//    @Value("${url.service}")
//    private String folderProcess;

    @Autowired
    private ProcessContentFile processContent;

    @Async("processContent")
    public CompletableFuture<ResponseContentsDTO> processContent(ContentDTO tmp) throws ExecutionException, InterruptedException, TimeoutException {
        ResponseContentsDTO result = new ResponseContentsDTO();
        String id = tmp.getMediaUuidKey();
        Boolean status = true;
        Object data = new Object();
        String fileType = ".exe,.msi,.java,.png,.jpeg,.exe,.msi,.java,.rut,.ts,.png,.jpg,.mp4,.mp3";
        Integer type = 0;
        File file = null;
//        LOGGER.info("giai ma ");
        if (fileType.contains(tmp.getFileType())) {
            status = false;
            data = null;
        } else {
            try {
                URL url = new URL(tmp.getMediaFileUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                String path = tmp.getMediaFileUrl().substring(tmp.getMediaFileUrl().lastIndexOf("/"));
                path = "config" + path;
                file= new File(path);
//                LOGGER.info("path {}", file.getAbsolutePath());
                FileUtils.writeByteArrayToFile(file, conn.getInputStream().readAllBytes());
                String mimeType = Files.probeContentType(file.toPath());
                if (mimeType != null) {
//                    LOGGER.info("mineType {} ", mimeType);
                    if (mimeType.contains("text")) {
                        if (mimeType.contains("html")) {
                            TextDTO textDTO = new TextDTO();
                            try {
                                String content = Files.readString(file.toPath());
                                Document doc = Jsoup.parse(content);
                                String text = doc.text();
                                text = text.replaceAll("<(img.*?)>", " ");
                                textDTO.setContentUTF8(text);
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                                doc = Jsoup.parse(dataGB18030);
                                text = doc.text();
                                text = text.replaceAll("<(img.*?)>", " ");
                                textDTO.setContentGB18030(text);
                                data = textDTO;
                                reader.close();
                            } catch (Exception e) {
                                try {
                                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                    String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                                    Document doc = Jsoup.parse(dataGB18030);
                                    String text = doc.text();
                                    text = text.replaceAll("<(img.*?)>", " ");
                                    textDTO.setContentGB18030(text);
                                    data = textDTO;
                                    reader.close();
                                } catch (Exception ex) {
                                    if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                        status = false;
                                        data = null;
                                    }
                                }

                            }


                        } else {
                            TextDTO textDTO = new TextDTO();
                            try {
                                String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                                textDTO.setContentUTF8(dataUTF8);
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                reader.close();
                                data = textDTO;
                            } catch (Exception e) {
                                try {
                                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                    String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                                    textDTO.setContentGB18030(dataGB18030);
                                    reader.close();
                                    data = textDTO;
                                } catch (Exception ex) {
                                    if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                        status = false;
                                        data = null;
                                    }
                                }

                            }


                        }
                    } else if (mimeType.contains("sheet") || mimeType.contains("ms-excel")) {
//                        LOGGER.info("loi sheet");
                        String text = convertExcelToText(file);
                        TextDTO textDTO = new TextDTO();
                        textDTO.setContentUTF8(text);
                        textDTO.setContentGB18030(text);
                        data = textDTO;
                    } else if (mimeType.contains(".presentation")) {
//                        LOGGER.info("loi ptt");
                        String textTest = convertPPTtoText(file.getAbsolutePath());
                        TextDTO textDTO = new TextDTO();
                        textDTO.setContentUTF8(textTest);
                        textDTO.setContentGB18030(textTest);
                        data = textDTO;
                    } else if (mimeType.contains("powerpoint")) {
//                        LOGGER.info("loi ptt");
                        String textTest = convertPPTtoText(file.getAbsolutePath());
                        TextDTO textDTO = new TextDTO();
                        textDTO.setContentUTF8(textTest);
                        textDTO.setContentGB18030(textTest);
                        data = textDTO;
                    } else if (mimeType.contains("document") || mimeType.contains("word")) {

//                        LOGGER.info("loi doc");
                        TextDTO textDTO = new TextDTO();
                        FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                        POITextExtractor extractor;
                        if (file.getAbsolutePath().endsWith(".docx")) {
                            try {
                                XWPFDocument document = new XWPFDocument(fis);
                                extractor = new XWPFWordExtractor(document);
                                textDTO.setContentUTF8(extractor.getText());
                                try {
                                    document.close();
                                    extractor.close();
                                    fis.close();
                                }catch (Exception exx){
                                    exx.printStackTrace();
                                }
                            } catch (Exception ex1) {
                                OPCPackage fs = OPCPackage.open(file);
                                XWPFDocument document = new XWPFDocument(fs);
                                extractor = new XWPFWordExtractor(document);
                                textDTO.setContentUTF8(extractor.getText());
                                try {
                                    document.close();
                                    extractor.close();
                                    fis.close();
                                    fs.close();
                                }catch (Exception exx){
                                    exx.printStackTrace();
                                }
                            }
                        } else {
                            // if doc
                            try {
                                HWPFDocument document = new HWPFDocument(fis);
                                String text = document.getDocumentText();
                                textDTO.setContentUTF8(text);
                                document.close();
                                fis.close();
                            } catch (Exception ex) {
                                try {
                                    XWPFDocument document = new XWPFDocument(fis);
                                    extractor = new XWPFWordExtractor(document);
                                    textDTO.setContentUTF8(extractor.getText());
                                    document.close();
                                    extractor.close();
                                    fis.close();
                                } catch (Exception ex1) {
                                    OPCPackage fs = OPCPackage.open(file);
                                    XWPFDocument document = new XWPFDocument(fs);
                                    extractor = new XWPFWordExtractor(document);
                                    textDTO.setContentUTF8(extractor.getText());
                                    try {
                                        document.close();
                                        extractor.close();
                                        fis.close();
                                        fs.close();
                                    }catch (Exception exx){
                                        exx.printStackTrace();
                                    }

                                }

                            }
                        }
                        textDTO.setContentGB18030(textDTO.getContentUTF8());
                        data = textDTO;
                    } else if (mimeType.contains("pdf")) {
                        LOGGER.info("loi pdf");
                        PDDocument document = PDDocument.load(file);
                        String text = "";
                        if (!document.isEncrypted()) {
                            PDFTextStripper stripper = new PDFTextStripper();
                            text = stripper.getText(document);
                        } else {
                            PDFTextStripper stripper = new PDFTextStripper();
                            text = stripper.getText(document);
                        }
                        document.close();
                        TextDTO textDTO = new TextDTO();
                        textDTO.setContentUTF8(text);
                        textDTO.setContentGB18030(text);
                        data = textDTO;
                    } else if (mimeType.contains("json")) {
                        TextDTO textDTO = new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data = textDTO;
                            reader.close();
                        } catch (Exception e) {
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data = textDTO;
                                reader.close();
                            } catch (Exception ex) {
                                if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                    status = false;
                                    data = null;
                                }
                            }

                        }
                    } else if (path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".tar") || path.endsWith(".tar.gz")) {
//                        LOGGER.info("file zip");
                        List<TextDTO> textDTOList = convertZipToText(file);
                        if (textDTOList != null && !textDTOList.isEmpty()) {
                            TextDTO textDTO = new TextDTO();
                            StringBuilder utf8 = new StringBuilder();
                            StringBuilder tq = new StringBuilder();
                            for (TextDTO tmp1 : textDTOList
                            ) {
                                if (tmp1.getContentUTF8() != null) {
                                    utf8.append(" \n").append(tmp1.getContentUTF8());
                                }
                                if (tmp1.getContentGB18030() != null) {
                                    tq.append(" \n").append(tmp1.getContentGB18030());
                                }

                            }
                            textDTO.setContentUTF8(utf8.toString());
                            textDTO.setContentGB18030(tq.toString());
                            data = textDTO;
                        } else {
//                            LOGGER.info("test loi zip");
                            status = false;
                            data = null;
                        }

                    } else if (path.endsWith(".eml")) {
                        try {
                            List<AttachmentsDTO> attachmentsDTOS = new ArrayList<>();
                            Email email = EmailConverter.emlToEmail(file);
                            if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
                                List<AttachmentResource> attachmentResources = email.getAttachments();
                                for (AttachmentResource attach : attachmentResources
                                ) {
                                    DateFormat timeff = new SimpleDateFormat("HH-mm-ss");
                                    DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                                    Date now = new Date();
                                    String nameUpload = "upload" + date.format(now) + "-" + timeff.format(now) + attach.getDataSource().getName();
                                    nameUpload = nameUpload.replaceAll("=", "");
                                    nameUpload = nameUpload.replaceAll("\\?", "");
                                    File attachmentFile = new File(nameUpload);
                                    FileUtils.writeByteArrayToFile(attachmentFile, attach.getDataSource().getInputStream().readAllBytes());
                                    String urlUpload = uploadFile(attachmentFile);
                                    AttachmentsDTO attachmentsDTO = new AttachmentsDTO();
                                    attachmentsDTO.setName(attach.getDataSource().getName());
                                    attachmentsDTO.setUrl(urlUpload);
                                    List<TextDTO> textDTOList = new ArrayList<>();
                                    if (nameUpload.endsWith(".zip") || nameUpload.endsWith(".rar") || nameUpload.endsWith(".tar") || nameUpload.endsWith(".tar.gz")) {
                                        textDTOList = convertZipToText(attachmentFile);
                                    } else {
                                        textDTOList = convertFileAttachmentToText(attachmentFile);
                                    }
                                    if (textDTOList != null && !textDTOList.isEmpty()) {
                                        attachmentsDTO.setContents(textDTOList);
                                    }
                                    attachmentsDTOS.add(attachmentsDTO);
                                    attachmentFile.exists();
                                    attachmentFile.delete();
                                }
                            }
                            EmailDTO emailDTO = new EmailDTO();
                            emailDTO.setReplyTo(email.getReplyToRecipient());

                            if (email.getHTMLText() != null && email.getHTMLText().contains("<div")) {
                                Document doc = Jsoup.parse(email.getHTMLText());
                                String text = doc.text();
                                emailDTO.setContents(text);
                            } else if (email.getHTMLText() != null) {
                                Document doc = Jsoup.parse(email.getHTMLText());
                                String text = doc.text();
                                emailDTO.setContents(text);
//                        emailDTO.setContents(email.getHTMLText());
                            } else {
                                emailDTO.setContents(email.getPlainText());
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
                        } catch (Exception ex) {
                            List<AttachmentsDTO> attachmentsDTOS = new ArrayList<>();
                            EmailDTO emailDTO = new EmailDTO();
                            MailMessage message = MailMessage.load(file.getAbsolutePath());
                            MailAddress from = message.getFrom();
                            RecipientDTO recipientFrom = new RecipientDTO();
                            recipientFrom.setName(from.getDisplayName());
                            recipientFrom.setAddress(from.getAddress());
                            List<RecipientDTO> recipientTo = new ArrayList<>();
                            if (message.getTo() != null) {
                                message.getTo().forEach((s) -> {
                                    RecipientDTO tmp1 = new RecipientDTO();
                                    tmp1.setName(s.getDisplayName());
                                    tmp1.setAddress(s.getAddress());
                                    tmp1.setType(RecipientType.TO);
                                    recipientTo.add(tmp1);
                                });
                            }
                            if (message.getCC() != null) {
                                message.getCC().forEach((s) -> {
                                    RecipientDTO tmp1 = new RecipientDTO();
                                    tmp1.setName(s.getDisplayName());
                                    tmp1.setAddress(s.getAddress());
                                    tmp1.setType(RecipientType.CC);
                                    recipientTo.add(tmp1);
                                });
                            }
                            if (message.getBcc() != null) {
                                message.getBcc().forEach((s) -> {
                                    RecipientDTO tmp1 = new RecipientDTO();
                                    tmp1.setName(s.getDisplayName());
                                    tmp1.setAddress(s.getAddress());
                                    tmp1.setType(RecipientType.CC);
                                    recipientTo.add(tmp1);
                                });
                            }
                            emailDTO.setFromV1(recipientFrom);
                            emailDTO.setToList(recipientTo);
                            String subject = message.getSubject();
                            subject = subject.replaceAll("\\(Aspose.Email Evaluation\\)", "");
                            emailDTO.setSubject(subject);
                            if (message.getAttachments() != null) {
                                message.getAttachments().forEach((attachment -> {
                                    DateFormat timeff = new SimpleDateFormat("HH-mm-ss");
                                    DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                                    Date now = new Date();
                                    String nameUpload = "upload" + date.format(now) + "-" + timeff.format(now) + attachment.getName();
                                    nameUpload = nameUpload.replaceAll("=", "");
                                    nameUpload = nameUpload.replaceAll("\\?", "");
                                    File attachmentFile = new File(nameUpload);
                                    try {
                                        FileUtils.writeByteArrayToFile(attachmentFile, attachment.getContentStream().readAllBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String urlUpload = null;
                                    try {
                                        urlUpload = uploadFile(attachmentFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    AttachmentsDTO attachmentsDTO = new AttachmentsDTO();
                                    attachmentsDTO.setName(attachment.getName());
                                    attachmentsDTO.setUrl(urlUpload);
                                    List<TextDTO> textDTOList = new ArrayList<>();
                                    if (nameUpload.endsWith(".zip") || nameUpload.endsWith(".rar") || nameUpload.endsWith(".tar") || nameUpload.endsWith(".tar.gz")) {
                                        try {
                                            textDTOList = convertZipToText(attachmentFile);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            textDTOList = convertFileAttachmentToText(attachmentFile);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                    if (textDTOList != null && !textDTOList.isEmpty()) {
                                        attachmentsDTO.setContents(textDTOList);
                                    }
                                    attachmentsDTOS.add(attachmentsDTO);
                                }));
                            }
                            emailDTO.setAttachments(attachmentsDTOS);
                            data = emailDTO;
                            status = true;
                            type = 1;

                        }
                    } else if (path.endsWith(".octet-stream")) {
//                        LOGGER.info(" octet");
                        status = false;
                        data = null;
                    } else if (path.endsWith(".ocsp-response")) {
//                        LOGGER.info(" ocsp");
                        status = false;
                        data = null;

                    } else if (path.endsWith(".form-data")) {
                        TextDTO textDTO = new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data = textDTO;
                            reader.close();
                        } catch (Exception e) {
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data = textDTO;
                                reader.close();
                            } catch (Exception ex) {
                                if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                    status = false;
                                    data = null;
                                }
                            }

                        }
                    } else if (path.endsWith(".x-www-form-urlencoded")) {
                        TextDTO textDTO = new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data = textDTO;
                            reader.close();
                        } catch (Exception e) {
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data = textDTO;
                                reader.close();
                            } catch (Exception ex) {
                                if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                    status = false;
                                    data = null;
                                }
                            }

                        }
                    } else if (path.endsWith(".pcap")) {
                        String pcap = convertPcapToText(file.getAbsolutePath());
                        TextDTO textDTO = new TextDTO();
                        textDTO.setContentUTF8(pcap);
                        textDTO.setContentGB18030(pcap);
//                        LOGGER.info(" pcap");
                    } else if (path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".tar") || path.endsWith(".tar.gz")) {
//                        LOGGER.info("file");
                        List<TextDTO> textDTOList = convertZipToText(file);
                        if (textDTOList != null && !textDTOList.isEmpty()) {
                            TextDTO textDTO = new TextDTO();
                            StringBuilder utf8 = new StringBuilder();
                            StringBuilder tq = new StringBuilder();
                            for (TextDTO tmp1 : textDTOList
                            ) {
                                if (tmp1.getContentUTF8() != null) {
                                    utf8.append(" \n").append(tmp1.getContentUTF8());
                                }
                                if (tmp1.getContentGB18030() != null) {
                                    tq.append(" \n").append(tmp1.getContentGB18030());
                                }

                            }
                            textDTO.setContentUTF8(utf8.toString());
                            textDTO.setContentGB18030(tq.toString());
                            data = textDTO;
                            data = textDTO;
                        } else {
//                            LOGGER.info("zip");
                            status = false;
                            data = null;
                        }

                    } else {
                        TextDTO textDTO = new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data = textDTO;
                            reader.close();
                        } catch (Exception e) {
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data = textDTO;
                                reader.close();
                            } catch (Exception ex) {
                                if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                    data = null;
                                }
                            }

                        }
                    }
                } else {
                    if (path.endsWith(".eml")) {
                        try {
                            List<AttachmentsDTO> attachmentsDTOS = new ArrayList<>();
                            Email email = EmailConverter.emlToEmail(file);
                            if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
                                List<AttachmentResource> attachmentResources = email.getAttachments();
                                for (AttachmentResource attach : attachmentResources
                                ) {
                                    DateFormat timeff = new SimpleDateFormat("HH-mm-ss");
                                    DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                                    Date now = new Date();
                                    String nameUpload = "upload" + date.format(now) + "-" + timeff.format(now) + attach.getDataSource().getName();
                                    nameUpload = nameUpload.replaceAll("=", "");
                                    nameUpload = nameUpload.replaceAll("\\?", "");
                                    File attachmentFile = new File(nameUpload);
                                    FileUtils.writeByteArrayToFile(attachmentFile, attach.getDataSource().getInputStream().readAllBytes());
                                    String urlUpload = uploadFile(attachmentFile);
                                    AttachmentsDTO attachmentsDTO = new AttachmentsDTO();
                                    attachmentsDTO.setName(attach.getDataSource().getName());
                                    attachmentsDTO.setUrl(urlUpload);
                                    List<TextDTO> textDTOList = new ArrayList<>();
                                    if (nameUpload.endsWith(".zip") || nameUpload.endsWith(".rar") || nameUpload.endsWith(".tar") || nameUpload.endsWith(".tar.gz")) {
                                        textDTOList = convertZipToText(attachmentFile);
                                    } else {
                                        textDTOList = convertFileAttachmentToText(attachmentFile);
                                    }
                                    if (textDTOList != null && !textDTOList.isEmpty()) {
                                        attachmentsDTO.setContents(textDTOList);
                                    }
                                    attachmentsDTOS.add(attachmentsDTO);
                                    attachmentFile.exists();
                                    attachmentFile.delete();
                                }
                            }
                            EmailDTO emailDTO = new EmailDTO();
                            emailDTO.setReplyTo(email.getReplyToRecipient());

                            if (email.getHTMLText() != null && email.getHTMLText().contains("<div")) {
                                Document doc = Jsoup.parse(email.getHTMLText());
                                String text = doc.text();
                                emailDTO.setContents(text);
                            } else if (email.getHTMLText() != null) {
                                Document doc = Jsoup.parse(email.getHTMLText());
                                String text = doc.text();
                                emailDTO.setContents(text);
//                        emailDTO.setContents(email.getHTMLText());
                            } else {
                                emailDTO.setContents(email.getPlainText());
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
                        } catch (Exception ex) {
                            List<AttachmentsDTO> attachmentsDTOS = new ArrayList<>();
                            EmailDTO emailDTO = new EmailDTO();
                            MailMessage message = MailMessage.load(file.getAbsolutePath());
                            MailAddress from = message.getFrom();
                            RecipientDTO recipientFrom = new RecipientDTO();
                            recipientFrom.setName(from.getDisplayName());
                            recipientFrom.setAddress(from.getAddress());
                            List<RecipientDTO> recipientTo = new ArrayList<>();
                            if (message.getTo() != null) {
                                message.getTo().forEach((s) -> {
                                    RecipientDTO tmp1 = new RecipientDTO();
                                    tmp1.setName(s.getDisplayName());
                                    tmp1.setAddress(s.getAddress());
                                    tmp1.setType(RecipientType.TO);
                                    recipientTo.add(tmp1);
                                });
                            }
                            if (message.getCC() != null) {
                                message.getCC().forEach((s) -> {
                                    RecipientDTO tmp1 = new RecipientDTO();
                                    tmp1.setName(s.getDisplayName());
                                    tmp1.setAddress(s.getAddress());
                                    tmp1.setType(RecipientType.CC);
                                    recipientTo.add(tmp1);
                                });
                            }
                            if (message.getBcc() != null) {
                                message.getBcc().forEach((s) -> {
                                    RecipientDTO tmp1 = new RecipientDTO();
                                    tmp1.setName(s.getDisplayName());
                                    tmp1.setAddress(s.getAddress());
                                    tmp1.setType(RecipientType.CC);
                                    recipientTo.add(tmp1);
                                });
                            }
                            emailDTO.setFromV1(recipientFrom);
                            emailDTO.setToList(recipientTo);
                            String subject = message.getSubject();
                            subject = subject.replaceAll("\\(Aspose.Email Evaluation\\)", "");
                            emailDTO.setSubject(subject);
                            if (message.getAttachments() != null) {
                                message.getAttachments().forEach((attachment -> {
                                    DateFormat timeff = new SimpleDateFormat("HH-mm-ss");
                                    DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                                    Date now = new Date();
                                    String nameUpload = "upload" + date.format(now) + "-" + timeff.format(now) + attachment.getName();
                                    nameUpload = nameUpload.replaceAll("=", "");
                                    nameUpload = nameUpload.replaceAll("\\?", "");
                                    File attachmentFile = new File(nameUpload);
                                    try {
                                        FileUtils.writeByteArrayToFile(attachmentFile, attachment.getContentStream().readAllBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String urlUpload = null;
                                    try {
                                        urlUpload = uploadFile(attachmentFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    AttachmentsDTO attachmentsDTO = new AttachmentsDTO();
                                    attachmentsDTO.setName(attachment.getName());
                                    attachmentsDTO.setUrl(urlUpload);
                                    List<TextDTO> textDTOList = new ArrayList<>();
                                    if (nameUpload.endsWith(".zip") || nameUpload.endsWith(".rar") || nameUpload.endsWith(".tar") || nameUpload.endsWith(".tar.gz")) {
                                        try {
                                            textDTOList = convertZipToText(attachmentFile);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            textDTOList = convertFileAttachmentToText(attachmentFile);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                    if (textDTOList != null && !textDTOList.isEmpty()) {
                                        attachmentsDTO.setContents(textDTOList);
                                    }
                                    attachmentsDTOS.add(attachmentsDTO);
                                }));
                            }
                            emailDTO.setAttachments(attachmentsDTOS);
                            data = emailDTO;
                            status = true;
                            type = 1;

                        }


                    } else if (path.endsWith(".octet-stream")) {
//                        LOGGER.info(" octet");
                        status = false;
                        data = null;
                    } else if (path.endsWith(".ocsp-response")) {
//                        LOGGER.info(" ocsp");
                        status = false;
                        data = null;

                    } else if (path.endsWith(".form-data")) {
                        TextDTO textDTO = new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data = textDTO;
                        } catch (Exception e) {
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data = textDTO;
                            } catch (Exception ex) {
                                if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                    status = false;
                                    data = null;
                                }
                            }

                        }
                    } else if (path.endsWith(".x-www-form-urlencoded")) {
                        TextDTO textDTO = new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data = textDTO;
                        } catch (Exception e) {
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data = textDTO;
                            } catch (Exception ex) {
                                if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                    status = false;
                                    data = null;
                                }
                            }

                        }
                    } else if (path.endsWith(".pcap")) {
                        String pcap = convertPcapToText(file.getAbsolutePath());
                        TextDTO textDTO = new TextDTO();
                        textDTO.setContentUTF8(pcap);
                        textDTO.setContentGB18030(pcap);
                        data=textDTO;
                        LOGGER.info(" pcap");
                    } else if (path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".tar") || path.endsWith(".tar.gz")) {
                        LOGGER.info("file");
                        List<TextDTO> textDTOList = convertZipToText(file);
                        if (textDTOList != null && !textDTOList.isEmpty()) {
                            TextDTO textDTO = new TextDTO();
                            ObjectMapper objectMapper = new ObjectMapper();
                            StringBuilder utf8 = new StringBuilder();
                            StringBuilder tq = new StringBuilder();
                            for (TextDTO tmp1 : textDTOList
                            ) {
                                if (tmp1.getContentUTF8() != null) {
                                    utf8.append(" \n").append(tmp1.getContentUTF8());
                                }
                                if (tmp1.getContentGB18030() != null) {
                                    tq.append(" \n").append(tmp1.getContentGB18030());
                                }

                            }
                            textDTO.setContentUTF8(utf8.toString());
                            textDTO.setContentGB18030(tq.toString());
                            data = textDTO;
                        } else {
                            LOGGER.info("zip");
                            status = false;
                            data = null;
                        }

                    } else {
                        TextDTO textDTO = new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data = textDTO;
                        } catch (Exception e) {
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data = textDTO;
                            } catch (Exception ex) {
                                if (textDTO.getContentGB18030() == null && textDTO.getContentUTF8() == null) {
                                    status = false;
                                    data = null;
                                }
                            }

                        }
                    }
                }
                file.delete();
            } catch (Exception ex) {
                if(file!=null){
                    file.delete();
                }
//                LOGGER.info("Loi gi do {}", ex.getMessage());
                ex.printStackTrace();
                status = false;
                data = null;
            }
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
        result.setRetryNum(tmp.getRetryNum());
        result.setStatus(status);
        result.setType(type);
        result.setData(data);
        result.setProcessStatus(tmp.getProcessStatus());

        sendReplyTopic(result);
        return CompletableFuture.completedFuture(result);
    }

    //    private String testPcap2(String path) throws PcapNativeException ,Exception{
//            PcapHandle handle;
//            try {
//                handle = Pcaps.openOffline(path, PcapHandle.TimestampPrecision.NANO);
//            } catch (Exception ex) {
//                handle = Pcaps.openOffline(path);
//            }
//
//
//            for (int i = 0; i<5; i++) {
//                try {
//                    org.pcap4j.packet.Packet packet = handle.getNextPacketEx();
//                    System.out.println(handle.getTimestamp());
//                    System.out.println(packet);
//                }  catch (EOFException e) {
//                    System.out.println(e.toString());
//                    break;
//                }
//            }
//
//            handle.close();
//            return "ak";
//    }
//
//    private String testPcap(String  path){
//
//        // StringBuilder is used to get
//        // error messages in case
//        // if any error occurs
//        StringBuilder errbuf = new StringBuilder();
//
//        // Making Pcap object an opening pcap file
//        // in offline mode and passing pcap filename
//        // and StringBuilder object to the function
//        org.jnetpcap.Pcap pcap = org.jnetpcap.Pcap.openOffline(path, errbuf);
//
//
//        pcap.loop(-1, new JPacketHandler() {
//
//            @Override
//            public void nextPacket(JPacket jPacket, Object errbuf) {
//                System.out.println(jPacket.toString());
//            }
//        }, errbuf);
//
//       return "oek";
//    }
    private String uploadFile(File path) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(path));
        body.add("keepFileName", true);
        body.add("localUpload ", true);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ResponseDto> response = restTemplate.postForEntity(upload, requestEntity, ResponseDto.class);
        if (response != null && response.getBody() != null && response.getBody().getData() != null) {
//            path.delete();
            return response.getBody().getData().getFileDownloadUri();
        }
        return null;
    }

    private String convertPPTtoText(String path) throws Exception {
        if (path.endsWith("pptx")) {
            SlideShow<XSLFShape, XSLFTextParagraph> slideshow
                    = new XMLSlideShow(new FileInputStream(path));
            SlideShowExtractor<XSLFShape, XSLFTextParagraph> slideShowExtractor
                    = new SlideShowExtractor<XSLFShape, XSLFTextParagraph>(slideshow);
            slideShowExtractor.setCommentsByDefault(true);
            slideShowExtractor.setMasterByDefault(true);
            slideShowExtractor.setNotesByDefault(true);

            String allTextContentInSlideShow = slideShowExtractor.getText();
//            StringBuilder sb = new StringBuilder();
//            for (XSLFSlide slide : ((XMLSlideShow) slideshow).getSlides()) {
//                for (org.apache.poi.ooxml.POIXMLDocumentPart part : slide.getRelations()) {
//                    if (part.getPackagePart().getPartName().getName().startsWith("/ppt/diagrams/data")) {
//                        org.apache.xmlbeans.XmlObject xmlObject = org.apache.xmlbeans.XmlObject.Factory.parse(part.getPackagePart().getInputStream());
//                        org.apache.xmlbeans.XmlCursor cursor = xmlObject.newCursor();
//                        while (cursor.hasNextToken()) {
//                            if (cursor.isText()) {
//                                sb.append(cursor.getTextValue() + "\r\n");
//                            }
//                            cursor.toNextToken();
//                        }
//                        sb.append(slide.getSlideNumber() + "\r\n\r\n");
//                    }
//                }
//            }
//            String allTextContentInDiagrams = sb.toString();

//        POITextExtractor textExtractor = slideShowExtractor.getMetadataTextExtractor();
//        String metaData = textExtractor.getText();

            return allTextContentInSlideShow;
        } else {
            SlideShow<HSLFShape, HSLFTextParagraph> slideshow
                    = new HSLFSlideShow(new FileInputStream(path));

            SlideShowExtractor<HSLFShape, HSLFTextParagraph> slideShowExtractor
                    = new SlideShowExtractor<>(slideshow);
            slideShowExtractor.setCommentsByDefault(true);
            slideShowExtractor.setMasterByDefault(true);
            slideShowExtractor.setNotesByDefault(true);
            String allTextContentInSlideShow = slideShowExtractor.getText();
            StringBuilder sb = new StringBuilder();
            return allTextContentInSlideShow;
        }
    }

    private String convertPcapToText(String path) throws Exception {
        final Pcap pcap = Pcap.openStream(path);
        StringBuilder content = new StringBuilder();

        pcap.loop(new PacketHandler() {
            @Override
            public boolean nextPacket(io.pkts.packet.Packet packet) throws IOException {

                if (packet.hasProtocol(Protocol.TCP)) {


                    TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
                    Buffer buffer = tcpPacket.getPayload();
                    String src = tcpPacket.getSourceIP();
                    Integer portSrc = tcpPacket.getSourcePort();
                    String dest = tcpPacket.getDestinationIP();

                    Integer portDest = tcpPacket.getDestinationPort();
                    String name = tcpPacket.getName();
                    content.append(" " + src);
                    content.append(":" + portSrc);
                    content.append(" " + dest);
                    content.append(":" + portDest);
                    content.append(" " + name + " ");

                    if (buffer != null) {
                        content.append(buffer);
                    }
                } else if (packet.hasProtocol(Protocol.UDP)) {

                    UDPPacket tcpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
                    Buffer buffer = tcpPacket.getPayload();
                    String src = tcpPacket.getSourceIP();
                    Integer portSrc = tcpPacket.getSourcePort();
                    String dest = tcpPacket.getDestinationIP();

                    Integer portDest = tcpPacket.getDestinationPort();
                    String name = tcpPacket.getName();
                    content.append(" " + src);
                    content.append(":" + portSrc);
                    content.append(" " + dest);
                    content.append(":" + portDest);
                    content.append(" " + name + " ");

                    if (buffer != null) {
                        content.append(buffer);
                    }
                }
                return true;
            }
        });
        return content.toString();
    }

    private String convertExcelToText(File file) {
        StringBuffer data = new StringBuffer();

        try {
            // Creating input stream
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            Workbook workbook = null;

            // Get the workbook object for Excel file based on file format
            if (file.getAbsolutePath().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (file.getAbsolutePath().endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                fis.close();
                throw new Exception("File not supported!");
            }

            // Get first sheet from the workbook
            int i = 0;
            int number = workbook.getNumberOfSheets();
            while (i < number) {
                Sheet sheet = workbook.getSheetAt(i);

                // Iterate through each rows from first sheet
                Iterator<Row> rowIterator = sheet.iterator();

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    // For each row, iterate through each columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {

                        Cell cell = cellIterator.next();

                        switch (cell.getCellType()) {
                            case BOOLEAN:
                                data.append(cell.getBooleanCellValue() + ",");
                                break;

                            case NUMERIC:
                                data.append(cell.getNumericCellValue() + ",");
                                break;

                            case STRING:
                                data.append(cell.getStringCellValue() + ",");
                                break;

                            case BLANK:
                                data.append("" + ",");
                                break;

                            default:
                                data.append(cell + ",");
                        }
                    }
                    // appending new line after each row
                    data.append('\n');
                }
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.toString();
    }

    private void unzip(File file, List<File> listFile, String rootFolder) {
        LOGGER.info("giai nen ");
        try {
            if (!file.getAbsolutePath().endsWith(".rar")) {
                if (file.getAbsolutePath().endsWith(".tar")) {
                    String path = file.getPath();
                    String inputPath = path;
                    String folder = "a" + UUID.randomUUID().toString() + "b";
//                    String tmp = rootFolder.substring(rootFolder.lastIndexOf("/")+1)+"/"+ folder;
                    String tmp = rootFolder + "/" + folder;
                    folder = rootFolder + "/" + folder;
                    Files.createDirectories(Paths.get(folder));
                    File folderTmp = new File(tmp);
                    unTarFile(file, folderTmp);
                    final File[] children = folderTmp.listFiles();
                    if (children != null) {
                        getFile(folderTmp, listFile, rootFolder);
                    }
                } else if (file.getAbsolutePath().endsWith(".tar.gz")) {
                    String path = file.getPath();
                    String folder = "a" + UUID.randomUUID().toString() + "b";
                    //                    String tmp = rootFolder.substring(rootFolder.lastIndexOf("/")+1)+"/"+ folder;
                    String tmp = rootFolder + "/" + folder;
                    folder = rootFolder + "/" + folder;
                    Files.createDirectories(Paths.get(folder));
                    File folderTmp = new File(tmp);
                    unGzip(file, tmp);
                    final File[] children = folderTmp.listFiles();
                    if (children != null) {
                        getFile(folderTmp, listFile, rootFolder);
                    }
                } else {
                    String path = file.getPath();
                    String inputPath = path;
                    String folder = "a" + UUID.randomUUID().toString() + "b";
//                    String tmp = rootFolder.substring(rootFolder.lastIndexOf("/")+1)+"/"+ folder;
                    String tmp = rootFolder + "/" + folder;
                    folder = rootFolder + "/" + folder;
                    Files.createDirectories(Paths.get(folder));
//                String folder = rootFolder+"/a" + UUID.randomUUID().toString() + "b";
//                Files.createDirectories(Paths.get(folder));
//                File destinationFolder = new File(folder);

                    boolean test = false;
                    String filter = null;
                    try {
                        new ExtractExample(file.getAbsolutePath(), tmp, test, filter).extract();
                        System.out.println("Extraction successfull");
                    } catch (Exception e) {
                        System.err.println("ERROR: " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    File folderTmp = new File(tmp);
                    final File[] children = folderTmp.listFiles();
                    if (children != null) {
                        getFile(folderTmp, listFile, rootFolder);
                    }
                }

            } else {
                if (!linux) {
                    String folder = rootFolder + "\\a" + UUID.randomUUID().toString() + "b";
                    folder = folder.substring(1);
                    Files.createDirectories(Paths.get(folder));
                    String cmd = "\"c:\\Program Files\\WinRAR\\UnRAR.exe\" x ";
                    String inputPath = "\"" + file.getAbsolutePath() + "\" ";
                    String out = " \"" + output + "\\" + folder + "\" ";
                    cmd += inputPath + out;
                    String a = "\"c:\\Program Files\\WinRAR\\UnRAR.exe\" x " + "\"D:\\test3.rar\" " + " \"D:\\test1\" ";
//                        ProcessBuilder builder = new ProcessBuilder(
//                                "cmd.exe", "/c", "\"c:\\Program Files\\WinRAR\\UnRAR.exe\" x D:\\testjhgj3.rar D:\\test1 ");
                    ProcessBuilder builder = new ProcessBuilder(
                            "cmd.exe", "/c", cmd);
                    builder.redirectErrorStream(true);
                    Process p = builder.start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while (true) {
                        line = r.readLine();
                        if (line != null && line.contains("Enter password")) {
                            break;
                        }
                        if (line == null) {
                            break;
                        }
                    }
                    p.destroy();
                    File folderTmp = new File(folder);
                    final File[] children = folderTmp.listFiles();
                    if (children != null) {
                        getFile(folderTmp, listFile, rootFolder);
                    }

                } else {
                    String s;
                    Process p;
                    try {
                        String path = file.getPath();
                        String inputPath = path;
                        String folder = "a" + UUID.randomUUID().toString() + "b";
                        //                    String tmp = rootFolder.substring(rootFolder.lastIndexOf("/")+1)+"/"+ folder;
                        String tmp = rootFolder + "/" + folder;
//                        String tmp = rootFolder.substring(rootFolder.lastIndexOf("/")+1)+"/"+ folder;
////                        String tmp = " "+rootFolder+"/"+ folder;
                        folder = rootFolder + "/" + folder;
                        Files.createDirectories(Paths.get(folder));
                        String cmd = "unrar x " + inputPath + " " + tmp;
                        LOGGER.info(" cmd {} ", cmd);
                        p = Runtime.getRuntime().exec(cmd);
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
                        String a = "";
                        if (!p.waitFor(15, TimeUnit.SECONDS)) {
                            //timeout - kill the process.
                            p.destroy(); // consider using destroyForcibly instead
                        }
                        while (br.readLine() != null) {
                            LOGGER.info("man hin hien {}", br.readLine());
//                            p.waitFor(3000,)
                            if (br.readLine() != null)
                                a += br.readLine() + " ";
                            if (a.contains("Enter password")) {
                                LOGGER.info("co password");
                                break;
                            }
                        }
                        LOGGER.info("ra khoi vong for");
                        if (a != null && a.contains("Enter password ")) {
                            p.destroy();
                        } else {
                            p.waitFor();
                            p.exitValue();
                            p.destroy();
                        }
                        File folderTmp = new File(folder);
                        final File[] children = folderTmp.listFiles();
                        if (children != null) {
                            getFile(folderTmp, listFile, rootFolder);
                        }
                    } catch (Exception e) {
                        LOGGER.info("loi");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.info("loi");
            ex.printStackTrace();
        }

    }

    private void getFile(File file, List<File> listFile, String root) {
        if (file.isDirectory()) {

            final File[] children = file.listFiles();
            if (children == null) {
                return;
            }
            for (final File each : children) {
                // gọi lại hàm traverseDepthFiles()
                getFile(each, listFile, root);
            }
        } else {
            String path = file.getAbsolutePath();
            if (path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".tar") || path.endsWith(".tar.gz")) {
                unzip(file, listFile, root);
            } else {
                listFile.add(file);
            }
        }
    }

    private List<TextDTO> convertZipToText(File file) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        List<File> listFile = new ArrayList<>();
//        String folder = folderProcess + UUID.randomUUID().toString() + "b";
        String folder = "a" + UUID.randomUUID().toString() + "b";
        Files.createDirectories(Paths.get(folder));
        unzip(file, listFile, folder);
        LOGGER.info(" file extach {}", listFile.size());
        List<CompletableFuture<TextDTO>> listResult = new ArrayList<>();
        for (File contentFile : listFile) {
            //!contentFile.getAbsolutePath().endsWith(".docx")&&!contentFile.getAbsolutePath().endsWith(".PDF")&&!contentFile.getAbsolutePath().endsWith(".xls")
//            if(!contentFile.getAbsolutePath().endsWith(".docx")&&!contentFile.getAbsolutePath().endsWith(".xls")&&!contentFile.getAbsolutePath().endsWith(".doc")) {
            CompletableFuture<TextDTO> result = processContent.processContentFile(contentFile);
            listResult.add(result);
//            }
        }
        CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> listResult.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
        List<TextDTO> contentFile = new ArrayList<>();
        for (CompletableFuture<TextDTO> result : listResult) {
            TextDTO resultCheckDto1 = result.get();
            if (resultCheckDto1 != null) {
                contentFile.add(resultCheckDto1);
            }
        }
//        folder=folder.substring(1);
        if (folder.contains("/")) {
            folder = folder.substring(folder.lastIndexOf("/") + 1);
        }
        FileUtils.deleteDirectory(new File(folder));
//        File fileDelete = new File(folder);
//        fileDelete.delete();
        file.delete();
        LOGGER.info("contentFile {}", contentFile.size());
        return contentFile;

    }

    private void unTarFile(File tarFile, File destFile) throws IOException {
        FileInputStream fis = new FileInputStream(tarFile);
        TarArchiveInputStream tis = new TarArchiveInputStream(fis);
        TarArchiveEntry tarEntry = null;

        // tarIn is a TarArchiveInputStream
        while ((tarEntry = tis.getNextTarEntry()) != null) {
            File outputFile = new File(destFile + File.separator + tarEntry.getName());
            if (tarEntry.isDirectory()) {
                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                }
            } else {
                //File outputFile = new File(destFile + File.separator + tarEntry.getName());
                System.out.println("outputFile File ---- " + outputFile.getAbsolutePath());
                outputFile.getParentFile().mkdirs();
                //outputFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(outputFile);
                IOUtils.copy(tis, fos);
                fos.close();
            }
        }
        tis.close();
    }

    private void unGzip(File inputFile, String dist) {
        try {
            String path = inputFile.getPath();
            String inputPath = path;
            String folder1 = "a" + UUID.randomUUID().toString() + "b";
            String folder = "a" + UUID.randomUUID().toString() + "b";
//            String tmp = "testgz"+"/"+ folder1;
//            String outPath = "testgz"+"/"+ folder;
            //           String tmp = " "+root.substring(root.lastIndexOf("/")+1)+"/"+ folder;
//           String outPath = " "+root.substring(root.lastIndexOf("/")+1)+"/"+ folder;
            String pathTarget = dist;
            String pathOutput = dist;
            Files.createDirectories(Paths.get(pathOutput));
            String outputFileTmp = getFileName(inputFile, pathTarget);
            System.out.println("outputFile " + outputFileTmp);
            File tarFile = new File(outputFileTmp);
            // Calling method to decompress file
            tarFile = deCompressGZipFile(inputFile, tarFile);
            File destFile = new File(dist);
            unTarFile(tarFile, destFile);
            tarFile.delete();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getFileName(File inputFile, String outputFolder) {
        return outputFolder + File.separator +
                inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));
    }

    private File deCompressGZipFile(File gZippedFile, File tarFile) throws IOException {
        FileInputStream fis = new FileInputStream(gZippedFile);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(fis);

        FileOutputStream fos = new FileOutputStream(tarFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gZIPInputStream.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        gZIPInputStream.close();
        return tarFile;
    }

    private List<TextDTO> convertFileAttachmentToText(File file) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        List<File> listFile = new ArrayList<>();
        listFile.add(file);
        List<CompletableFuture<TextDTO>> listResult = new ArrayList<>();
        for (File contentFile : listFile) {
            CompletableFuture<TextDTO> result = processContent.processContentFile(contentFile);
            listResult.add(result);
        }
        CompletableFuture.allOf(listResult.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> listResult.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
        List<TextDTO> contentFile = new ArrayList<>();
        for (CompletableFuture<TextDTO> result : listResult) {
            TextDTO resultCheckDto1 = result.get();
            if (resultCheckDto1 != null) {
                contentFile.add(resultCheckDto1);
            }
        }
        file.delete();
        return contentFile;

    }


    void sendReplyTopic(ResponseContentsDTO responseContentsDTO) {
        long start = System.currentTimeMillis();
        long end = start;
        try {
            kafkaClient.callKafkaServerWorker(KafkaProperties.CONTENT_REPLY_TOPIC, responseContentsDTO.getMediaUuidKey(), responseContentsDTO.toJsonString());
        }catch (Exception ex){
            ex.printStackTrace();
            responseContentsDTO.setStatus(false);
            responseContentsDTO.setData(null);
            kafkaClient.callKafkaServerWorker(KafkaProperties.CONTENT_REPLY_TOPIC, responseContentsDTO.getMediaUuidKey(), responseContentsDTO.toJsonString());

        }
        end = System.currentTimeMillis();
//        LOGGER.info("Reply content - Push to {} msg: {} - total: {} ms => {}",
//                KafkaProperties.CONTENT_REPLY_TOPIC, responseContentsDTO.toJsonString(),
//                (end - start), true);
    }
}
