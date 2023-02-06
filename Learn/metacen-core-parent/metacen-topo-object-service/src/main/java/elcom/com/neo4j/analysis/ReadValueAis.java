package elcom.com.neo4j.analysis;

import elcom.com.neo4j.clickhouse.model.Ais;
import elcom.com.neo4j.clickhouse.model.VsatMedia;
import elcom.com.neo4j.dto.KeytoObject;
import elcom.com.neo4j.dto.ObjectInfo;
import elcom.com.neo4j.dto.ValueSpark;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple11;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Component
public class ReadValueAis implements FlatMapFunction<List<Ais>, Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> {
    private static final Logger logger = LoggerFactory.getLogger(ReadValueAis.class);

    @Override
    public void flatMap(List<Ais> value, Collector<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> out) {
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Ais vsatMedia: value
             ) {
            ObjectInfo objectInfo = new ObjectInfo();
            if(vsatMedia.getSourceName()!=null&&!vsatMedia.getSourceName().isEmpty()) {
                objectInfo.setName(vsatMedia.getSourceName());
            }else {
                objectInfo.setName(vsatMedia.getSourceIp());
            }
            if(vsatMedia.getSourceId()!=null&&vsatMedia.getSourceId().compareTo(new BigInteger("0"))==1) {
                objectInfo.setId(String.valueOf(vsatMedia.getSourceId()));
            }else {
                objectInfo.setId(vsatMedia.getSourceIp());
            }
            objectInfo.setIps(vsatMedia.getSourceIp());

            ObjectInfo objectInfoDest = new ObjectInfo();
            objectInfoDest.setId(vsatMedia.getDestIp());
            objectInfoDest.setName(vsatMedia.getDestIp());
            objectInfoDest.setIps(vsatMedia.getDestIp());
            if (objectInfo != null && objectInfoDest != null) {
                Tuple11<String,String,String,String,Long,Long,String,String,String,String,String> tuple9= new Tuple11<>(objectInfo.getId(),objectInfo.getIps(),objectInfo.getName(),"Ais",Long.valueOf(vsatMedia.getCount()),1L,String.valueOf(vsatMedia.getDataSource()),objectInfoDest.getId(),objectInfoDest.getIps(),objectInfoDest.getName(),dff.format(vsatMedia.getEventTime()));
                out.collect(tuple9);
            }

        }

    }
}
