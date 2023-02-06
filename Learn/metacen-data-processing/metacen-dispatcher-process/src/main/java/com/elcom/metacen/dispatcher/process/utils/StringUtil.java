package com.elcom.metacen.dispatcher.process.utils;

import com.elcom.metacen.dispatcher.process.model.dto.DataProcessConfig;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
    
    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static String Empty = "";

//    public static void main(String[] args) {
//        List<DataProcessConfig> dataProcessConfigs;
//        try {
//            
//            String result = "{\"status\":200,\"message\":\"200 OK\",\"data\":{\"status\":200,\"message\":\"200 OK\",\"data\":[{\"id\":\"635b30639b933e1adcff9f66\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 08:29:07\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-28 08:39:10\",\"uuid\":\"fa9ddb8a-26c1-4f8c-aff8-d27e856a8ca9\",\"name\":null,\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"\",\"detailConfig\":{\"source_ip\":\"1.1.1.1\",\"dest_ip\":\"1.1.1.2\",\"source_id\":\"94939\",\"data_type\":\"Audio, Web\",\"format\":\".mp3, .amr, .docx, .xls\"},\"status\":0,\"startTime\":null,\"endTime\":null},{\"id\":\"635b41979b933e1adcff9f69\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 09:42:31\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 09:42:31\",\"uuid\":\"c1d4433d-e0e9-48a4-8854-b73b59834f2b\",\"name\":null,\"dataType\":\"AIS\",\"processType\":\"FUSION\",\"dataVendor\":\"AIS-01\",\"detailConfig\":{\"mmsi\":\"66735206,66735207,66735210\"},\"status\":1,\"startTime\":null,\"endTime\":null},{\"id\":\"635b43d79b933e1adcff9f6b\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 09:52:07\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 09:52:07\",\"uuid\":\"27d7daf5-6718-4839-b457-cd6c93231f42\",\"name\":null,\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"10.2.2.3\",\"dest_ip\":\"10.2.2.3\",\"source_id\":\"94932\",\"data_type\":\"\",\"format\":\"\"},\"status\":0,\"startTime\":\"2022-10-19 00:00:00\",\"endTime\":\"2022-10-29 00:00:00\"},{\"id\":\"635b5bc89b933e1adcff9f71\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 11:34:16\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 11:34:16\",\"uuid\":\"688b5647-4125-4fb4-8698-0270a8bbd56b\",\"name\":null,\"dataType\":\"VSAT\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"10.2.2.3\",\"dest_ip\":\"10.2.2.3\",\"source_id\":\"94938\",\"data_type\":\"Email\",\"format\":\".POP3\"},\"status\":1,\"startTime\":\"2022-10-28 00:00:00\",\"endTime\":\"2022-10-29 00:00:00\"},{\"id\":\"635b75069b933e1adcff9f72\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 13:21:58\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-28 13:22:27\",\"uuid\":\"b5fdb7e8-05e2-4af9-99c4-60115e843d05\",\"name\":null,\"dataType\":\"VSAT\",\"processType\":\"FUSION\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"10.2.2.3\",\"dest_ip\":\"10.2.2.3\",\"source_id\":\"94937\",\"data_type\":\"Web\",\"format\":\".doc\"},\"status\":1,\"startTime\":\"2022-10-29 00:00:00\",\"endTime\":\"2022-10-31 00:00:00\"},{\"id\":\"635b7c119b933e1adcff9f73\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 13:52:01\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 13:52:01\",\"uuid\":\"5c162acf-f9fb-4506-86ac-3b9b38af5e72\",\"name\":null,\"dataType\":\"AIS\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"AIS-01\",\"detailConfig\":{\"mmsi\":\"120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138\"},\"status\":1,\"startTime\":\"2022-10-20 00:00:00\",\"endTime\":\"2022-10-28 00:00:00\"},{\"id\":\"635b7d219b933e1adcff9f74\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-28 13:56:33\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-28 13:56:33\",\"uuid\":\"2e308e80-4a39-4ccf-ab14-88a18301ad65\",\"name\":null,\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"1.1.1.1,1.1.1.2,1.1.1.3,1.1.1.4,1.1.1.5,1.1.1.6,1.1.1.7,1.1.1.8,1.1.1.9,1.1.1.10,1.1.1.11,1.1.1.12,1.1.1.13,1.1.1.14,1.1.1.15,1.1.1.16,1.1.1.17,1.1.1.18,1.1.1.19\",\"dest_ip\":\"1.1.2.1,1.1.2.2,1.1.2.3,1.1.2.4,1.1.2.5,1.1.2.6,1.1.2.7,1.1.2.8,1.1.2.9,1.1.2.10,1.1.2.11,1.1.2.12,1.1.2.13,1.1.2.14,1.1.2.15,1.1.2.16,1.1.2.17,1.1.2.18,1.1.2.19\",\"source_id\":\"94939\",\"data_type\":\"Audio, Video, Web, Email, Transfer file, Undefined\",\"format\":\".mp3, .wav, .amr, .g711u, .g711a, .g711mu, .g722, .g7221, .g723, .g7231, .g728, .g729, .g719, .mp4, .ts, .h263, .h263+, .h264, .H264MC, .H264 polycom, .html, .txt, .json, .doc, .docx, .xls, .xlsx, .pdf, .pptx, .git, .3GP, .WMV, .AVI, .MJPEG, .mov, .flv, .POP3, .SMTP, .IMAP4, .Coremail, .UDP, .TCP, .AH, .eml, .ppt, .tiff, .zip, .rar, .jpeg, .jpg, .png, .xml, .pcap, .ESP, .bmp, .g726, .g727\"},\"status\":0,\"startTime\":\"2022-10-28 00:00:00\",\"endTime\":\"2022-10-29 00:00:00\"},{\"id\":\"635f772e7b467679fb99e73d\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-31 14:20:14\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-31 14:26:12\",\"uuid\":\"eece1444-37b1-472d-b5f2-018a7c226be8\",\"name\":null,\"dataType\":\"SATELLITE\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"108.0081505923 19.691212513, 108.1876477199 17.6312492904, 112.0727126347 17.5599636956, 111.8048926552 19.7699830952, 108.0081505923 19.691212513\"},\"status\":0,\"startTime\":\"2022-10-31 00:00:00\",\"endTime\":\"2022-12-10 00:00:00\"},{\"id\":\"635f77497b467679fb99e73e\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-31 14:20:41\",\"modifiedBy\":null,\"modifiedDate\":\"2022-10-31 14:20:41\",\"uuid\":\"a626dc8f-7e5c-43bd-9051-d1eb8f3b7acb\",\"name\":null,\"dataType\":\"SATELLITE\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"109.9124230019 20.3323158426, 109.9124230019 20.3062106721, 109.9497161028 20.3062106721, 109.9444950687 20.3338075667, 109.9124230019 20.3323158426\"},\"status\":0,\"startTime\":\"2022-10-31 00:00:00\",\"endTime\":\"2022-12-10 00:00:00\"},{\"id\":\"635f7951e0d5477a5ccb916c\",\"createdBy\":\"admin\",\"createdDate\":\"2022-10-31 14:29:21\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-10-31 14:54:22\",\"uuid\":\"bc956220-ed9c-46db-965b-bdc418ad4302\",\"name\":\"vsat b\",\"dataType\":\"VSAT\",\"processType\":\"FUSION\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"10.20.61.11\",\"dest_ip\":\"10.78.90.98\",\"source_id\":\"987898,94939\",\"data_type\":\"Web, Email\",\"format\":\".html, .eml\"},\"status\":1,\"startTime\":\"2022-10-19 00:00:00\",\"endTime\":\"2022-10-19 00:00:00\"},{\"id\":\"63636e46a5cea105c87f69f9\",\"createdBy\":\"admin\",\"createdDate\":\"2022-11-03 14:31:18\",\"modifiedBy\":null,\"modifiedDate\":\"2022-11-03 14:31:18\",\"uuid\":\"cc4ef858-9203-4af6-a2ed-9d5fd6bcc96f\",\"name\":\"\",\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"1.1.1.1\",\"dest_ip\":\"1.1.1.2\",\"source_id\":\"94543,94545,94511\",\"data_type\":\"Audio, Video, Web, Email, Transfer file, Undefined\",\"format\":\".mp3, .wav, .amr, .g711a, .g711u\"},\"status\":0,\"startTime\":\"2022-11-01 00:00:00\",\"endTime\":\"2022-11-03 00:00:00\"},{\"id\":\"636370eba5cea105c87f69fa\",\"createdBy\":\"admin\",\"createdDate\":\"2022-11-03 14:42:35\",\"modifiedBy\":null,\"modifiedDate\":\"2022-11-03 14:42:35\",\"uuid\":\"89a2977b-ff6d-47f0-9c8a-55affb9509d8\",\"name\":\"Tên cấu hình - huongpt\",\"dataType\":\"AIS\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"AIS-01\",\"detailConfig\":{\"mmsi\":\"16816293,66668888\"},\"status\":1,\"startTime\":\"2022-11-01 00:00:00\",\"endTime\":\"2022-11-15 00:00:00\"},{\"id\":\"63637443a5cea105c87f69fb\",\"createdBy\":\"admin\",\"createdDate\":\"2022-11-03 14:56:51\",\"modifiedBy\":null,\"modifiedDate\":\"2022-11-03 14:56:51\",\"uuid\":\"9e54c2b4-5834-4eb5-b5d5-7c1b81a34eea\",\"name\":\"Medatec1\",\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"1.1.1.1,1.1.1.2,1.1.1.3,1.1.1.4\",\"dest_ip\":\"9.9.9.1,9.9.9.2,9.9.9.3\",\"source_id\":\"94634,94633\",\"data_type\":\"Audio\",\"format\":\".mp3, .wav\"},\"status\":0,\"startTime\":\"2022-11-01 00:00:00\",\"endTime\":\"2022-11-10 00:00:00\"},{\"id\":\"636374cea5cea105c87f69fc\",\"createdBy\":\"admin\",\"createdDate\":\"2022-11-03 14:59:10\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-11-03 15:14:26\",\"uuid\":\"4657e6e9-6738-49cf-8379-582b8ed67002\",\"name\":\"Minh Vy 01\",\"dataType\":\"SATELLITE\",\"processType\":\"FUSION\",\"dataVendor\":\"SATELLITE-IMAGE-01\",\"detailConfig\":{\"coordinates\":\"110.6653764466 20.1404093026, 110.7285375441 20.0675311133, 110.7795522766 20.0116578348, 110.8621475578 19.992223651, 110.9277379282 20.0140871078, 111.0346259392 19.6351205233, 110.9641770229 19.6254034314, 110.8572890119 19.5185154204, 110.8232791902 19.5379496042, 110.6775228115 19.353324858, 110.6143617141 19.2172855712, 110.5706348005 19.1784172036, 110.5099029761 19.1589830198, 110.4977566112 18.9039093572, 110.5341957059 18.7921628002, 110.3738636894 18.6877040622, 110.2426829486 18.6536942405, 109.657228161 19.1274024711, 109.66451598 19.3946224986, 110.0696804501 19.6965290333, 110.1354997859 20.059521666, 110.3697118209 20.068359856, 110.5067037658 20.028588001, 110.5553138108 20.037426191, 110.5729901908 20.108131711, 110.6653764466 20.1404093026\"},\"status\":1,\"startTime\":\"2022-11-01 00:00:00\",\"endTime\":\"2022-12-01 00:00:00\"},{\"id\":\"63638813a5cea105c87f69fd\",\"createdBy\":\"admin\",\"createdDate\":\"2022-11-03 16:21:23\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-11-04 10:16:17\",\"uuid\":\"455a169c-df0a-483b-aedb-cd5a74db05a8\",\"name\":\"anhdv - test cấu hình media\",\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"1.2.2.2,1.3.3.3,1.4.4.4\",\"dest_ip\":\"5.5.5.1,5.5.5.2\",\"source_id\":\"94913,94846,94945\",\"data_type\":\"Web\",\"format\":\"\"},\"status\":1,\"startTime\":\"2022-11-01 00:00:00\",\"endTime\":\"2022-11-03 00:00:00\"},{\"id\":\"63638effa5cea105c87f69fe\",\"createdBy\":\"admin\",\"createdDate\":\"2022-11-03 16:50:55\",\"modifiedBy\":null,\"modifiedDate\":\"2022-11-03 16:50:55\",\"uuid\":\"bd008498-e4d9-4737-bdb4-92b09fc64c4b\",\"name\":\"hhhhh\",\"dataType\":\"VSAT\",\"processType\":\"VSAT_MEDIA_ANALYTICS\",\"dataVendor\":\"VSAT-01\",\"detailConfig\":{\"source_ip\":\"1.1.1.1,1.1.1.4\",\"dest_ip\":\"1.1.1.2,1.1.4.8\",\"source_id\":\"94939\",\"data_type\":\"Video\",\"format\":\".mp4\"},\"status\":0,\"startTime\":\"2022-11-01 00:00:00\",\"endTime\":\"2022-11-02 00:00:00\"},{\"id\":\"63639325a5cea105c87f69ff\",\"createdBy\":\"admin\",\"createdDate\":\"2022-11-03 17:08:37\",\"modifiedBy\":\"admin\",\"modifiedDate\":\"2022-11-03 17:08:59\",\"uuid\":\"45052762-1154-4150-b963-a6f00745684e\",\"name\":\"anhdv test cấu hình xử lý ảnh vệ tinh cho vùng Hoàng Sa\",\"dataType\":\"SATELLITE\",\"processType\":\"SATELLITE_ANALYTICS\",\"dataVendor\":\"\",\"detailConfig\":{\"coordinates\":\"109.9000740641 16.8532021309, 110.5921693674 14.6384971604, 114.1564601793 14.2924495087, 115.2292078994 17.510692669, 112.6684552773 19.0679071014, 110.0730978899 17.8221355555, 109.9000740641 16.8532021309\"},\"status\":1,\"startTime\":null,\"endTime\":null}]}}";
//            
//            ObjectMapper om = new ObjectMapper();
//            om.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
//            
//            ResponseMessage response;
//            try {
//                response = om.readValue(result, ResponseMessage.class);
//            } catch (Exception ex) {
//                System.err.println("ProcessConfig -> json parse err(1): " + printException(ex));
//                return;
//            }
//            if( response == null || response.getStatus() != HttpStatus.OK.value() || response.getData() == null || response.getData().getData() == null ) {
//                System.err.println("ProcessConfig -> json parse err(2): " + response);
//                return;
//            }
//            
//            dataProcessConfigs = om.readValue(JSONConverter.toJSON(response.getData().getData()), new TypeReference<List<DataProcessConfig>>(){});
//            
//            List<DataProcessConfig> dataProcessConfigs2 = filterConfigProcessForVsatMedia(dataProcessConfigs);
//            
//            LOGGER.info("dataProcessConfigs: {}", JSONConverter.toJSON(dataProcessConfigs));
//            
//            LOGGER.info("dataProcessConfigs2: {}", JSONConverter.toJSON(dataProcessConfigs2));
//            
//        } catch (Exception ex) {
//            System.err.println("ProcessConfig -> json parse err(3): " + printException(ex));
//            return;
//        }
//    }
    
    private static List<DataProcessConfig> filterConfigProcessForVsatMedia(List<DataProcessConfig> dataProcessConfigs) {
        
        if( dataProcessConfigs == null || dataProcessConfigs.isEmpty() )
            return null;
        
        Predicate<DataProcessConfig> streamsPredicate = dataProcessConfig
                -> ( "VSAT".equals(dataProcessConfig.getDataType()) && "VSAT_MEDIA_ANALYTICS".equals(dataProcessConfig.getProcessType())
                && dataProcessConfig.getDetailConfig()!= null && dataProcessConfig.getStatus() != null && dataProcessConfig.getStatus() == 1 );

        return dataProcessConfigs.stream()
                .filter(streamsPredicate)
                .collect(Collectors.toList());
    }
    
    public static boolean validIpV4(String ip) {
        return ip.matches("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    }
    
    public static String printException(Exception ex) {
        return ex.getCause()!=null ? ex.getCause().toString() : ex.toString();
    }
    
    public static Long stringToLong(String input) {
        try {
            return Long.parseLong(input);
        }catch(NumberFormatException ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
    
    public static Integer stringToInteger(String input) {
        try {
            return Integer.parseInt(input);
        }catch(NumberFormatException ex) {
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
    
    public static Long objectToLong(Object input) {
        try {
            return (Long) input;
        }catch(Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
    
    public static boolean validUuid(String uuid) {
        if(uuid==null) return false;
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return Pattern.matches(regex, uuid);
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
            } catch (Exception ex) {
                LOGGER.error("ex: ", ex);
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
            sdf.parse(birthDay);
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
