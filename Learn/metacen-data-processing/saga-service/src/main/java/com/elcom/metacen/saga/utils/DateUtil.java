package com.elcom.metacen.saga.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DateUtil {

    public static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Date getDateTime(String strTime, String formatDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatDate);
        Date date = null;
        try {
            date = formatter.parse(strTime);
        } catch (ParseException e) {
        }

        return date;
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime != null) {
            return dateTime.format(defaultFormatter);
        } else {
            return "";
        }
    }

    public static long getDaysBetweenTwoDates(String strDate1, String strDate2) {

        SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date date1 = myFormat.parse(strDate1);
            Date date2 = myFormat.parse(strDate2);
            return TimeUnit.DAYS.convert(date2.getTime() - date1.getTime(), TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static long minutesFromTwoTimes(DateTimeFormatter fmt, String firstTime, String secondTime) {
        try {
            LocalTime t1 = LocalTime.parse(firstTime, fmt);
            LocalTime t2 = LocalTime.parse(secondTime, fmt);
            long result = ChronoUnit.MINUTES.between(t1, t2);
            return result < 0 ? 0 : result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static boolean validateFormat(String s, String format) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(s);
            if (!s.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
        }
        return date != null;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    public static Date getDayOfThisMonth(int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    public static Date cacularDate(Date dateFrom, int value) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateFrom);
        cal.add(Calendar.DATE, value);
        return cal.getTime();
    }

    public static Date stringToDateReport(String dateInString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = formatter.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String changeFormat(String s, String inputFormat, String outFormat) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
        return toString(formatter.parse(s), outFormat);
    }

    public static Date toDate(String s, DateTimeFormat format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(format.getDescription());
        return formatter.parse(s);
    }

    public static Date toDate(String s, String format, Date defaultVal) throws ParseException {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.parse(s);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static Date toDate(String s, String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(s);
    }

    public static String toString(Date s, String format) throws ParseException {
        if (s == null) {
            return "";
        }

        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(s);
    }

    public static Date add(Date dt, int calendar, Integer amount) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, amount);
        dt = c.getTime();
        return dt;
    }

    public static Date addSecond(Date dt, Integer amount) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.SECOND, amount);
        dt = c.getTime();
        return dt;
    }

    public static Date addMiliSecond(Date dt, Integer amount) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.MILLISECOND, amount);
        dt = c.getTime();
        return dt;
    }

    public static Date addHour(Date dt, Integer amount) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.HOUR, amount);
        dt = c.getTime();
        return dt;
    }

    public static Date addDay(Date dt, Integer amount) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, amount);
        dt = c.getTime();
        return dt;
    }

    public static boolean isValidDate(String value) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            df.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isValidFormat(String value) {
        String format = "yyyy-MM-dd";
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }

    public static Date addMonth(Date dt, Integer amount) {

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.MONTH, amount);
        dt = c.getTime();
        return dt;
    }

    public static int getNextMonthIntValue(Date currDate) {

        Calendar c = Calendar.getInstance();
        c.setTime(currDate);
        c.add(Calendar.MONTH, 1);
        return Integer.parseInt(new SimpleDateFormat("MM").format(c.getTime()));
    }

    public static String today(DateTimeFormat format) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format.getDescription());
        return df.format(new Date());
    }

    public static String today(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }

    public static Integer subtract(Date dt1, Date dt2) {
        long diff = Math.abs(dt1.getTime() - dt2.getTime());
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return Integer.valueOf(String.valueOf(diffDays));
    }

    public static Integer getDayOfMonth(Date from) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);

        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static Integer getMonth(Date from) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);

        return cal.get(Calendar.MONTH);
    }

    public static Integer getYear(Date from) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);

        return cal.get(Calendar.YEAR);
    }

    public static Date getLastDateOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public Timestamp toTimestamp(Date data) {
        return new java.sql.Timestamp(data.getTime());
    }

    //compute by milisecons
    public static long getDateDiff(Date startDate, Date endDate, TimeUnit timeUnit) {
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static long dateToLong(String format, String date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

        Date inputDate;
        try {
            inputDate = simpleDateFormat.parse(date);
            return inputDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static Date stringToDateByForm(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        try {
            date = formatter.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    public static String getHourRange(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String ddMMyyyy = formatter.format(date);
        try {
            Date zeroDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 00:00:00");
            Date oneDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 01:00:00");
            Date twoDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 02:00:00");
            Date threeDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 03:00:00");
            Date fourDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 04:00:00");
            Date fiveDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 05:00:00");
            Date sixDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 06:00:00");
            Date sevenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 07:00:00");
            Date eightDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 08:00:00");
            Date nineDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 09:00:00");
            Date tenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 10:00:00");
            Date elevenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 11:00:00");
            Date twelveDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 12:00:00");
            Date thirteenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 13:00:00");
            Date fourteenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 14:00:00");
            Date fiveteenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 15:00:00");
            Date sixteenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 16:00:00");
            Date seventeenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 17:00:00");
            Date eightteenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 18:00:00");
            Date nineteenDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 19:00:00");
            Date twentyDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 20:00:00");
            Date twentyOneDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 21:00:00");
            Date twentyTwoDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 22:00:00");
            Date twentyThreeDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(ddMMyyyy + " 23:00:00");
            if (date.compareTo(zeroDate) >= 0 && date.compareTo(oneDate) < 0) {
                return "00-01";
            } else if (date.compareTo(oneDate) >= 0 && date.compareTo(twoDate) < 0) {
                return "01-02";
            } else if (date.compareTo(twoDate) >= 0 && date.compareTo(threeDate) < 0) {
                return "02-03";
            } else if (date.compareTo(threeDate) >= 0 && date.compareTo(fourDate) < 0) {
                return "03-04";
            } else if (date.compareTo(fourDate) >= 0 && date.compareTo(fiveDate) < 0) {
                return "04-05";
            } else if (date.compareTo(fiveDate) >= 0 && date.compareTo(sixDate) < 0) {
                return "05-06";
            } else if (date.compareTo(sixDate) >= 0 && date.compareTo(sevenDate) < 0) {
                return "06-07";
            } else if (date.compareTo(sevenDate) >= 0 && date.compareTo(eightDate) < 0) {
                return "07-08";
            } else if (date.compareTo(eightDate) >= 0 && date.compareTo(nineDate) < 0) {
                return "08-09";
            } else if (date.compareTo(nineDate) >= 0 && date.compareTo(tenDate) < 0) {
                return "09-10";
            } else if (date.compareTo(tenDate) >= 0 && date.compareTo(elevenDate) < 0) {
                return "10-11";
            } else if (date.compareTo(elevenDate) >= 0 && date.compareTo(twelveDate) < 0) {
                return "11-12";
            } else if (date.compareTo(twelveDate) >= 0 && date.compareTo(thirteenDate) < 0) {
                return "12-13";
            } else if (date.compareTo(thirteenDate) >= 0 && date.compareTo(fourteenDate) < 0) {
                return "13-14";
            } else if (date.compareTo(fourteenDate) >= 0 && date.compareTo(fiveteenDate) < 0) {
                return "14-15";
            } else if (date.compareTo(fiveteenDate) >= 0 && date.compareTo(sixteenDate) < 0) {
                return "15-16";
            } else if (date.compareTo(sixteenDate) >= 0 && date.compareTo(seventeenDate) < 0) {
                return "16-17";
            } else if (date.compareTo(seventeenDate) >= 0 && date.compareTo(eightteenDate) < 0) {
                return "17-18";
            } else if (date.compareTo(eightteenDate) >= 0 && date.compareTo(nineteenDate) < 0) {
                return "18-19";
            } else if (date.compareTo(nineteenDate) >= 0 && date.compareTo(twentyDate) < 0) {
                return "19-20";
            } else if (date.compareTo(twentyDate) >= 0 && date.compareTo(twentyOneDate) < 0) {
                return "20-21";
            } else if (date.compareTo(twentyOneDate) >= 0 && date.compareTo(twentyTwoDate) < 0) {
                return "21-22";
            } else if (date.compareTo(twentyTwoDate) >= 0 && date.compareTo(twentyThreeDate) < 0) {
                return "22-23";
            } else {
                return "23-24";
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List<Date> getListOfDaysBetweenTwoDates(Date startDate, Date endDate) {
        List<Date> result = new ArrayList<Date>();
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        end.add(Calendar.DAY_OF_YEAR, 1); //Add 1 day to endDate to make sure endDate is included into the final list
        while (start.before(end)) {
            result.add(start.getTime());
            start.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    public static Date fromLocalDateTime(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String getPartitionNameOfNextMonth(String table) {
        String yyyyMmDd = LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth()).toString();
        String[] arr = yyyyMmDd.split("-");
        String year = arr[0];
        String month = arr[1];
        if (month.length() == 1) {
            month = "0" + month;
        }
        return table + "_" + year + "_" + month;
    }

    public static String getPartitionValueOfCurrentMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth()).toString() + " 00:00:00";
    }

    public static String getPartitionValueOfNextMonth() {
        return LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfNextMonth()).toString() + " 00:00:00";
    }

    public static void main(String[] args) {
        try {
            System.out.println("getPartitionValueOfCurrentMonth: " + getPartitionValueOfCurrentMonth());
            System.out.println("getPartitionValueOfNextMonth: " + getPartitionValueOfNextMonth());
            Date date1 = new Date(System.currentTimeMillis());
            Date date2 = new Date(System.currentTimeMillis() - 600000);
            System.out.println(date1 + " | " + date2 + " | Same day : " + isSameDay(date1, date2));
            System.out.println("hour range 1: " + getHourRange(date1));
            System.out.println("hour range 2: " + getHourRange(date2));

            //List date
            String startDate = "2021-02-01";
            String endDate = "2030-01-01";
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            List<Date> dateList = getListOfDaysBetweenTwoDates(df.parse(startDate), df.parse(endDate));
            if (dateList != null && !dateList.isEmpty()) {
                //alter TABLE report_user add PARTITION (PARTITION p_2020_08_25 VALUES LESS THAN (UNIX_TIMESTAMP('2020-08-26 00:00:00')))
                String str = "alter TABLE report_user add PARTITION (PARTITION p_%s VALUES LESS THAN (UNIX_TIMESTAMP('%s 00:00:00')));";
                Date tomorrow = null;
                for (Date date : dateList) {
                    tomorrow = cacularDate(date, 1);
                    df = new SimpleDateFormat("yyyy-MM-dd");
                    System.out.println(String.format(str, df.format(date).replace("-", "_"), df.format(tomorrow)));
                }
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
}
