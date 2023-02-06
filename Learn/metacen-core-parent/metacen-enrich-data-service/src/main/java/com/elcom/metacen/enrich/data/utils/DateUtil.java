package com.elcom.metacen.enrich.data.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    
    public static String dateToFolderName(Date s, String format) {
        if ( s == null )
            return "";
        try {
            return new SimpleDateFormat(format).format(s);
        } catch (Exception e) {}
        return "default";
    }
}
