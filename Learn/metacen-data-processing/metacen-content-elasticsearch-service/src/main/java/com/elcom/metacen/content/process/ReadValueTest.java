package com.elcom.metacen.content.process;

import com.elcom.metacen.content.dto.ContentDTO;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class ReadValueTest implements FlatMapFunction<List<ContentDTO>, Tuple2<String,Long>> {
    private static final Logger logger = LoggerFactory.getLogger(ReadValueTest.class);

    @Override
    public void flatMap(List<ContentDTO> value, Collector<Tuple2<String,Long>> out) {
        for (ContentDTO tmp: value
             ) {
            String key = tmp.getMediaUuidKey();
            Tuple2<String,Long> tuple4 = new Tuple2<>(key, Long.valueOf(1000000000));
            out.collect(tuple4);
        }

    }
}
