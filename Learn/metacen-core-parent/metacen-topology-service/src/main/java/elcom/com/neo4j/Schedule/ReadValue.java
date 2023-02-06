//package elcom.com.neo4j.Schedule;
//
//import org.apache.spark.api.java.function.MapFunction;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.RowFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//@Component
//public class ReadValue implements MapFunction<Row, Row> {
//    private static final Logger logger = LoggerFactory.getLogger(ReadValue.class);
//
//    @Override
//    public Row call(Row row) throws Exception {
//        String source_id = row.getAs("source_id");
//        String object_type = row.getAs("object_type");
//        String color = row.getAs("color");
//        String id = row.getAs("id");
//        Date start_time = row.getAs("start_time");
//        String brand = row.getAs("brand");
//        String plate_color = row.getAs("plate_color");
//        DateFormat df = new SimpleDateFormat("HH_dd_MM_yyyy");
//        String key = "";
//        key+= source_id;
//        key+= "_"+object_type;
//        return RowFactory.create(key,
//                1L);
//
//    }
//}
