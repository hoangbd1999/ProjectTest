package elcom.com.neo4j.analysis;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.tuple.Tuple11;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.util.Collector;

import java.util.HashSet;
import java.util.Set;

public class DistinctSrcAndDest implements GroupReduceFunction<Tuple11<String,String, String, String, Long, Long, String,String, String, String, String>, Tuple11<String,String, String, String, Long, Long, String,String, String, String, String>> {

    @Override
    public void reduce(Iterable<Tuple11<String,String, String, String, Long, Long, String,String, String, String, String>> in, Collector<Tuple11<String,String, String, String, Long, Long, String,String, String, String, String>> out) throws Exception {
        Set<String> uniqStrings = new HashSet<String>();
        Long countFile = 0L;
        Long countSize = 0L;
        Set<String> ipSrc = new HashSet<>();
        Set<String> ipDest = new HashSet<>();
        Set<String> dataSource = new HashSet<>();
        Tuple11<String,String, String, String, Long, Long, String,String, String, String, String> tmp = new Tuple11<>();
        // add all strings of the group to the set
        for (Tuple11<String,String, String, String, Long, Long, String,String, String, String, String> data : in) {
            countFile += data.f4;
            countSize += data.f5;
            ipSrc.add(data.f1);
            ipDest.add(data.f8);
            dataSource.add(data.f6);
            tmp = data;
        }
        String srcs = StringUtils.join(ipSrc,",");
        String dests = StringUtils.join(ipDest,",");
        String dataSources = StringUtils.join(dataSource,",");
        tmp.f4=countFile;
        tmp.f5=countSize;
        tmp.f6=dataSources;
        tmp.f1=srcs;
        tmp.f8=dests;
        out.collect(tmp);
    }
}
