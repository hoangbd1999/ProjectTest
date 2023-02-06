package elcom.com.neo4j.clickhouse.service.impl;

import elcom.com.neo4j.clickhouse.model.Ais;
import elcom.com.neo4j.clickhouse.model.VsatMedia;
import elcom.com.neo4j.clickhouse.repository.VsatMediaCHRepository;
import elcom.com.neo4j.clickhouse.service.MetaCenMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetaCenMediaServiceImpl implements MetaCenMediaService {

    @Autowired
    private VsatMediaCHRepository vsatMediaCHRepository;

    @Override
    public List<VsatMedia> findSearch(String fromDate, String toDate) {
        return vsatMediaCHRepository.searchPlateGroupBy(fromDate, toDate);
    }

    @Override
    public List<VsatMedia> findSearch2(String fromDate, String toDate) {
        return vsatMediaCHRepository.searchPlateGroupBy2(fromDate, toDate);
    }

    @Override
    public List<VsatMedia> findReProcessing(String fromDate, String toDate, String ips) {
        return vsatMediaCHRepository.searchPlateGroupBy2(fromDate,toDate,ips);
    }

    @Override
    public List<Ais> findAis(String fromDate, String toDate) {
        return vsatMediaCHRepository.searchAis(fromDate,toDate);
    }

    @Override
    public List<Ais> findAisReProcess(String fromDate, String toDate, String ips) {
        return vsatMediaCHRepository.searchAis(fromDate,toDate,ips);
    }

    @Override
    public List<String> findNodeImportant(String startTime,String endTime, Integer size, Integer page) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calFrom = Calendar.getInstance();
        try {
            calFrom.setTime(df.parse(startTime));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        calFrom.set(Calendar.HOUR_OF_DAY,0);
        calFrom.clear(Calendar.MINUTE);
        calFrom.clear(Calendar.SECOND);
        calFrom.clear(Calendar.MILLISECOND);
        Calendar calTo = Calendar.getInstance();
        try {
            calTo.setTime(df.parse(endTime));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        calTo.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        calTo.set(Calendar.HOUR_OF_DAY,0);
        calTo.clear(Calendar.MINUTE);
        calTo.clear(Calendar.SECOND);
        calTo.clear(Calendar.MILLISECOND);
        StringBuilder stringBuilder = new StringBuilder("");
        Date start = new Date();
        Date end = new Date();
        try {
            start = df.parse(startTime);
            end = df.parse(endTime);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        String queryYear = "";
        if(end.getYear()-start.getYear()>=2){
            queryYear = "select  y.nodeIds, SUM(nodeSize) as count  from topology_node_to_node_month where startTime>= '"+
                    df.format(calFrom.getTime()) +"' and startTime<= '"+df.format(calTo.getTime())+"'  group by (nodeIds,startTime)";
        }

        String queryDay = "";
        if(end.getYear()==start.getYear()&&end.getMonth()==start.getMonth()){
            queryDay = "select  nodeIds,SUM(nodeSize) as count from topology_node_to_node_day y where startTime>= '"+
                    df.format(calFrom.getTime()) +"' and startTime< '"+df.format(calTo.getTime())+"' group by (nodeIds,startTime)  ";
        } else {
            queryDay = "select  nodeIds,SUM(nodeSize) as count  from topology_node_to_node_day y where  ( startTime>= '"+
                    df.format(calFrom.getTime()) +"'";
            calFrom.set(Calendar.DAY_OF_MONTH,1);
            calFrom.add(Calendar.MONTH,1);
            queryDay += " and startTime< '" +df.format(calFrom.getTime())+"' ) or (";
            queryDay += "startTime<= '"+df.format(calTo.getTime())+"' and ";
            calTo.set(Calendar.DAY_OF_MONTH,1);
            queryDay += "startTime>= '"+df.format(calTo.getTime())+"') group by (nodeIds,startTime) ";
        }

        String queryMonth = "";
        if(end.getYear()==start.getYear()){
            calFrom.set(Calendar.DAY_OF_MONTH,1);
            calFrom.add(Calendar.MONTH,1);
            calTo.set(Calendar.DAY_OF_MONTH,1);
            calTo.add(Calendar.MONTH,-1);
            queryMonth = "select  nodeIds,SUM(nodeSize) as count  from topology_node_to_node_month y where startTime>= '"+
                    df.format(calFrom.getTime()) +"' and startTime<= '"+df.format(calTo.getTime())+"' group by (nodeIds,startTime) ";
        }else {
            try {
                calFrom.setTime(df.parse(startTime));
            }catch (Exception ex){
                ex.printStackTrace();
            }

            calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calFrom.set(Calendar.HOUR_OF_DAY,0);
            calFrom.clear(Calendar.MINUTE);
            calFrom.clear(Calendar.SECOND);
            calFrom.clear(Calendar.MILLISECOND);
            calFrom.set(Calendar.DAY_OF_MONTH,1);
            calFrom.add(Calendar.MONTH,1);
            try {
                calTo.setTime(df.parse(endTime));
            }catch (Exception ex){
                ex.printStackTrace();
            }

            calTo.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calTo.set(Calendar.HOUR_OF_DAY,0);
            calTo.clear(Calendar.MINUTE);
            calTo.clear(Calendar.SECOND);
            calTo.clear(Calendar.MILLISECOND);
            queryMonth = "select  nodeIds,SUM(nodeSize) as count  from topology_node_to_node_month y where  ( startTime>= '"+
                    df.format(calFrom.getTime()) +"'";
            calFrom.set(Calendar.DAY_OF_MONTH,1);
            calFrom.add(Calendar.MONTH,-1);
            calFrom.set(Calendar.MONTH,11);
            queryMonth += " and startTime<= '" +df.format(calFrom.getTime())+"' ) or (";
            calTo.set(Calendar.DAY_OF_MONTH,1);
            calTo.add(Calendar.MONTH,-1);
            queryMonth += "startTime<= '"+df.format(calTo.getTime())+"' and ";
            calTo.add(Calendar.MONTH,1);
            calTo.set(Calendar.MONTH,0);
            queryMonth += "startTime>= '"+df.format(calTo.getTime())+"') group by (nodeIds,startTime) ";
        }

        String query = "select a.nodeIds, max(a.count) as count  from( ";
        if(queryDay.length()>1){
            query+=queryDay;
        }
        if(queryMonth.length()>1){
            query+= " UNION ALL  " +queryMonth;
        }
        if(queryYear.length()>1){
            query+= " UNION ALL  " +queryYear;
        }
        query+= ") a group by a.nodeIds order by  count desc";
        stringBuilder.append(query);
        stringBuilder.append(" LIMIT ").append(size).append(" OFFSET ").append(page);
        List<Object[]> result =vsatMediaCHRepository.getNodeImportant(stringBuilder.toString());
        if(result!=null) {
            List<String> tmp = result.stream().map(item -> item[0].toString()).collect(Collectors.toList());
            return tmp;
        }else {
            return new ArrayList<>();
        }

    }
}
