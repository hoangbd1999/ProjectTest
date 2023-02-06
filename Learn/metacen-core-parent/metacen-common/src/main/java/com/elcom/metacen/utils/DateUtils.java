package com.elcom.metacen.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author hanh
 */
public class DateUtils {

    public static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static DateTimeFormatter yyyyMMddFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static DateFormat vnDateFormatter;
    public static ZoneId vnzone;

    static {
        vnzone = ZoneId.of("Asia/Ho_Chi_Minh");
        vnDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        vnDateFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static LocalDateTime parse(String str) {
        LocalDateTime dateTime = LocalDateTime.parse(str, defaultFormatter);
        return dateTime;
    }

    public static LocalDateTime parse(String str, DateTimeFormatter formatter) {
        if (formatter == null)
            formatter = defaultFormatter;
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        return dateTime;
    }

    public static Date parseDate(String str) throws ParseException {
        Date dateTime = vnDateFormatter.parse(str);
        return dateTime;
    }

    public static Date parseDate(String str, String pattern, TimeZone zone) throws ParseException {
        if (pattern == null){
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        DateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(zone);
        Date dateTime = formatter.parse(str);
        return dateTime;
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime != null)
            return dateTime.format(defaultFormatter);
        else
            return "";
    }

    public static String format(Date dateTime) {
        if (dateTime != null){
            return vnDateFormatter.format(dateTime);
        }
        else
            return "";
    }

    public static String format(Date dateTime, String pattern, TimeZone zone) {
        if (dateTime != null){
            if (pattern == null){
                pattern = "yyyy-MM-dd HH:mm:ss";
            }
            DateFormat formatter = new SimpleDateFormat(pattern);
            formatter.setTimeZone(zone);

            return formatter.format(dateTime);
        }
        else
            return "";
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime != null) {
            if (formatter == null)
                return dateTime.format(defaultFormatter);
            else
                return dateTime.format(formatter);
        }
        else
            return "";
    }

    public static LocalDateTime getDateFromLong(long timestamp) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.of("+07:00"));
        } catch (DateTimeException tdException) {
            //  throw new
            return null;
        }
    }

    public static Long getLongFromDateTime(LocalDateTime dateTime) {
        //return dateTime.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
        return dateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    public static Long getLongFromDateTimeLocal(LocalDateTime dateTime) {
        return dateTime.atZone(vnzone).toInstant().toEpochMilli();
    }

    public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert != null? dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    public static LocalDateTime convertToLocalDateTimeViaMilisecond(Date dateToConvert) {
        if (dateToConvert == null)
            return null;

        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

//    public static void main(String[] args) {
//        String s = "2020-02-27 01:00:00";
//        LocalDateTime localDateTime = DateUtils.parse(s);
//        System.out.println("localDateTime: " + localDateTime);
//
//        long lutc = DateUtils.getLongFromDateTime(localDateTime);
//        long litc = DateUtils.getLongFromDateTimeLocal(localDateTime);
//
//        System.out.println("lutc: " + lutc);
//        System.out.println("litc: " + litc);
//
//        System.out.println("from lUTC: " + DateUtils.getDateFromLong(lutc));
//        System.out.println("from litc: " + DateUtils.getDateFromLong(litc));
//
//    }
}
