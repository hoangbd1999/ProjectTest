//package elcom.com.neo4j.service;
//
//import org.apache.flink.types.Row;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.TimeZone;
//
//@Component
//public class ReadValuetoMonth implements MapFunction<Row, Row> {
//    private static final Logger logger = LoggerFactory.getLogger(ReadValuetoMonth.class);
//
//
//    @Override
//    public Row call(Row row) throws Exception {
//        Calendar cal = Calendar.getInstance();
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dateTime = row.getAs("dateTime");
//        try {
//            cal.setTime(df.parse(dateTime));
//            cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//            cal.set(Calendar.DAY_OF_MONTH, 1);
//            cal.set(Calendar.HOUR_OF_DAY,0);
//            cal.clear(Calendar.MINUTE);
//            cal.clear(Calendar.SECOND);
//            cal.clear(Calendar.MILLISECOND);
//            dateTime = df.format(cal.getTime());
//        }catch (Exception ex){
//
//        }
//        return  RowFactory.create(dateTime,
//                row.getAs("idsSrc"),
//                row.getAs("idsDest"),
//                row.getAs("ipsSrc"),row.getAs("longitudeSrc"),
//                row.getAs("latitudeSrc"),row.getAs("nameSrc"),
//                row.getAs("ipsDest"),row.getAs("longitudeDest"),
//                row.getAs("latitudeDest"),row.getAs("nameDest"),
//                row.getAs("WebCount"),row.getAs("WebFileSize"),
//                row.getAs("VoiceCount"),row.getAs("VoiceFileSize"),
//                row.getAs("TransferFileCount"),row.getAs("TransferFileFileSize"),
//                row.getAs("VideoCount"),row.getAs("VideoFileSize"),
//                row.getAs("EmailCount"),row.getAs("EmailFileSize"),
//                row.getAs("UNDEFINEDCount"),row.getAs("UNDEFINEDFileSize"),
//                row.getAs("count"));
//    }
//}
