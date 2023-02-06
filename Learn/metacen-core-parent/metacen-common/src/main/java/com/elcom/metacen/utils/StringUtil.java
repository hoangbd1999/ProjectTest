package com.elcom.metacen.utils;

import com.elcom.metacen.enums.DataSequenceStatus;
import com.elcom.metacen.message.RequestMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.*;
import org.slf4j.LoggerFactory;

public class StringUtil {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static String Empty = "";

    public static String normalizeCoordinatesValue(String input) {
        if( isNullOrEmpty(input) )
            return null;
        return input.startsWith(".") ? "0" + input : input.startsWith("-.") ? "-0." + input.substring(input.indexOf(".") + 1) : input;
    }
    
    public static String printException(Exception ex) {
        return ex.getCause() != null ? ex.getCause().toString() : ex.toString();
    }

    public static Map<String, String> getUrlParamValues(String url) {
        Map<String, String> paramsMap = new HashMap<>();
        String params[] = url.split("&");
        String[] temp;
        for (String param : params) {
            temp = param.split("=");
            try {
                paramsMap.put(temp[0], temp.length > 1 ? java.net.URLDecoder.decode(temp[1], "UTF-8") : "");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }
        return paramsMap;
    }
    
    public static Map<String, String> getUrlParamValuesNoDecode(String url) {
        Map<String, String> paramsMap = new HashMap<>();
        String params[] = url.split("&");
        String[] temp;
        for (String param : params) {
            temp = param.split("=");
            paramsMap.put(temp[0], temp.length > 1 ? temp[1] : "");
        }
        return paramsMap;
    }

    public static String generateRandomString(int num) {
        return RandomStringUtils.randomAlphanumeric(num);
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

    public static boolean isNumeric(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }

        return s.matches("[-+]?\\d*\\.?\\d+");
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

    public static String objectToString(Object input) {
        if( input == null )
            return null;
        try {
            return ((String) input).trim();
        }catch(Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }

    public static Integer objectToInteger(Object input) {
        try {

            if( input instanceof String )
                return Integer.parseInt((String) input);

            return (Integer) input;

        }catch(Exception ex) {
            LOGGER.warn(StringUtil.printException(ex));
        }
        return null;
    }

    public static String getStringFromMap(Object input) {
        if (input == null || input == "") {
            return null;
        } else {
            return (String) input;
        }
    }

    public static Integer getIntegerFromMap(Object input) {
        if (input == null || input == "") {
            return null;
        } else {
            return (Integer) input;
        }
    }

    public static Double getDoubleFromMap(Object input) {
        if (input == null || input == "") {
            return null;
        } else {
            return (Double) input;
        }
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
        return Long.parseLong(input);
    }

    public static int intFromString(String input) {
        if (isNullOrEmpty(input) || !isNumberic(input)) {
            return 0;
        }
        return Integer.parseInt(input);
    }

    public static String currencyFormat(String input) {
        double myNum = Double.parseDouble(input);
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
        return (input == null ? "" : ("null".equals(input) ? "" : input
                .toString()));
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

    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static String generateMapString(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            //builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            try {
                builder.append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String result = builder.toString();
        return result != null && result.endsWith("&") ? result.substring(0, result.length() - 1) : result;
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
            sdf.parse(birthDay);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        //Check number
        String input1 = "123";
        System.out.println(isDigit(input1) + "|" + isNumberic(input1) + "|" + isNumeric(input1));

        String input2 = "123.1";
        System.out.println(isDigit(input2) + "|" + isNumberic(input2) + "|" + isNumeric(input2));

        String input3 = "-123.1";
        System.out.println(isDigit(input3) + "|" + isNumberic(input3) + "|" + isNumeric(input3));

        String json = "{'servers': [], 'detectors': [{'id': 8, 'nms': 0.4, 'hier': 0.5, 'meta': [], 'enable': true, 'net_file': 'traffic.cfg', 'sub_model': 'traffic', 'threshold': 0.7, 'base_model': 'trt-yolov3', 'input_rate': 15, 'max_object': 180000, 'min_object': 300, 'model_name': 'Traffic', 'labels_file': 'traffic.names', 'output_rate': 15, 'output_width': 1024, 'weights_file': 'traffic.weights', 'output_height': 576, 'static_object': 1, 'filter_objects': ['car', 'motorbike', 'bike', 'truck', 'bus', 'constructed']}, {'id': 9, 'nms': 0.4, 'hier': 0.5, 'meta': [], 'enable': true, 'net_file': 'trafficlight.cfg', 'sub_model': 'trafficlight', 'threshold': 0.7, 'base_model': 'trt-resnet18', 'input_rate': 15, 'max_object': 180000, 'min_object': 300, 'model_name': 'Trafficlight', 'labels_file': 'trafficlight.txt', 'output_rate': 15, 'output_width': 1024, 'weights_file': 'trafficlight.engine', 'output_height': 576, 'static_object': 1, 'filter_objects': ['constructed']}], 'input_sources': [{'id': 15, 'urls': {'hls_url': 'http://tmon-media.elcom.com.vn/hls/caotoc.m3u8', 'admin_url': 'http://hobao.ddns.net:8890', 'stream_url': 'rtsp://root:Elcom6789@hobao.ddns.net:8990/axis-media/media.amp'}, 'enable': true, 'layout': {'viewbox': {'h': 576, 'w': 1024, 'x': 0, 'y': 0}, 'roi_list': [{'item': {'id': 29, 'name': 'BKS', 'path': 'M4.942,178.056 L1012.937,175.681 L1013.784,471.075 L4.095,471.867 L4.942,178.056', 'color': {'b': 153, 'g': 255, 'r': 102}, 'roi_type': 9, 'color_str': 'rgba(102,255,153,0.3)', 'direction': 56, 'mid_range': 0, 'type_name': 'NHAN_DIEN_BIEN_SO', 'related_roi': [], 'measure_time': 15, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.8, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.4, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 12000, 'det_ratio': 0.4, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'arr_direction': [8, 16, 32], 'move_direction': 0, 'allow_turnright': false}, 'type': 9}, {'item': {'id': 30, 'info': {'x': 5, 'y': 5}, 'name': 'DEMXE', 'path': 'M28.28,50.688 L29.978,298.368 L956.889,297.216 L956.041,62.208 L28.28,50.688', 'color': {'b': 0, 'g': 0, 'r': 204}, 'period': 15, 'roi_type': 5, 'color_str': 'rgba(204,0,0,0.3)', 'direction': 56, 'mid_range': 11, 'type_name': 'DO_DEM_LUU_LUONG', 'related_roi': [], 'measure_time': 15, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'arr_direction': [8, 16, 32], 'allow_turnright': false}, 'type': 5}]}, 'cache_url': '127.0.0.1', 'detectors': [8, 9], 'capability': {'h': 576, 'w': 1024, 'fps': 25}, 'auto_adjust': 1, 'device_name': '', 'cache_length': 1, 'device_model': '', 'video_output': {'render_url': 'rtmp://192.168.6.170/live/caotoc', 'render_port': '127.0.0.1:10000'}, 'input_attributes': {'camera_key': 'Axis - 5M'}, 'setup_attributes': {'farest_sp': '12', 'nearest_sp': '21', 'focal_length': '11-19', 'installation_height': '12'}}, {'id': 100043, 'urls': {'hls_url': 'http://tmon-media.elcom.com.vn/hls/luongna.m3u8', 'admin_url': 'http://hobao.ddns.net:8890', 'stream_url': 'file:///va/videos/luongna.mp4'}, 'enable': true, 'layout': {'viewbox': {'h': 768, 'w': 1024, 'x': 0, 'y': 0}, 'roi_list': [{'item': {'id': 470, 'info': {'x': 0, 'y': 0}, 'name': 'LUU LUONG', 'path': 'M2.209,624.48 L1024.575,630.986 L1023.508,361.662 L1.142,364.264 L2.209,624.48', 'color': {'b': 0, 'g': 0, 'r': 204}, 'roi_type': 5, 'color_str': 'rgba(204,0,0,0.3)', 'direction': 131, 'mid_range': 3, 'type_name': 'DO_DEM_LUU_LUONG', 'related_roi': [], 'measure_time': 1, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'motorbike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'constructed', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'arr_direction': [1, 2, 128], 'move_direction': 0}, 'type': 5}, {'item': {'id': 471, 'name': 'BIEN SO', 'path': 'M2.209,710.425 L1026.71,710.425 L1026.71,285.102 L2.209,289.398 L2.209,710.425', 'color': {'b': 255, 'g': 0, 'r': 204}, 'roi_type': 9, 'color_str': 'rgba(204,0,255,0.3)', 'direction': 191, 'mid_range': 0, 'type_name': 'NHAN_DIEN_BIEN_SO', 'related_roi': [], 'measure_time': 15, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.95, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.9, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 12000, 'det_ratio': 0.9, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'motorbike', 'min_area': 9000, 'det_ratio': 0.98, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'constructed', 'min_area': 9000, 'det_ratio': 0.9, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'arr_direction': [1, 2, 128, 4, 8, 16, 32], 'move_direction': 0}, 'type': 9}, {'item': {'id': 474, 'info': {'x': 200, 'y': 5}, 'name': 'VI PHAM DEN DO', 'path': 'M239.51,461.188 L836.068,467.694 L880.89,563.974 L168.008,557.468 L239.51,461.188', 'color': {'b': 255, 'g': 0, 'r': 51}, 'roi_type': 8, 'color_str': 'rgba(51,0,255,0.3)', 'direction': 131, 'mid_range': 0, 'type_name': 'VI_PHAM_DEN_TIN_HIEU', 'related_roi': [], 'measure_time': 1, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'motorbike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'constructed', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'arr_direction': [1, 2, 128], 'move_direction': 0, 'allow_turnright': false}, 'type': 8}, {'item': {'id': 479, 'name': 'HUONG', 'path': 'M289.144,172.101 L799.687,170.149 L934.579,413.126 L35.58,412.15 L289.144,172.101', 'color': {'b': 136, 'g': 136, 'r': 136}, 'roi_type': 16, 'color_str': 'rgba(136,136,136,0.3)', 'direction': 0, 'mid_range': 0, 'type_name': 'HUONG_DI_CHUYEN', 'related_roi': [], 'measure_time': 15, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'motorbike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'move_direction': 0}, 'type': 16}, {'item': {'id': 480, 'name': 'DEN_DO', 'path': 'M932.648,65.009 L933.715,183.407 L967.865,182.106 L966.798,66.31 L932.648,65.009', 'color': {'b': 0, 'g': 204, 'r': 255}, 'roi_type': 7, 'color_str': 'rgba(255,204,0,0.3)', 'direction': 0, 'mid_range': 0, 'type_name': 'TRANG_THAI_DEN_GIAO_THONG', 'related_roi': [474], 'measure_time': 15, 'affect_object': [], 'move_direction': 0}, 'type': 7}, {'item': {'id': 476, 'info': {'x': 500, 'y': 5}, 'name': 'VI PHAM NGUOC CHIEU', 'path': 'M276.097,389.629 L868.371,392.231 L974.836,629.028 L121.052,623.824 L276.097,389.629', 'color': {'b': 0, 'g': 0, 'r': 153}, 'roi_type': 1, 'color_str': 'rgba(153,0,0,0.3)', 'direction': 131, 'mid_range': 0, 'type_name': 'NGUOC_CHIEU', 'related_roi': [], 'measure_time': 1, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'motorbike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'constructed', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'arr_direction': [1, 2, 128], 'move_direction': 0}, 'type': 1}, {'item': {'id': 477, 'info': {'x': 700, 'y': 5}, 'name': 'LAN OTO', 'path': 'M584.189,471.597 L886.382,472.898 L938.521,575.684 L576.741,573.081 L584.189,471.597', 'color': {'b': 0, 'g': 204, 'r': 102}, 'roi_type': 18, 'sub_type': 0, 'color_str': 'rgba(102,204,0,0.3)', 'direction': 0, 'mid_range': 0, 'threshold': 1, 'type_name': 'SAI_LAN', 'related_roi': [], 'measure_time': 15, 'affect_object': [{'label': 'car', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'truck', 'min_area': 14000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bus', 'min_area': 12000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'bike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}, {'label': 'constructed', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'move_direction': 0}, 'type': 18}, {'item': {'id': 478, 'info': {'x': 0, 'y': 710}, 'name': 'LAN XE MAY', 'path': 'M240.043,463.146 L583.677,467.049 L575.14,575.039 L169.608,565.932 L240.043,463.146', 'color': {'b': 255, 'g': 0, 'r': 255}, 'roi_type': 18, 'sub_type': 0, 'color_str': 'rgba(255,0,255,0.3)', 'direction': 0, 'mid_range': 0, 'threshold': 1, 'type_name': 'SAI_LAN', 'related_roi': [], 'measure_time': 15, 'affect_object': [{'label': 'motorbike', 'min_area': 9000, 'det_ratio': 0.3, 'direction': 0, 'max_ratio': 6, 'min_ratio': 0.3}], 'move_direction': 0}, 'type': 18}]}, 'cache_url': '127.0.0.1', 'detectors': [8, 9], 'capability': {'h': 768, 'w': 1024, 'fps': 15}, 'auto_adjust': 1, 'device_name': '', 'cache_length': 1, 'device_model': '', 'video_output': {'render_url': 'rtmp://192.168.6.170/live/luongna', 'render_port': '127.0.0.1:10000'}, 'input_attributes': {'camera_key': 'Axis 1376 Vinh'}, 'setup_attributes': {'farest_sp': '12', 'nearest_sp': '21', 'focal_length': '11-19', 'installation_height': '12'}}], 'event_notifiers': [{}]}";
        System.out.println("is json : " + isJSONValid(json));

        String urlParam = "currentPage=1&rowsPerPage=20&keyword=+";
        Map<String, String> paramsMap = new HashMap<>();
        String params[] = urlParam.split("&");
        String[] temp;
        for (String param : params) {
            temp = param.split("=");
            try {
                paramsMap.put(temp[0], temp.length > 1 ? java.net.URLDecoder.decode(temp[1], "UTF-8") : "");
                System.out.println(temp[0] + " ==> " + temp[1] + " => decode: " + java.net.URLDecoder.decode(temp[1], "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }

        String jsonRequest = "{\"requestMethod\":\"GET\",\"requestPath\":\"/v1.0/user/cms\",\"version\":\"/v1.0\",\"urlParam\":\"currentPage\\u003d1\\u0026rowsPerPage\\u003d20\\u0026sort\\u003dcreatedAt\\u0026keyword\\u003d+\\u0026\",\"headerParam\":{\"authorization\":\"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkZDYwOWE0ZC0zYWZmLTRlZWYtYTUzYy05ZmE3NmQ2NjlhMDMiLCJpYXQiOjE2MjM2NDUxNjUsImV4cCI6MTYyNDI0OTk2NX0.d1PSG8CSsJsxLQMFg-f76-SkqPPAQJzNOiqqp0hhUA-opjrugh6R7Ir3EMFS-xJ1HAdta9jsAQyPlLzZHk1jdA\",\"user-agent\":\"PostmanRuntime/7.26.8\",\"accept\":\"*/*\",\"postman-token\":\"50d930f4-281a-49f0-b3ec-d6e1080ab291\",\"host\":\"192.168.51.34:8685\",\"accept-encoding\":\"gzip, deflate, br\",\"connection\":\"keep-alive\",\"cookie\":\"JSESSIONID\\u003d5DB370D7AEE647A151477C60B9779252\",\"platform\":\"WEB\",\"ip-address\":\"192.168.9.108\"}}";
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        RequestMessage request = mapper.readValue(jsonRequest, RequestMessage.class);
        System.out.println(request.getUrlParam());
    }

    public static Map<String, Object> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, Object> map = new HashMap<String, Object>();
        List<String> paramsList = Arrays.asList(params);
        for (String param : paramsList) {
            List<String> paramSplit = Arrays.asList(param.split("="));
            if (paramSplit.size() > 1) {
                String name = paramSplit.get(0);
                Object value = paramSplit.get(1);
                map.put(name, value);
            } else {
                String name = paramSplit.get(0);
                map.put(name, null);
            }

        }
        return map;
    }
    public static String convertObject(DataSequenceStatus key, Long number){
        String result = null;
        DecimalFormat df = new DecimalFormat("#000000");
        if(key.equals(DataSequenceStatus.P)) {
            result = key + df.format(number);
        } else if (key.equals(DataSequenceStatus.O)) {
            result = key + df.format(number);
        } else if (key.equals(DataSequenceStatus.V)) {
            result = key + df.format(number);
        } else if (key.equals(DataSequenceStatus.E)) {
            result = key + df.format(number);
        } else if (key.equals(DataSequenceStatus.I)) {
            result = key + df.format(number);
        } else if (key.equals(DataSequenceStatus.A)) {
            result = key + df.format(number);
        } else if (key.equals(DataSequenceStatus.R)) {
            result = key + df.format(number);
        } else if (key.equals(DataSequenceStatus.D)) {
            result = key + df.format(number);
        }
        return result;
    }



    
    public static String replaceSpecialSQLCharacter(String keyword){
        if(keyword == null) return null;
        return keyword.replace("\\", "\\\\").replace("_", "\\_").replace("%", "\\%").replace("'", "''");
    }
}
