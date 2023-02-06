package elcom.com.neo4j.analysis;

import elcom.com.neo4j.clickhouse.model.VsatMedia;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple11;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
public class ReadValueNodeImportant implements FlatMapFunction<List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>>, Tuple4<String,String,String,Long>> {
    private static final Logger logger = LoggerFactory.getLogger(ReadValueNodeImportant.class);

    @Override
    public void flatMap(List<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> in, Collector<Tuple4<String,String,String,Long>> out) {
        for (Tuple11<String,String,String,String,Long,Long,String,String,String,String,String> data: in) {
            try {
                Tuple4<String, String, String, Long> src = new Tuple4<>(data.f0, data.f7, data.f10, 1L);
                Tuple4<String, String, String, Long> dest = new Tuple4<>(data.f0, data.f1, data.f10, 1L);
                out.collect(src);
                out.collect(dest);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
