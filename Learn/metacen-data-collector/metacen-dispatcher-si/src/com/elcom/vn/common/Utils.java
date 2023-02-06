/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.vn.common;

import java.util.Calendar;

/**
 *
 * @author Admin
 */
public class Utils {

    public static String curDate(String space) {
        String result = "";
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int intMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        String month = (intMonth < 10) ? "0" + intMonth : String.valueOf(intMonth);
        int intDay = Calendar.getInstance().get(Calendar.DATE);
        String day = (intDay < 10) ? "0" + intDay : String.valueOf(intDay);
        result = day + space + month + space + year;
        return result;
    }

    public static String curDate1(String space) {
        String result = "";
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int intMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        String month = (intMonth < 10) ? "0" + intMonth : String.valueOf(intMonth);
        int intDay = Calendar.getInstance().get(Calendar.DATE);
        String day = (intDay < 10) ? "0" + intDay : String.valueOf(intDay);
        result = year + space + month + space + day;
        return result;
    }

    public static int getCurStrDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public static int getCurHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static String curTime() {
        String result = "";
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int intMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        String month = (intMonth < 10) ? "0" + intMonth : String.valueOf(intMonth);
        int intDay = Calendar.getInstance().get(Calendar.DATE);
        String day = (intDay < 10) ? "0" + intDay : String.valueOf(intDay);
        int intHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String hh = (intHour < 10) ? "0" + intHour : String.valueOf(intHour);
        int intMinute = Calendar.getInstance().get(Calendar.MINUTE);
        String mm = (intMinute < 10) ? "0" + intMinute : String.valueOf(intMinute);
        int intSecond = Calendar.getInstance().get(Calendar.SECOND);
        String ss = (intSecond < 10) ? "0" + intSecond : String.valueOf(intSecond);
        result = day + month + year + "_" + hh + mm + ss;
        return result;
    }

    public static String curTime24(String space) {
        String result = "";
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int intMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        String month = (intMonth < 10) ? "0" + intMonth : String.valueOf(intMonth);
        int intDay = Calendar.getInstance().get(Calendar.DATE);
        String day = (intDay < 10) ? "0" + intDay : String.valueOf(intDay);
        int intHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String hh = (intHour < 10) ? "0" + intHour : String.valueOf(intHour);
        int intMinute = Calendar.getInstance().get(Calendar.MINUTE);
        String mm = (intMinute < 10) ? "0" + intMinute : String.valueOf(intMinute);
        int intSecond = Calendar.getInstance().get(Calendar.SECOND);
        String ss = (intSecond < 10) ? "0" + intSecond : String.valueOf(intSecond);
        result = day + space + month + space + year + " " + hh + ":" + mm + ":" + ss;
        return result;
    }
}
