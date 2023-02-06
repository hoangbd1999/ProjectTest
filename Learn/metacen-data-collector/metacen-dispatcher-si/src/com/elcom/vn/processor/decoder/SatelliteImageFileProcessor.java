package com.elcom.vn.processor.decoder;

import com.elcom.util.queue.BoundBlockingQueue;
import com.elcom.vn.DispatcherMain;
import com.elcom.vn.common.AppUtils;
import com.elcom.vn.object.EventContainer;
import com.elcom.vn.object.SatelliteImageMessage;
import com.elcom.vn.object.request.GeoModelCreateCoveragestores;
import com.elcom.vn.object.request.GeoModelCreateCoveragestoresDetails;
import com.elcom.vn.object.request.GeoModelCreateLayer;
import com.elcom.vn.object.request.GeoModelCreateLayerDetails;
import com.elcom.vn.utils.FileUtil;
import com.elcom.vn.utils.JSONConverter;
import com.elcom.vn.utils.StringUtil;
import com.google.common.base.Stopwatch;
import jakarta.xml.bind.JAXBContext;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLInputFactory;
import org.apache.commons.io.FileUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

public class SatelliteImageFileProcessor extends AbstractFileProcessor {

    private final DateTimeFormatter FMT;
    
    public SatelliteImageFileProcessor(String name, Logger logger, DocumentBuilderFactory dbFactory, JAXBContext jc, XMLInputFactory xmlif, SAXParser saxParser
                            , BoundBlockingQueue<EventContainer> vsatQueue, BoundBlockingQueue<String> decodeFileQueue) throws SAXException, ParserConfigurationException {
        
        super(name, logger, vsatQueue, decodeFileQueue);
        
        FMT = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    }

    @Override
    protected void childAction() throws Throwable {
        try {
            
            String folderData = decodeFileQueue.take();
            
            if( StringUtil.isNullOrEmpty(folderData) )
                return;
            
            logger.info("Xu ly folder: " + folderData + " , decodeFileQueue.currentSize: " + decodeFileQueue.size());

            long startTime = System.currentTimeMillis();
            
            try {
                
                folderData = folderData.trim();
                
                String folderName = folderData.substring(folderData.lastIndexOf(File.separator));
                
                String[] arr = folderName.split("_");
                if( arr == null || arr.length < 7 ) {
                    logger.error("Invalid folder name: [ {} ]", folderName);
                    return;
                }
                
                // read from folder name ----------------------
                String missionId = arr[0].replace(File.separator, "");
                String productLevel = arr[1];
                
                Long captureTime = 0L;
                try {
                    captureTime = FMT.parseDateTime(arr[2].replace("T", "")).toDate().getTime();
                } catch (Exception e) {
                    logger.error("ex: ", e);
                }
                
                String baseLineNumber = arr[3];
                String relativeOrbitNumber = arr[4];
                String tileNumber = arr[5];
                
                Long secondTime = 0L;
                try {
                    secondTime = FMT.parseDateTime(arr[6].replace("T", "")).toDate().getTime();
                } catch (Exception e) {
                    logger.error("ex: ", e);
                }
                // ----------------------------------------
                
                
                // read from `infor.meta` ----------------------
                Double originLongitude = null;
                Double originLatitude = null;
                Double cornerLongitude = null;
                Double cornerLatitude = null;
                
                File metaFile = new File(folderData + File.separator + "infor.meta");
                if( !metaFile.exists() ) {
                    logger.error("Missing .meta file, return!");
                    try {
                        FileUtils.deleteDirectory(new File(folderData));
                    } catch (Exception e) {
                        logger.error("[Missing .meta file] Clean origin data folder error: {}", StringUtil.printException(e));
                    }
                    return;
                }
                    
                try (BufferedReader reader = new BufferedReader(new FileReader(metaFile))) {
                    String line = reader.readLine();
                    while ( line != null ) {

                        line = line.trim();
                        
                        if( line.contains("Origin Longitude") ) {
                            String s = line.substring(line.indexOf("=") + 1);
                            originLongitude = Double.parseDouble(s);
//                            logger.info("s: [{}] - originLongitude: [{}]", s, originLongitude);
                        }
                        else if( line.contains("Origin Latitude") ) {
                            String s = line.substring(line.indexOf("=") + 1);
                            originLatitude = Double.parseDouble(s);
//                            logger.info("s: [{}] - originLatitude: [{}]", s, originLatitude);
                        }
                        else if( line.contains("Corner Longitude") ) {
                            String s = line.substring(line.indexOf("=") + 1);
                            cornerLongitude = Double.parseDouble(s);
//                            logger.info("s: [{}] - cornerLongitude: [{}]", s, cornerLongitude);
                        }
                        else if( line.contains("Corner Latitude") ) {
                            String s = line.substring(line.indexOf("=") + 1);
                            cornerLatitude = Double.parseDouble(s);
//                            logger.info("s: [{}] - cornerLatitude: [{}]", s, cornerLatitude);
                        }
                        
                        // read next line
                        line = reader.readLine();
                    }
                } catch (Exception ex) {
                    logger.error("ex: ", ex);
                }
                // ----------------------------------------
                
                // Upload folder data to NAS ----------------------
                String destFolderPath = DispatcherMain.DEST_UPLOAD_PATH + File.separator + AppUtils.folderDayHourMinute("yyyyMMdd/HH") + File.separator + folderName;
                destFolderPath = destFolderPath.replace("//", "/");
                
                File destFolder = new File(destFolderPath);
                if( !destFolder.exists() ) {
                    try {
                        
                        // FileUtils.moveDirectory(new File(folderData), destFolder);
                        
//                        File srcFolder = new File(folderData);
                        
                        File srcFolder = new File(folderData);

                        logger.info("Starting copyDirectory from [ {} ] -> to [ {} ] .......", folderData, destFolderPath);
                        Stopwatch stopwatch = Stopwatch.createStarted();
                        
                            // FileUtils.copyDirectory(srcFolder, destFolder);
                            FileUtils.moveDirectory(srcFolder, destFolder);
                            
                        stopwatch.stop();
                        logger.info("moveDirectory from [ {} ] -> to [ {} ] SUCCESS!, spent [ " + getElapsedTime(stopwatch.elapsed(TimeUnit.MILLISECONDS)) + " ] ms"
                                    , folderData, destFolderPath);
                        
//                        srcFolder.renameTo(new File(folderData.replace("_FN", "")));
                        
                        //TODO
                        // logger.info("Delete folderData [ {} ]: {}", folderData, FileUtil.deleteDirectoryNonEmptyRecursively2(srcFolder));
                        
                    } catch (Exception e) {
                        String errMsg = StringUtil.printException(e);
                        logger.error("Error when copyDirectory [ " + folderData + " ] -> to [ " + destFolderPath + " ] , ex: [ {} ]", errMsg);
                        if( !errMsg.contains("Unable to delete") ) // Nếu gặp lỗi không xóa được folder gốc, thì vẫn tiếp tục cho luồng chạy bình thường, các lỗi khác thì mới cancel process
                            return;
                    }
                } else {
                    logger.error("destFolder [ {} ] is existed, return!", destFolderPath);
                    return;
                }
                // ----------------------------------------
                
                
                // Call api create coverStorage ----------------------
                String geoTiffFilePath = destFolderPath + File.separator + "infor.tiff";
                
                String geoCoverageName = StringUtil.getSaltString(16);
                
                GeoModelCreateCoveragestores geoModelCreateCoveragestores = new GeoModelCreateCoveragestores(
                    new GeoModelCreateCoveragestoresDetails(DispatcherMain.GEOSERVER_WORKSPACE
                                                        , geoCoverageName, "GeoTIFF", "geotiff", true, true, geoTiffFilePath
                ));
                
                URL obj = new URL(DispatcherMain.GEOSERVER_URL + "/rest/workspaces/new-workspace/coveragestores");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                
                Authenticator authenticator;
                try {
                    authenticator = new Authenticator() {
                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            return (new PasswordAuthentication("admin", "geoserver".toCharArray())); //enter credentials 
                        }
                    };
                    con.setAuthenticator(authenticator);
                } catch (Exception e) {
                    logger.error("Geoserver Authenticator FAILED, ex: {}", e);
                    return;
                }
                
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("User-Agent", "Dispatcher-satellite-image");
		con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(JSONConverter.toJSON(geoModelCreateCoveragestores).getBytes());
                    os.flush();
                }

                StringBuffer responseBody;
                try ( BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    responseBody = new StringBuffer();
                    while ((inputLine = in.readLine()) != null)
                        responseBody.append(inputLine);
                } catch(Exception e) {
                    logger.error("Create coverageStores FAILED, ex: {}", e);
                    return;
                }
                
                int responseCode = con.getResponseCode();
                
                if ( responseCode != HttpURLConnection.HTTP_CREATED || responseBody.length() == 0 || !responseBody.toString().equals(geoCoverageName) ) {
                    logger.error("Create coverageStores FAILED, responseCode: {}, responseBody: {}", responseCode, responseBody);
                    return;
                }
                // ----------------------------------------
                
                
                // Call API create GeoLayer ----------------------
                // String geoLayerName = StringUtil.getSaltString(16);
                GeoModelCreateLayer geoModelCreateLayer = new GeoModelCreateLayer(
                    new GeoModelCreateLayerDetails(geoCoverageName, geoCoverageName, geoCoverageName
                                                , "{ \"string\": [ \"GEOTIFF\", \"PNG\", \"JPEG\", \"TIFF\" ] }"
                                                , "{ \"string\": \"EPSG:4326\" }"
                                                , "{ \"string\": \"EPSG:4326\" }"   
                                                , "EPSG:4326")
                );
                
                obj = new URL(DispatcherMain.GEOSERVER_URL + String.format("/rest/workspaces/new-workspace/coveragestores/%s/coverages", geoCoverageName));
		con = (HttpURLConnection) obj.openConnection();
                con.setAuthenticator(authenticator);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("User-Agent", "Dispatcher-satellite-image");
		con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(JSONConverter.toJSON(geoModelCreateLayer).getBytes());
                    os.flush();
                }

                responseBody = new StringBuffer();
                try ( BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                        responseBody.append(inputLine);
                } catch(Exception e) {
                    logger.error("Create layer FAILED, ex: ", e);
                    return;
                }
                
                responseCode = con.getResponseCode();
                
                if ( responseCode != HttpURLConnection.HTTP_CREATED || responseBody.length() == 0 || !responseBody.toString().equals(geoCoverageName) ) {
                    logger.error("Create layer FAILED, responseCode: {}, responseBody: {}", responseCode, responseBody);
                    return;
                }
                // ----------------------------------------
                
                
                
                SatelliteImageMessage satelliteImageMessage = new SatelliteImageMessage(DispatcherMain.SATELLITE_NAME, missionId, productLevel, captureTime, baseLineNumber
                                                                    , relativeOrbitNumber, tileNumber, secondTime
                                                                    , originLongitude, originLatitude, cornerLongitude, cornerLatitude
                                                                    , folderName.replace(File.separator, "")
                                                                    , destFolderPath
                                                                    , DispatcherMain.GEOSERVER_URL + "/wms", DispatcherMain.GEOSERVER_WORKSPACE, geoCoverageName
                                                                    , DispatcherMain.DATA_VENDOR);
                
                this.enqueue(new EventContainer(EventContainer.MESSAGE_TYPE_SATELLITE_IMAGE, gson.toJson(satelliteImageMessage), satelliteImageMessage.getUuidKey()), vsatQueue);
                
            } catch (Exception ex) {
                logger.error("ex: ", ex);
            } finally {
                String timeTaked5 = getElapsedTime(System.currentTimeMillis() - startTime);
                int len5 = timeTaked5.length() - 5;
                if( len5 > 3 )
                    logger.info("fn: [ " + len5 + " ] , fi: [ " + timeTaked5.substring(0, 1) + "] , ti: " + timeTaked5);
            }
        } catch (Exception ex) {
            logger.error("ex: ", ex);
        }
    }
    
    /***
        TOP 3 faster
    ***/
    private boolean copyFileUsingStream(File source, File dest) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[16 * 1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            logger.error("ex: ", e);
            return false;
        } finally {
            if( is != null ) {
                try {
                    is.close();
                } catch (Exception e) {}
            }
            if( os != null ) {
                try {
                    os.close();
                } catch (Exception e) {}
            }
        }
        return true;
    }
    
    private void copyFileUsingStreamBaeldung(File source, File dest) {
        try {
            try (
                InputStream in = new BufferedInputStream(new FileInputStream(source));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(dest)) ) {

                byte[] buffer = new byte[1024];
                int lengthRead;
                while ((lengthRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, lengthRead);
                    out.flush();
                }
            }
        } catch (Exception e) {
            logger.error("ex: ", e);
        }
    }
    
    private void copyFileUsingChannel(File source, File dest) {
        try {
            FileChannel sourceChannel = null;
            FileChannel destChannel = null;
            try {
                sourceChannel = new FileInputStream(source).getChannel();
                destChannel = new FileOutputStream(dest).getChannel();
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            } finally {
                if( sourceChannel != null )
                 sourceChannel.close();
                if( destChannel != null )
                 destChannel.close();
            }
        } catch (Exception e) {
            logger.error("ex: ", e);
        }
    }
    
    private void copyFileUsingApacheCommonsIO(File source, File dest) {
        try {
            FileUtils.copyFile(source, dest);
        } catch (Exception e) {
            logger.error("ex: ", e);
        }
    }
    
    /***
        TOP 2 faster
    ***/
    private boolean copyFileUsingJava7Files(File source, File dest) {
        try {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.error("ex: ", e);
            return false;
        }
        return true;
    }
    
    private void copyFileUsingGuava(File source, File dest) {
        try {
            com.google.common.io.Files.copy(source, dest);
        } catch (Exception e) {
            logger.error("ex: ", e);
        }
    }
    
    /***
        TOP 1 faster
    ***/
    private boolean copyFileUsingChannelFast(File mediaFile, File destFile) {
        
        ReadableByteChannel src = null;
        
        WritableByteChannel dest = null;
        
        try {
            // allocate the stream ... only for example
            final InputStream input = new FileInputStream(mediaFile);
            
            final OutputStream output = new FileOutputStream(destFile);

            // get an channel from the stream
            src = Channels.newChannel(input);
            
            dest = Channels.newChannel(output);

            final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

            while (src.read(buffer) != -1) {
                // prepare the buffer to be drained
                buffer.flip();
                // write to the channel, may block
                dest.write(buffer);
                // If partial transfer, shift remainder down
                // If buffer is empty, same as doing clear()
                buffer.compact();
            }
            // EOF will leave buffer in fill state
            buffer.flip();
            // make sure the buffer is fully drained.
            while (buffer.hasRemaining()) {
                dest.write(buffer);
            }
        } catch (Exception e) {
            logger.error("ex_up: ", e);
            return false;
        } finally {
            // closing the channels
            if( src != null ) {
                try {
                    src.close();
                } catch (Exception e) {
                    return false;
                }
            }
            if( dest != null ) {
                try {
                    dest.close();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void uploadFile(File source, File dest, String fileName) {
        try {
            //copy file conventional way using Stream
            long start = System.currentTimeMillis();
            copyFileUsingStream(source, dest);
            logger.error("Time taken by Stream Copy ("+fileName+") = "+(System.currentTimeMillis()-start)); // 134ms
            
            //copy file conventional way using Stream Baeldung
            start = System.currentTimeMillis();
            copyFileUsingStreamBaeldung(source, new File(dest.getPath().replace(fileName, "111-" + fileName)));
            logger.error("Time taken by Stream Baeldung Copy  ("+fileName+") = "+(System.currentTimeMillis()-start)); // 134ms

            //copy files using java.nio FileChannel
            start = System.currentTimeMillis();
            copyFileUsingChannel(source, new File(dest.getPath().replace(fileName, "222-" + fileName)));
            logger.error("Time taken by Channel Copy ("+fileName+") = "+(System.currentTimeMillis()-start)); // 15ms

            //copy files using apache commons io
            start = System.currentTimeMillis();
            copyFileUsingApacheCommonsIO(source, new File(dest.getPath().replace(fileName, "333-" + fileName)));
            logger.error("Time taken by Apache Commons IO Copy ("+fileName+") = "+(System.currentTimeMillis()-start)); // 14ms

            //using Java 7 Files class
            // Hàm này đang ko dùng đc, lỗi NoSuchFileException
            start = System.currentTimeMillis();
            copyFileUsingJava7Files(source, new File(dest.getPath().replace(fileName, "444-" + fileName)));
            logger.error("Time taken by Java7 Files Copy ("+fileName+") = "+(System.currentTimeMillis()-start)); // 12ms
            
            //using Google Guava
            start = System.currentTimeMillis();
            copyFileUsingGuava(source, new File(dest.getPath().replace(fileName, "555-" + fileName)));
            logger.error("Time taken by Guava Files Copy ("+fileName+") = "+(System.currentTimeMillis()-start)); // 28ms
        } catch (Exception e) {
            logger.error("ex: ", e);
        }
    }
}
