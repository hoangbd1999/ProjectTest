/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author Admin
 */
public class ShellUtils {

    private static final Logger log = Logger.getLogger(ShellUtils.class.getName());

    public static boolean executeCommand(String cmd) throws IOException, InterruptedException {
        Runtime run = Runtime.getRuntime();
        String[] commands = {"bash", "-c", cmd};

        Process pr = null;
        if (SystemUtils.IS_OS_LINUX) {
            pr = run.exec(commands);
        } else {
            pr = run.exec(cmd);
        }
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        while ((line = buf.readLine()) != null) {
            log.info(line);
        }
        int exitVal = pr.waitFor();
        log.info(String.format("Execute: %s => exited with error code : %d", cmd, exitVal));
        return exitVal == 0;
    }

    public static String createImageFromVideo(String videoLink, String outputFile) {
        if (StringUtil.isNullOrEmpty(videoLink) || StringUtil.isNullOrEmpty(outputFile)) {
            return null;
        }
        String[] cmd = null;
        String videoName = null;
        if (videoLink.endsWith("m3u8")) {
            videoName = UUID.randomUUID().toString() + ".png";
            cmd = new String[]{"ffmpeg", "-i", videoLink, "-s", "400x222", "-ss", "00:00:14.435",
                "-vframes", "1", outputFile + "/" + videoName};
        } else if (videoLink.endsWith("mp4")) {
            videoName = UUID.randomUUID().toString() + ".jpg";
            cmd = new String[]{"ffmpeg", "-i", videoLink, "-vf", "fps=1", outputFile + "/" + videoName};
        }
        if (!StringUtil.isNullOrEmpty(videoName)) {
            try {
                System.out.println("cmd: " + Arrays.toString(cmd));
                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                Process process = processBuilder.start();
                BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while ((line = buf.readLine()) != null) {
                    System.out.println(line);
                }
                int exitCode = process.waitFor();
                System.out.println("exitCode: " + exitCode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return videoName;
    }

    public static String createImageFromVideo2(String videoLink, String outputFilePath) {
        if (StringUtil.isNullOrEmpty(videoLink) || StringUtil.isNullOrEmpty(outputFilePath)) {
            return null;
        }
        String[] cmd = null;
        if (videoLink.endsWith("m3u8")) {
            cmd = new String[]{"ffmpeg", "-i", videoLink, "-s", "400x222", "-ss", "00:00:14.435",
                "-vframes", "1", outputFilePath};
        } else if (videoLink.endsWith("mp4")) {
            cmd = new String[]{"ffmpeg", "-i", videoLink, "-vf", "fps=1", outputFilePath};
        } else if (videoLink.endsWith("mjpg")) {
            cmd = new String[]{"ffmpeg", "-i", videoLink, "-vframes", "1", "-y", outputFilePath};
        }
        if (cmd != null && cmd.length > 0) {
            try {
                System.out.println("cmd: " + Arrays.toString(cmd));
                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                Process process = processBuilder.start();
                BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while ((line = buf.readLine()) != null) {
                    System.out.println(line);
                }
                int exitCode = process.waitFor();
                System.out.println("exitCode: " + exitCode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return outputFilePath;
    }
    
    public static String verifyRtspLink(String rtspLink) {
        if (StringUtil.isNullOrEmpty(rtspLink)) {
            return null;
        }
        //String[] cmd = new String[]{"ffmpeg", "-i", "\"" + rtspLink + "\""};
        String[] cmd = new String[]{"ffmpeg", "-i", rtspLink};
        StringBuilder builder = new StringBuilder();
        if (cmd != null && cmd.length > 0) {
            try {
                System.out.println("cmd: " + Arrays.toString(cmd));
                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                Process process = processBuilder.start();
                BufferedReader buf = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = "";
                while ((line = buf.readLine()) != null) {
                    System.out.println(line);
                    builder.append(line).append("\n");
                }
                int exitCode = process.waitFor();
                System.out.println("exitCode: " + exitCode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String rootCmd = "cmd.exe /c";
        //String cmd = "dir -L";
        //String cmd = "more D:\\863E719954D1";
        //String cmd = "curl -X GET \"http://103.21.151.157:9200/_cat/indices?v\"";
        //String cmd = "ffmpeg -i rtsp://hobao.ddns.net:8556/stream/profile/0";
        //boolean result = executeCommand(rootCmd + " " + cmd);
        //System.out.println("result: " + result);
        //Link die
        String rtsp = "rtsp://service:WSS4Bosch!@etccamth.ddns.net:554";
        //Link live
        //String rtsp = "rtsp://hobao.ddns.net:8556/stream/profile/0";
        //String rtsp = "rtsp://service:WSS4Bosch!@elccamdlvng.ddns.net:9054";
        String info = verifyRtspLink(rtsp);
        System.out.println("=========================================================");
        System.out.println("info: " + info);
    }
}
