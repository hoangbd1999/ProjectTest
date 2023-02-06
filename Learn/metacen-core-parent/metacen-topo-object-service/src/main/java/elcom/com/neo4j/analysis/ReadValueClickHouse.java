package elcom.com.neo4j.analysis;

import elcom.com.neo4j.clickhouse.model.VsatMedia;
import elcom.com.neo4j.dto.KeytoObject;
import elcom.com.neo4j.dto.ObjectInfo;
import elcom.com.neo4j.dto.ValueSpark;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Component
public class ReadValueClickHouse implements FlatMapFunction<List<VsatMedia>,  Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> {
    private static final Logger logger = LoggerFactory.getLogger(ReadValueClickHouse.class);

    @Override
    public void flatMap(List<VsatMedia> value, Collector<Tuple11<String,String,String,String,Long,Long,String,String,String,String,String>> out) {
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (VsatMedia vsatMedia: value
             ) {
            ObjectInfo objectInfo =  new ObjectInfo();

            if(vsatMedia.getSourceName()!=null&&!vsatMedia.getSourceName().isEmpty()) {
                objectInfo.setName(vsatMedia.getSourceName());
                objectInfo.setId(String.valueOf(vsatMedia.getSourceId()));
            }else {
                objectInfo.setName(vsatMedia.getSourceIp());
                objectInfo.setId(vsatMedia.getSourceIp());
            }
//            if(vsatMedia.getSourceId()!=null&&vsatMedia.getSourceId().compareTo(new BigInteger("0"))==1) {
//                objectInfo.setId(String.valueOf(vsatMedia.getSourceId()));
//            }else {
//                objectInfo.setId(vsatMedia.getSourceIp());
//            }
            objectInfo.setIps(vsatMedia.getSourceIp());

            ObjectInfo objectInfoDest = new ObjectInfo();
            if(vsatMedia.getDestName()!=null&&!vsatMedia.getDestName().isEmpty()) {
                objectInfoDest.setName(vsatMedia.getDestName());
            }else {
                objectInfoDest.setName(vsatMedia.getDestIp());
            }
            if(vsatMedia.getDestId()!=null&&vsatMedia.getDestId().compareTo(new BigInteger("0"))==1) {
                objectInfoDest.setId(String.valueOf(vsatMedia.getDestId()));
            }else {
                objectInfoDest.setId(vsatMedia.getDestIp());
            }
            objectInfoDest.setIps(vsatMedia.getDestIp());
            if (objectInfo != null && objectInfoDest != null) {
                Tuple11<String,String,String,String,Long,Long,String,String,String,String,String> tuple11= new Tuple11<>(objectInfo.getId(),objectInfo.getIps(),objectInfo.getName(),vsatMedia.getMediaTypeName(),1L,vsatMedia.getFileSize(),String.valueOf(vsatMedia.getDataSource()),objectInfoDest.getId(),objectInfoDest.getIps(),objectInfoDest.getName(),dff.format(vsatMedia.getEventTime()));
                out.collect(tuple11);
            }

        }

    }
}
