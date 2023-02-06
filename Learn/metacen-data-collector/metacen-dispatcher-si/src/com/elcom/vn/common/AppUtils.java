/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.vn.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Admin
 */
public class AppUtils {

    private static final AtomicLong longSeq = new AtomicLong();
    public static String getReceivedTime() {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        return currentDate;
    }

    public static String convertDateToString(Date value) {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value);
        return currentDate;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String folderDayHourMinute(String format) {
        try {
          // yyyyMMdd/HH/mm
          return new SimpleDateFormat(format).format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long convertStringToLong(String value) {
        try {
            long resp = Long.parseLong(value);
            return resp;
        } catch (Exception e) {
        }
        return 0;
    }

    public static int convertStringToInt(String value) {
        try {
            int resp = Integer.parseInt(value);
            return resp;
        } catch (Exception e) {
        }
        return 0;
    }

    public static long getSeq() {
        return AppUtils.longSeq.incrementAndGet();
    }
}
