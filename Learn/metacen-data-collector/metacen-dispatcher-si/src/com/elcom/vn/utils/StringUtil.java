package com.elcom.vn.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static String Empty = "";
    
    public static void main(String[] args) {
        
        System.out.println("res: "+getSaltString(16));
        
        System.out.println("res: "+generateMmsiBaseOnWords("BYZR", 14));
        System.out.println("res: "+generateMmsiBaseOnWords("BYZQ", 14));
        
        System.out.println("res: "+generateMmsiBaseOnWords("BYZR", 4) +"-"+ ipToLong("10.64.20.11"));
        System.out.println("res: "+generateMmsiBaseOnWords("BYZQ", 4) +"-"+ ipToLong("10.64.20.11"));
    }
    
    public static String getSaltString(int maxLength) {
        final String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < maxLength) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
    
    public static String generateMmsiBaseOnWords(String input, int maxLengthReturn) {
        if( input == null || "".equals(input) )
            return "";
        input = input.toLowerCase().replace(" ", "").trim();
        String result = "";
        try {
            final String alphabet = "abcdefghijklmnopqrstuvwxyz";
            char c;
            int idx;
            for ( int i = 0; i < input.length(); i++ ) {
                c = input.charAt(i);
                if( isNumeric(c + "") )
                    result += c;
                else {
                    idx = alphabet.indexOf(c) + 1;
                    if( idx > 0 )
                        result += idx;
                    else
                        result += (int) c;
                }
            }
            if( result.length() > maxLengthReturn )
                result = result.substring(0, maxLengthReturn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static long ipToLongBitShift(String ipAddress) {
        long result = 0;
        try {
            String[] ipAddressInArray = ipAddress.trim().split("\\.");
            for (int i = 3; i >= 0; i--) {
                long ip = Long.parseLong(ipAddressInArray[3 - i]);
                // left shifting 24,16,8,0 and bitwise OR
                // 1. 192 << 24
                // 1. 168 << 16
                // 1. 1 << 8
                // 1. 2 << 0
                result |= ip << (i * 8);
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static long ipToLong(String ipString) {
        try {
            InetAddress a = InetAddress.getByName(ipString.trim());
            byte[] bytes = a.getAddress();
            return new BigInteger(1, bytes).longValue();
        }catch (Exception e) {
            return 0;
        }
    }
    
    public static int calculateCountryId(String mmsiInput) {
        mmsiInput = mmsiInput.trim().substring(0, 3);
        long firstDigit = Long.parseLong(mmsiInput.charAt(0)+"");
        if( firstDigit >= 2 && firstDigit <= 7 )
            return Integer.parseInt(mmsiInput);
        return 0;
    }
    
    public static boolean isValidMmsiWith10Digit(String mmsiInput) {
        
        if( isNullOrEmpty(mmsiInput) )
            return false;
        
        long firstDigit = Long.parseLong(mmsiInput.trim().charAt(0)+"");
        return firstDigit >= 2 && firstDigit <= 7;
    }
    
    public static boolean isNumeric(String sNumber) {
        if( isNullOrEmpty(sNumber) )
            return false;
        
        return sNumber.trim().matches("[-+]?\\d*\\.?\\d+");
    }
    
    public static String printException(Exception ex) {
        return ex.getCause()!=null ? ex.getCause().toString() : ex.toString();
    }
    
    public static Map<String, String> getUrlParamValues(String url) {
        Map<String, String> paramsMap = new HashMap<>();
        String params[] = url.split("&");
        String[] temp;
        for (String param : params) {
            temp = param.split("=");
            try {
                //paramsMap.put(temp[0], java.net.URLDecoder.decode(temp[1], "UTF-8"));
                paramsMap.put(temp[0], temp.length > 1 ? java.net.URLDecoder.decode(temp[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return paramsMap;
    }

    public static String putArrayStringIntoParameter(String input) {

        if (isNullOrEmpty(input)) {
            return "";
        }

        String output = input.substring(0, input.length() - 1).replaceAll(",", "','");

        return "('" + output + "')";
    }

    public static String getComputerName() {
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (Throwable ex) {
            try {
                Map<String, String> env = System.getenv();
                if (env.containsKey("COMPUTERNAME")) {
                    return env.get("COMPUTERNAME");
                } else if (env.containsKey("HOSTNAME")) {
                    return env.get("HOSTNAME");
                } else {
                    return "Unknown";
                }
            } catch (Exception e) {
                return "Unknown";
            }
        }
    }

    public static boolean equalsIgnoreCase(String input, String input1) {
        if (input == null && input1 == null) {
            return true;
        }

        if (input == null && input1 != null) {
            return false;
        }

        if (input != null && input1 == null) {
            return false;
        }

        if (input.equalsIgnoreCase(input1)) {
            return true;
        }

        return false;
    }

    public static boolean isNullOrEmpty(String input) {

        return input == null || input.trim().isEmpty();
    }

    public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }

    

    public static boolean isDigit(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }

        return s.matches("\\d+");
    }

    public static String consolidate(String s) {
        if (isNullOrEmpty(s)) {
            return Empty;
        } else {
            s = s.trim();
            return s;
        }
    }

    public static String toLiteral(String str) {
        if (str == null || str.isEmpty() || str.trim().isEmpty()) {
            return "''";
        } else {
            return "'" + str + "'";
        }
    }

    public static String consolidate(String s, String outValue) {
        if (isNullOrEmpty(s)) {
            return outValue;
        } else {
            s = s.trim();
            return s;
        }
    }

    public static boolean isNotContainSpecialCharator(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }

        return s.matches("^[a-zA-Z0-9]*$");
    }

    public static Integer toInt(String s) throws Exception {
        if (isNullOrEmpty(s)) {
            throw new Exception("Input is required.");
        }

        try {
            return Integer.valueOf(s.trim());
        } catch (Exception ex) {
            throw new Exception("Input is invalid format.");
        }
    }

    public static Long toLong(String input) {
        if (isNullOrEmpty(input) || !isNumeric(input)) {
            return null;
        }
        return Long.parseLong(input.trim());
    }

    public static int intFromString(String input) {
        if (isNullOrEmpty(input) || !isNumberic(input))
            return 0;
        
        return Integer.parseInt(input.trim());
    }

    public static String currencyFormat(String input) {
        
        if( isNullOrEmpty(input) )
            return null;
        
        double myNum = Double.parseDouble(input.trim());
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        try {
            return nf.format(myNum).replace("$", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String ConvertFromFloatingPointToInt(String disbursementAmount) {

        BigDecimal bd = new BigDecimal(disbursementAmount);
        bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd.stripTrailingZeros().toPlainString();

    }

    public static boolean validLength(String field, int maxLength) {
        if (!isNullOrEmpty(field) && field.length() > maxLength) {
            return false;
        }
        return true;
    }

    public static String ConvertFromFloatingPoint(String disbursementAmount,
            int scale) {

        BigDecimal bd = new BigDecimal(disbursementAmount);
        bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return bd.stripTrailingZeros().toPlainString();

    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static BigDecimal toBigDecimal(String input) {
        return toBigDecimal(input, 0, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal toBigDecimal(String input, int roundingMode) {
        return toBigDecimal(input, 0, roundingMode);
    }

    public static BigDecimal toBigDecimal(String input, int scale,
            int roundingMode) {
        BigDecimal output = new BigDecimal(input);
        output.setScale(scale, roundingMode);
        return output;
    }

    public static String toCurrency(String amount) {
        return String.format("%,.0f", Double.valueOf(amount));
    }

    public static String nullToEmpty(Object input) {
        return (input == null ? "" : ("null".equals(input) ? "" : input.toString()));
    }

    public static boolean isNumberic(String sNumber) {
        if (sNumber == null || "".equals(sNumber)) {
            return false;
        }
        char ch_max = (char) 0x39;
        char ch_min = (char) 0x30;

        for (int i = 0; i < sNumber.length(); i++) {
            char ch = sNumber.charAt(i);
            if ((ch < ch_min) || (ch > ch_max)) {
                return false;
            }
        }
        return true;
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    public static boolean validateEmail(String emailStr) {
        if (isNullOrEmpty(emailStr)) {
            return false;
        }
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean checkMobilePhoneNumber(String number) {
        if (number == null) {
            return false;
        }

        boolean result = false;

        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(number);

        if (matcher.matches() && (number.length() == 10 || number.length() == 11)) {
            number = number.substring(0, 2);
            if (number.equals("01") || number.equals("02") || number.equals("08") || number.equals("09")) {
                result = true;
            }
        }

        return result;
    }

    public static boolean checkMobilePhoneNumberNew(String number) {
        if (isNullOrEmpty(number)) {
            return false;
        }
        if(number.startsWith("+84")) number = number.replace("+84", "0");
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(number);

        if (matcher.matches() && (number.length() == 10)) {
            if ("09".equals(number.substring(0, 2)) || Arrays.asList(new String[]{"032", "033", "034", "035", "036", "037", "038", "039", "052", "056", "058", "059", "070", "076", "077", "078", "079", "081", "082", "083", "084", "085", "086", "088", "089"}).contains(number.substring(0, 3))) {
                return true;
            }
        }

        return false;
    }

    public static String generateMcCustCode(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static boolean isUUID(String string) {
        if (isNullOrEmpty(string)) {
            return false;
        }
        try {
            UUID.fromString(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean validateBirthDay(String birthDay, String dateFormat) {
        if (isNullOrEmpty(birthDay)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(birthDay.trim());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

//    public static void main(String[] args) {
//        //Check number
//        String input1 = "123";
//        LOGGER.info(isDigit(input1) + "|" + isNumberic(input1) + "|" + isNumeric(input1));
//        
//        String input2 = "123.1";
//        LOGGER.info(isDigit(input2) + "|" + isNumberic(input2) + "|" + isNumeric(input2));
//        
//        String input3 = "-123.1";
//        LOGGER.info(isDigit(input3) + "|" + isNumberic(input3) + "|" + isNumeric(input3));
//    }
}
