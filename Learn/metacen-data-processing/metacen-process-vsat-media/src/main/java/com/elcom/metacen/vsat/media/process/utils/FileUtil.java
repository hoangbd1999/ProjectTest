/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.elcom.metacen.vsat.media.process.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Admin
 */
public class FileUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    
//    public static void main(String[] args) {
//        String res = "/ttttbien2/vsat/media_files/web/20221024/09/24/8fd562b0-61f3-4e9b-b845-06930f162640.octet-stream";
//        System.out.println("res: " + res.substring(res.indexOf("media_files")));
//    }
    
    public static boolean copyFileUsingChannelFast(File mediaFile, File destFile) {
        
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
//            LOGGER.error("ex0: ", e);
            return false;
        } finally {
            // closing the channels
            if( src != null ) {
                try {
                    src.close();
                } catch (Exception e) {
                    LOGGER.error("ex1: ", e);
                    return false;
                }
            }
            if( dest != null ) {
                try {
                    dest.close();
                } catch (Exception e) {
                    LOGGER.error("ex2: ", e);
                    return false;
                }
            }
        }
        return true;
    }
}
