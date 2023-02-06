package elcom.com.neo4j.analysis;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple11;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Component
public class ReadValueToMonth implements FlatMapFunction<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>,Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> {
    private static final Logger logger = LoggerFactory.getLogger(ReadValueToMonth.class);

    @Override
    public void flatMap(Tuple11<String,String,String,String,Long,Long,String,String,String,String,String> in, Collector<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> out) {
        String eventTime ="";
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startTime = dff.parse(in.f10);
//            time = df.format(startTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);
            cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            in.f10 = dff.format(cal.getTime());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        out.collect(in);
    }
}
