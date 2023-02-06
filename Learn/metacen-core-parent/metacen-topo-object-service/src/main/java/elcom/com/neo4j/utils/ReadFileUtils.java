package elcom.com.neo4j.utils;//package com.elcom.media.utils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.nio.channels.FileChannel;
//
//import it.sauronsoftware.jave.AudioInfo;
//import it.sauronsoftware.jave.Encoder;
//import it.sauronsoftware.jave.MultimediaInfo;
//import it.sauronsoftware.jave.VideoInfo;
//
///**
// *
// * @author Admin
// */
//public class ReadFileUtils {
//
//    public MultimediaInfo m;
//    private Long ls;
//    private File file;
//
//    /**
//     * Initialisieren Sie die aus der Datei erhaltenen Informationen
//     *
//     * @param file
//     */
//    public ReadFileUtils(File file) {
//        this.file = file;
//
//        Encoder encoder = new Encoder();
//        FileChannel fc = null;
//        try {
//            this.m = encoder.getInfo(file);
//            this.ls = this.m.getDuration();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (null != fc) {
//                try {
//                    fc.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
//    }
//
//    /**
//     * Stellen Sie fest, ob die Datei im Format MP4, H264 vorliegt
//     */
//    public boolean isMp4H264() {
//        VideoInfo v = m.getVideo();
//        AudioInfo a = m.getAudio();
//        if (a == null || v == null) {
//            return false;
//        }
//
//        if (!"h264".equalsIgnoreCase(v.getDecoder())) {
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Bestimmen Sie, ob die Datei ein JPG ist
//     */
//    public boolean isJpg() {
//        VideoInfo v = m.getVideo();
//        if (v == null) {
//            return false;
//        }
//
//        if (!"mjpeg".equalsIgnoreCase(v.getDecoder()) && !"mjpg".equalsIgnoreCase(v.getDecoder())
//                && !"jpg".equalsIgnoreCase(v.getDecoder())) {
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Bestimmen Sie, ob die Datei mp3 ist
//     */
//    public boolean isMp3() {
//        AudioInfo a = m.getAudio();
//        if (a == null) {
//            return false;
//        }
//
//        if (!"mp3".equalsIgnoreCase(a.getDecoder())) {
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Erhalten der Dateidauer
//     *
//     * @return
//     */
//    public Integer getFileTime() {
//        return Integer.parseInt(ls / 1000 + "");
//    }
//
//    /**
//     * Holen Sie sich die Höhe der Datei
//     *
//     * @return
//     */
//    public Integer getFileHeight() {
//        return m.getVideo().getSize().getHeight();
//    }
//
//    /**
//     * Holen Sie sich die Breite der Datei
//     *
//     * @return
//     */
//    public Integer getFileWidth() {
//        return m.getVideo().getSize().getWidth();
//    }
//
//    /**
//     * Das Format der erhaltenen Datei ist mov, mp3, jpg
//     *
//     * @return
//     */
//    public String getFileFormat() {
//        return m.getFormat();
//    }
//
//    /**
//     * Holen Sie sich die Dateigre
//     *
//     * @return
//     */
//    public Long getFileSize() {
//
//        FileInputStream fis;
//        Long size = null;
//        try {
//            fis = new FileInputStream(file);
//            FileChannel fc = fis.getChannel();
//
//            BigDecimal fileSize = new BigDecimal(fc.size());
//
//            // "MB""
//            // size = new Long(fileSize.divide(new BigDecimal(1048576), 2,
//            // RoundingMode.HALF_UP) + "");
//            // "KB"
//            size = new Long(fileSize.divide(new BigDecimal(1024), 0, RoundingMode.HALF_UP) + "");
//            System.out.println("Die Größe dieses Videos ist");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return size;
//    }
//}
