package com.elcom.metacen.content.service.impl;

import com.elcom.metacen.content.dto.*;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import io.pkts.PacketHandler;
import io.pkts.buffer.Buffer;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;
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
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
public class ProcessContentFile {

    @Async("processContent")
    public CompletableFuture<TextDTO> processContentFile(File file) throws ExecutionException, InterruptedException, TimeoutException {
        TextDTO data=null;
        String fileType = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
        String fileTypes = ".exe,.msi,.java,.png,.jpeg,.exe,.msi,.java,.rut,.ts,.png,.jpg,.mp4,.mp3";
        if(fileTypes.contains(fileType)){
            return CompletableFuture.completedFuture(data);
        }
        try {
            String mimeType = Files.probeContentType(file.toPath());
            if(mimeType!=null){
                if(mimeType.contains("text")) {
                    if (mimeType.contains("html")) {
                        TextDTO textDTO= new TextDTO();
                        try {
                            String content = Files.readString(file.toPath());
                            Document doc = Jsoup.parse(content);
                            String text = doc.text();
                            text = text.replaceAll("<(img.*?)>"," ");
                            textDTO.setContentUTF8(text);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                            doc = Jsoup.parse(dataGB18030);
                            text = doc.text();
                            text = text.replaceAll("<(img.*?)>"," ");
                            textDTO.setContentGB18030(text);
                            data=textDTO;
                            reader.close();
                        }catch (Exception e){
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                                String dataGB18030 = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                                Document doc = Jsoup.parse(dataGB18030);
                                String text = doc.text();
                                text = text.replaceAll("<(img.*?)>"," ");
                                textDTO.setContentGB18030(text);
                                data=textDTO;
                                reader.close();
                            }catch (Exception ex){
                                if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                    data=null;
                                }
                            }

                        }
                    } else {
                        TextDTO textDTO= new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception e){
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data=textDTO;
                                reader.close();
                            }catch (Exception ex){
                                if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                    data=null;
                                }
                            }

                        }
                    }
                } else if(mimeType.contains("sheet")|| mimeType.contains("ms-excel") ){
                    String text = convertExcelToText(file);
                    TextDTO textDTO= new TextDTO();
                    textDTO.setContentUTF8(text);
                    textDTO.setContentGB18030(text);
                    data=textDTO;
                }else if(mimeType.contains(".presentation")){
                    Long start= System.currentTimeMillis();
                    String textTest=convertPPTtoText(file.getAbsolutePath());
                    Long end= System.currentTimeMillis();
                    TextDTO textDTO= new TextDTO();
                    textDTO.setContentUTF8(textTest);
                    textDTO.setContentGB18030(textTest);
                    data=textDTO;

                }else if (mimeType.contains("powerpoint")) {
                    String textTest = convertPPTtoText(file.getAbsolutePath());
                    TextDTO textDTO = new TextDTO();
                    textDTO.setContentUTF8(textTest);
                    textDTO.setContentGB18030(textTest);
                    data = textDTO;
                }
                else if(mimeType.contains("document")||mimeType.contains("word")){
                    TextDTO textDTO= new TextDTO();
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
                        }catch (Exception ex1){
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
                        }catch (Exception ex){
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
                            }catch (Exception ex1){
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
                    data=textDTO;

                }else if(mimeType.contains("pdf")){
                    PDDocument document = PDDocument.load(file);
                    String text ="";
                    if (!document.isEncrypted()) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        text= stripper.getText(document);
                    }else {
                        PDFTextStripper stripper = new PDFTextStripper();
                        text= stripper.getText(document);
                    }
                    document.close();
                    file.delete();
                    TextDTO textDTO= new TextDTO();
                    textDTO.setContentUTF8(text);
                    textDTO.setContentGB18030(text);
                    data=textDTO;
                } else if(mimeType.contains("json")){
                     TextDTO textDTO= new TextDTO();
                        try {
                            String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                            textDTO.setContentUTF8(dataUTF8);
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception e){
                            try {
                                InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                                String dataGB18030 = new BufferedReader(reader)
                                        .lines().collect(Collectors.joining(""));
                                textDTO.setContentGB18030(dataGB18030);
                                data=textDTO;
                                reader.close();
                            }catch (Exception ex){
                                if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                    data=null;
                                }
                            }

                        }
                }else if(file.getAbsolutePath().endsWith(".octet-stream")){
                    data=null;
                }else if(file.getAbsolutePath().endsWith(".ocsp-response")){
                    data=null;

                }else if(file.getAbsolutePath().endsWith(".form-data")){
                    TextDTO textDTO= new TextDTO();
                    try {
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        textDTO.setContentUTF8(dataUTF8);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader)
                                .lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data=textDTO;
                        reader.close();
                    }catch (Exception e){
                        try {
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception ex){
                            if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                data=null;
                            }
                        }

                    }
                }else if(file.getAbsolutePath().endsWith(".x-www-form-urlencoded")) {
                    TextDTO textDTO= new TextDTO();
                    try {
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        textDTO.setContentUTF8(dataUTF8);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader)
                                .lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data=textDTO;
                        reader.close();
                    }catch (Exception e){
                        try {
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception ex){
                            if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                data=null;
                            }
                        }

                    }
                }else  if(file.getAbsolutePath().endsWith(".pcap")) {
                    String pcap = convertPcapToText(file.getAbsolutePath());
                    TextDTO textDTO= new TextDTO();
                    textDTO.setContentUTF8(pcap);
                    textDTO.setContentGB18030(pcap);
                    data=textDTO;
                }else {

                    TextDTO textDTO= new TextDTO();
                    try {
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        textDTO.setContentUTF8(dataUTF8);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader)
                                .lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data=textDTO;
                        reader.close();
                    }catch (Exception e){
                        try {
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception ex){
                            if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                data=null;
                            }
                        }

                    }
                }
            }else {
                if(file.getAbsolutePath().endsWith(".octet-stream")){
                    data=null;
                }else if(file.getAbsolutePath().endsWith(".ocsp-response")){
                    data=null;

                }else if(file.getAbsolutePath().endsWith(".form-data")){
                    TextDTO textDTO= new TextDTO();
                    try {
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        textDTO.setContentUTF8(dataUTF8);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader)
                                .lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data=textDTO;
                        reader.close();
                    }catch (Exception e){
                        try {
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception ex){
                            if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                data=null;
                            }
                        }

                    }
                }else if(file.getAbsolutePath().endsWith(".x-www-form-urlencoded")) {
                    TextDTO textDTO= new TextDTO();
                    try {
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        textDTO.setContentUTF8(dataUTF8);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader)
                                .lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data=textDTO;
                        reader.close();
                    }catch (Exception e){
                        try {
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception ex){
                            if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                data=null;
                            }
                        }

                    }
                }else  if(file.getAbsolutePath().endsWith(".pcap")) {
                    String pcap = convertPcapToText(file.getAbsolutePath());
                    TextDTO textDTO= new TextDTO();
                    textDTO.setContentUTF8(pcap);
                    textDTO.setContentGB18030(pcap);
                }else {
                    TextDTO textDTO= new TextDTO();
                    try {
                        String dataUTF8 = FileUtils.readFileToString(file, "UTF-8");
                        textDTO.setContentUTF8(dataUTF8);
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                        String dataGB18030 = new BufferedReader(reader)
                                .lines().collect(Collectors.joining(""));
                        textDTO.setContentGB18030(dataGB18030);
                        data=textDTO;
                        reader.close();
                    }catch (Exception e){
                        try {
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"GB18030");
                            String dataGB18030 = new BufferedReader(reader)
                                    .lines().collect(Collectors.joining(""));
                            textDTO.setContentGB18030(dataGB18030);
                            data=textDTO;
                            reader.close();
                        }catch (Exception ex){
                            if(textDTO.getContentGB18030()==null &&textDTO.getContentUTF8()==null ){
                                data=null;
                            }
                        }

                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CompletableFuture.completedFuture(data);
    }

    private String convertPPTtoText(String path) throws Exception {
        if(path.endsWith("pptx")) {
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
        }else {
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
    private String convertPcapToText(String path) throws Exception{
        final io.pkts.Pcap pcap = io.pkts.Pcap.openStream(path);
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
                    content.append(" "+src);
                    content.append(":"+portSrc);
                    content.append(" "+dest);
                    content.append(":"+portDest);
                    content.append(" "+name+" ");

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
                    content.append(" "+src);
                    content.append(":"+portSrc);
                    content.append(" "+dest);
                    content.append(":"+portDest);
                    content.append(" "+name+" ");

                    if (buffer != null) {
                        content.append(buffer);
                    }
                }
                return true;
            }
        });
        return content.toString();
    }

    private String convertExcelToText(File file){
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
            int i=0;
            int number = workbook.getNumberOfSheets();
            while (i<number) {
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
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.toString();
    }
}
