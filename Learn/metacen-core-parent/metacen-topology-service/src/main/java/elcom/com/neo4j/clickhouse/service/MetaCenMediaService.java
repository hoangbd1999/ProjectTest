package elcom.com.neo4j.clickhouse.service;

import elcom.com.neo4j.clickhouse.model.Ais;
import elcom.com.neo4j.clickhouse.model.VsatMedia;

import java.text.ParseException;
import java.util.List;

public interface MetaCenMediaService {

    List<VsatMedia> findSearch(String fromDate, String toDate);

    List<VsatMedia> findSearch2(String fromDate, String toDate);

    List<VsatMedia> findReProcessing(String fromDate, String toDate,String ips);

    List<Ais> findAis(String fromDate,
                      String toDate);

    List<Ais> findAisReProcess(String fromDate,
                      String toDate, String ips);

    List<String> findNodeImportant(String startTime,String endTime, Integer size, Integer page);



}
