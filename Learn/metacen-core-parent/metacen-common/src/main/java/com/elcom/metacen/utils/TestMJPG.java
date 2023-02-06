/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Admin
 */
public class TestMJPG {

    public static void main(String[] args) {
//        String uri = "http://103.21.151.157:8181/hls/luongna.m3u8";
//        String outputFilePath = "D://test.jpg";
//        TestMJPG mp = new TestMJPG(uri, outputFilePath);
//        String rootCmd = "cmd.exe /c";
//        String rootCmd = "";
//        String cmd1 = "ffmpeg -i http://103.21.151.157:8181/hls/luongna.m3u8 -s 400x222 -ss 00:00:14.435 -vframes 1 /opt/its/backend/out.png";
//        String cmd2 = "copy pom.xml D:\\pom.xml";
//        String cmd3 = "dir";
        try {
            //String video = "http://103.21.151.157:8181/hls/luongna.m3u8";
            //String[] cmd = {"ffmpeg", "-i", video,
            //    "-s", "400x222", "-ss", "00:00:14.435" ,"-vframes", "1", "/opt/its/backend/test/out123.png"};
//            String video = "https://static.1sk.vn:8403/v1.0/upload/fitness/video/27112020/3547ee45-c6b9-4cfd-97ef-601f819985c4.mp4";
//            String[] cmd = {"ffmpeg", "-i", video, "-vf", "fps=1", "/opt/its/backend/test/out123.jpg"};

            //ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            //Process process = processBuilder.start();
            //BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            //String line = "";
            //while ((line = buf.readLine()) != null) {
            //   System.out.println(line);
            //}
            //int exitCode = process.waitFor();
            //System.out.println("exitCode: " + exitCode);
            
            String videoLink = "http://103.21.151.157:8181/hls/luongna.m3u8";
            String outputFile = "/opt/its/backend/test";
            String videoName = ShellUtils.createImageFromVideo(videoLink, outputFile);
            System.out.println("videoName: " + videoName);
            
//            ShellUtils.executeCommand(rootCmd + " " + cmd1);
//            ShellUtils.executeCommand(rootCmd + " " + cmd3);
//            if(ShellUtils.executeCommand(rootCmd + " " + cmd1)){
                //ShellUtils.executeCommand(rootCmd + " " + cmd2);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("OK");
    }
}
