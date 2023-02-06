/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.utils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class FileUtils {

    public static String validPath(String... path) {
        Path p = Paths.get("", path);
        return p.toString();
    }

    public static String getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1))
                .orElse("");
    }

    public static void copyFile(String fromUrl, String toLocalFile) throws Exception {
        try (BufferedInputStream in = new BufferedInputStream(new URL(fromUrl).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(toLocalFile)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.WARNING, "Can not download file [" + fromUrl + "]");
            throw new Exception(e.getMessage());
        }
    }

//    public static void main(String ...args) {
//        String url = "copy->http://192.169.10.1/image.jpg";
//        if (url.startsWith("copy->")) {
//            url = url.substring(url.indexOf("copy->") + 6);
//            System.out.println("url: " + url);
//        }
//    }
}
