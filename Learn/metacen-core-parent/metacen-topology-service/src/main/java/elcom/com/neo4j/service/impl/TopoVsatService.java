package elcom.com.neo4j.service.impl;

import elcom.com.neo4j.clickhouse.service.MetaCenMediaService;
import elcom.com.neo4j.dto.MapRecordNeo4j;
import elcom.com.neo4j.dto.ResponseTopo;
import elcom.com.neo4j.dto.Statement;
import elcom.com.neo4j.message.MessageContent;
import elcom.com.neo4j.message.ResponseMessage;
import elcom.com.neo4j.node.Object;
import elcom.com.neo4j.node.ObjectToNode;
import elcom.com.neo4j.node.Relationships;
import elcom.com.neo4j.node.ValueReport;
import elcom.com.neo4j.repositoryPostgre.CustomerRepository;
import elcom.com.neo4j.utils.StringUtil;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TopoVsatService {
    private final ObjectRepository movieRepository;

    private final Neo4jClient neo4jClient;

    private final Driver driver;

    private final DatabaseSelectionProvider databaseSelectionProvider;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MetaCenMediaService metaCenMediaService;

    @Autowired
    private SaveMutil saveMutil;

    TopoVsatService(ObjectRepository movieRepository,
                    Neo4jClient neo4jClient,
                    Driver driver,
                    DatabaseSelectionProvider databaseSelectionProvider) {

        this.movieRepository = movieRepository;
        this.neo4jClient = neo4jClient;
        this.driver = driver;
        this.databaseSelectionProvider = databaseSelectionProvider;
    }
    private Session sessionFor(String database) {
        if (database == null) {
            return driver.session();
        }
        return driver.session(SessionConfig.forDatabase(database));
    }

    private String database() {
        return databaseSelectionProvider.getDatabaseSelection().getValue();
    }

    private MapRecordNeo4j transform(Record record, Map<String, Object> listNode, Map<String,Integer> listNodeId, Map<String,Integer> indexNode
    , Map<String, Relationships> listRelationShipsMedia, List<String> listKeyMedia, Integer deep){
        MapRecordNeo4j mapRecordNeo4j = new MapRecordNeo4j();
//        String ids = record.get("a").get("id").asString();
//        String ips = record.get("a").get("ips").asString();
        String mmsi = record.get("a").get("mmsi").asString();
        String name = record.get("a").get("name").asString();
        if(listNode.get(mmsi)==null) {
            Object src = new Object(mmsi,name, indexNode.get("index"));
            listNode.put(mmsi,src);
            listNodeId.put(mmsi, indexNode.get("index"));
            indexNode.replace("index",indexNode.get("index")+1);


        }
        for(int i=1;i<=deep;i++ ) {
            String mmsiSrc = record.get("a"+i).get("mmsi").asString();
            String nameSrc = record.get("a"+i).get("name").asString();
            if (listNode.get(mmsiSrc) == null) {
                Object src = new Object( mmsiSrc, nameSrc,indexNode.get("index"));
                listNode.put(mmsiSrc, src);
                listNodeId.put(mmsiSrc, indexNode.get("index"));
                indexNode.replace("index", indexNode.get("index") + 1);


            }
            Relationships relationships = new Relationships();
            try {
                Long count = record.get("r"+i).get("count").asLong();
                relationships.setCount(count);
            }catch (Exception ex){
                Long count = Long.valueOf(record.get("r"+i).get("count").asString());
                relationships.setCount(count);
            }

            String time = record.get("r"+i).get("startTime").asString();
            String type =  record.get("r"+i).asRelationship().type();
            try {
                Long count = record.get("r"+i).get("fileSize").asLong();
                relationships.setFileSize(count);
            }catch (Exception ex){
                Long count = Long.valueOf(record.get("r"+i).get("fileSize").asString());
                relationships.setFileSize(count);
            }
            relationships.setType(type);
            relationships.setStartTime(time);
            relationships.setDataSource(record.get("r"+i).get("dataSource").asString());
            relationships.setSrc(record.get("r"+i).get("src").asString());
            relationships.setDest(record.get("r"+i).get("dest").asString());
            relationships.setSrcIp(record.get("r"+i).get("srcIp").asString());
            relationships.setDestIp(record.get("r"+i).get("destIp").asString());
            relationships.setStart(listNodeId.get(record.get("r"+i).get("src").asString()));
            relationships.setEnd(listNodeId.get(record.get("r"+i).get("dest").asString()));
            if(listRelationShipsMedia.get(record.get("r"+i).get("src").asString()+record.get("r"+i).get("dest").asString()+relationships.getType())==null){
                    listRelationShipsMedia.put(record.get("r"+i).get("src").asString()+record.get("r"+i).get("dest").asString()+relationships.getType(),relationships);
                    listKeyMedia.add(record.get("r"+i).get("src").asString()+record.get("r"+i).get("dest").asString()+relationships.getStartTime()+relationships.getType());
            } else {
                if(!listKeyMedia.contains(record.get("r"+i).get("src").asString()+record.get("r"+i).get("dest").asString()+time)){
                        Relationships relationships1 = listRelationShipsMedia.get(record.get("r"+i).get("src").asString()+record.get("r"+i).get("dest").asString()+relationships.getType());
                        relationships1.addRelation(relationships);
                        listRelationShipsMedia.replace(record.get("r"+i).get("src").asString()+record.get("r"+i).get("dest").asString()+relationships.getType(),relationships1);
                        listKeyMedia.add(record.get("r"+i).get("src").asString()+record.get("r"+i).get("dest").asString()+relationships.getStartTime()+relationships.getType());
                }
            }
        }
        mapRecordNeo4j.setIndexNode(indexNode);
        mapRecordNeo4j.setListKeyMedia(listKeyMedia);
        mapRecordNeo4j.setListRelationShipsMedia(listRelationShipsMedia);
        mapRecordNeo4j.setListNode(listNode);
        mapRecordNeo4j.setListNodeId(listNodeId);
        return mapRecordNeo4j;
    }

    private MapRecordNeo4j transform(Record record, Map<String, Relationships> listRelationShipsMedia, List<String> listKeyMedia,Set<java.lang.Object> keyRelation2,Set<Integer> idNode, Map<Long,String> relationIdToKey){
        MapRecordNeo4j mapRecordNeo4j = new MapRecordNeo4j();
////        String ids = record.get("a").get("id").asString();
////        String ips = record.get("a").get("ips").asString();
//        Relationships relationships = new Relationships();
//        try {
//            Long count = record.get("r1").get("count").asLong();
//            relationships.setCount(count);
//        }catch (Exception ex){
//            Long count = Long.valueOf(record.get("r1").get("count").asString());
//            relationships.setCount(count);
//        }
//        Relationship te=record.get("r1").asRelationship();
//
//        String time = record.get("r1").get("startTime").asString();
//        String type =  record.get("r1").asRelationship().type();
//        try {
//            Long count = record.get("r1").get("fileSize").asLong();
//            relationships.setFileSize(count);
//        }catch (Exception ex){
//            Long count = Long.valueOf(record.get("r1").get("fileSize").asString());
//            relationships.setFileSize(count);
//        }
//        relationships.setType(type);
//        relationships.setStartTime(time);
//        relationships.setDataSource(record.get("r1").get("dataSource").asString());
//        relationships.setSrc(record.get("r1").get("src").asString());
//        relationships.setDest(record.get("r1").get("dest").asString());
//        relationships.setSrcIp(record.get("r1").get("srcIp").asString());
//        relationships.setDestIp(record.get("r1").get("destIp").asString());
//        relationships.setStart((int) record.get("r1").asRelationship().startNodeId());
//        relationships.setEnd((int) record.get("r1").asRelationship().endNodeId());
//        idNode.add((int) record.get("r1").asRelationship().startNodeId());
//        idNode.add((int) record.get("r1").asRelationship().endNodeId());
        List<java.lang.Object> idss= record.get("r2").asList();
        keyRelation2.addAll(idss);
        keyRelation2.add(record.get("r1").asLong());

//        if(listRelationShipsMedia.get(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType())==null){
//            Long id = record.get("r1").asRelationship().id();
//            relationIdToKey.put(record.get("r1").asRelationship().id(),record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType());
//            listRelationShipsMedia.put(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType(),relationships);
//            listKeyMedia.add(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getStartTime()+relationships.getType());
//        } else {
//            if(!listKeyMedia.contains(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+time)){
//                Relationships relationships1 = listRelationShipsMedia.get(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType());
//                relationships1.addRelation(relationships);
//                listRelationShipsMedia.replace(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType(),relationships1);
//                listKeyMedia.add(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getStartTime()+relationships.getType());
//            }
//        }
        return mapRecordNeo4j;
    }

    private void transform(Record record, Map<String, Relationships> listRelationShipsMedia, List<String> listKeyMedia,Set<Integer> idNode,Map<Long,String> relationIdToKey){
//        String ids = record.get("a").get("id").asString();
//        String ips = record.get("a").get("ips").asString();
        Relationships relationships = new Relationships();
        try {
            Long count = record.get("r1").get("count").asLong();
            relationships.setCount(count);
        }catch (Exception ex){
            Long count = Long.valueOf(record.get("r1").get("count").asString());
            relationships.setCount(count);
        }

        String time = record.get("r1").get("startTime").asString();
        String type =  record.get("r1").asRelationship().type();
        try {
            Long count = record.get("r1").get("fileSize").asLong();
            relationships.setFileSize(count);
        }catch (Exception ex){
            Long count = Long.valueOf(record.get("r1").get("fileSize").asString());
            relationships.setFileSize(count);
        }
        relationships.setType(type);
        relationships.setStartTime(time);
        relationships.setDataSource(record.get("r1").get("dataSource").asString());
        relationships.setSrc(record.get("r1").get("src").asString());
        relationships.setDest(record.get("r1").get("dest").asString());
        relationships.setSrcIp(record.get("r1").get("srcIp").asString());
        relationships.setDestIp(record.get("r1").get("destIp").asString());
        relationships.setStart((int) record.get("start").asLong());
        relationships.setEnd((int) record.get("end").asLong());
        idNode.add((int) record.get("start").asLong());
        idNode.add((int) record.get("end").asLong());
        Long id= record.get("id").asLong();
        if(listRelationShipsMedia.get(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType())==null){

            relationIdToKey.put(id,record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType());
            listRelationShipsMedia.put(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType(),relationships);
            listKeyMedia.add(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getStartTime()+relationships.getType());
        } else {
            if(!listKeyMedia.contains(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+time+relationships.getType())){
                Relationships relationships1 = listRelationShipsMedia.get(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType());
                relationships1.addRelation(relationships);
                listRelationShipsMedia.replace(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType(),relationships1);
                listKeyMedia.add(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getStartTime()+relationships.getType());
            }
        }
    }

    private void transform(Record record, Map<String, Object> listNode){
//        String ids = record.get("a").get("id").asString();
//        String ips = record.get("a").get("ips").asString();
        String mmsi = record.get("a").get("mmsi").asString();
        String name = record.get("a").get("name").asString();
        Integer index = (int) record.get("id").asLong();
        if(listNode.get(mmsi)==null) {
            Object src = new Object(mmsi,name, index);
            listNode.put(mmsi,src);
        }
    }

    public void transformSub(Record record, Map<String,Integer> listNodeId, Map<String, Relationships> listRelationShips){
        Relationships relationships = new Relationships();
        try {
            Long count = record.get("r1").get("count").asLong();
            relationships.setCount(count);
        }catch (Exception ex){
            Long count = Long.valueOf(record.get("r1").get("count").asString());
            relationships.setCount(count);
        }

        String time = record.get("r1").get("startTime").asString();
        relationships.setStartTime(time);
        String type =  record.get("r1").asRelationship().type();
        relationships.setType(type);
        try {
            Long count = record.get("r1").get("fileSize").asLong();
            relationships.setFileSize(count);
        }catch (Exception ex){
            Long count = Long.valueOf(record.get("r1").get("fileSize").asString());
            relationships.setFileSize(count);
        }
        if(listRelationShips.get(record.get("r1").get("src").asString()+record.get("r1").get("dest").asString()+relationships.getType())!=null) {
            Relationships relationships1 = listRelationShips.get(record.get("r1").get("src").asString() + record.get("r1").get("dest").asString()+relationships.getType());
            relationships1.subRelation(relationships);
            listRelationShips.replace(record.get("r1").get("src").asString() + record.get("r1").get("dest").asString()+relationships.getType(), relationships1);
        }
    }


//    public ResponseTopo getTopo(String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
//            exactly) throws ParseException {
//
//        var nodes = new ArrayList<>();
//        var links = new ArrayList<>();
//        String key="use fabric.";
//        ResponseTopo result = new ResponseTopo();
//        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        DateFormat df = new SimpleDateFormat("yyyy-MM");
//        DateFormat dfYear = new SimpleDateFormat("yyyy");
//        DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
//        Set<Object> setNode = new LinkedHashSet<>();
//        Set<Relationships> relationshipsResponse = new LinkedHashSet<>();
//        Date startTime = dff.parse(fromDate);
//        Date endTime = dff.parse(toDate);
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(startTime);
//        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//        cal.set(Calendar.HOUR_OF_DAY,0);
//        cal.clear(Calendar.MINUTE);
//        cal.clear(Calendar.SECOND);
//        cal.clear(Calendar.MILLISECOND);
//        String query="";
//        Map<String, Object> listNode = new HashMap<>();
//        Map<String,Integer> listNodeId = new HashMap<>();
//        Map<String, Relationships> listRelationShipsAis = new HashMap<>();
//        Map<String, Relationships> listRelationShipsMedia = new HashMap<>();
//        Map<String,Integer> indexNode = new HashMap<>();
//        List<String> listKeyMedia = new ArrayList<>();
//        List<String> listKeyAis = new ArrayList<>();
//        indexNode.put("index",1);
//        try (Session session = sessionFor(database())) {
//            if (df.format(startTime).equals(df.format(endTime))) {
//                Calendar calStart = Calendar.getInstance();
//                calStart.setTime(startTime);
//                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                Calendar calEnd = Calendar.getInstance();
//                calEnd.setTime(endTime);
//                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                calEnd.set(Calendar.HOUR_OF_DAY, 0);
//                calEnd.clear(Calendar.MINUTE);
//                calEnd.clear(Calendar.SECOND);
//                calEnd.clear(Calendar.MILLISECOND);
//                query = "use fabric.vsatmonth ";
//                List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                if(nodeImportant.isEmpty()){
//                    return new ResponseTopo();
//                }
//                query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                String finalQuery1 = query;
//                var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                int indexPage = 1;
//                while (records.isEmpty()){
//                    if(nodeImportant.size()==5){
//                        indexPage=0;
//                        nodeImportant = listNodeImportant(fromDate,toDate,50,indexPage);
//                        query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                        String finalQuery2 = query;
//                        records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                        indexPage++;
//                    }else {
//                        if(nodeImportant.size()==50){
//                            indexPage=0;
//                            nodeImportant = listNodeImportant(fromDate,toDate,100,indexPage);
//                            query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                            String finalQuery2 = query;
//                            records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                            indexPage++;
//                        } else  if(nodeImportant.size()==100){
//                            indexPage=0;
//                            nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                            query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                            String finalQuery2 = query;
//                            records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                            indexPage++;
//                        } else  if(nodeImportant.size()==200){
//                            if(indexPage>50){
//                                break;
//                            }
//                            nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                            query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                            String finalQuery2 = query;
//                            records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                            indexPage++;
//                        } else {
//                            break;
//                        }
//                    }
//
//                }
//                if(records.isEmpty()){
//                    query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
//                    String finalQuery2 = query;
//                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                }
//                System.out.println("xong" + new Date().toString());
//                records.forEach(record -> {
//                    transform(record,listNode,listNodeId,indexNode,listRelationShipsMedia,listKeyMedia,deep);
//                });
//                processMonthSubDay(session,listNodeId,listRelationShipsMedia,startTime,endTime,typeRelation,typeData);
//                processResult(records,listNode,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//
//            } else {
//                Calendar calStart = Calendar.getInstance();
//                calStart.setTime(startTime);
//                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                Calendar calEnd = Calendar.getInstance();
//                calEnd.setTime(endTime);
//                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                calEnd.set(Calendar.HOUR_OF_DAY, 0);
//                calEnd.clear(Calendar.MINUTE);
//                calEnd.clear(Calendar.SECOND);
//                calEnd.clear(Calendar.MILLISECOND);
//                if (dfYear.format(startTime).equals(dfYear.format(endTime))) {
//                    if (endTime.getMonth() - startTime.getMonth() <= 6) {
////                        query = "use fabric.vsatmonth MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
////                        String finalQuery1 = query;
////                        var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                        query = "use fabric.vsatmonth ";
//                        List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                        query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                        //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                        String finalQuery1 = query;
//                        if(nodeImportant.isEmpty()){
//                            return new ResponseTopo();
//                        }
//                        var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                        int indexPage = 1;
//                        while (records.isEmpty()){
////                            if(ip==null|| ip.isEmpty())
////                                break;
//                            if(nodeImportant.size()==5){
//                                indexPage=0;
//                                nodeImportant = listNodeImportant(fromDate,toDate,50,indexPage);
//                                query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                String finalQuery2 = query;
//                                records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                indexPage++;
//                            }else {
//                                if(nodeImportant.size()==50){
//                                    indexPage=0;
//                                    nodeImportant = listNodeImportant(fromDate,toDate,100,indexPage);
//                                    query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                    String finalQuery2 = query;
//                                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                    indexPage++;
//                                } else  if(nodeImportant.size()==100){
//                                    indexPage=0;
//                                    nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                                    query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                    String finalQuery2 = query;
//                                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                    indexPage++;
//                                } else  if(nodeImportant.size()==200){
//                                    if(indexPage>50){
//                                        break;
//                                    }
//                                    nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                                    query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                    String finalQuery2 = query;
//                                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                    indexPage++;
//                                } else {
//                                    break;
//                                }
//                            }
//
//                        }
//                        if(records.isEmpty()){
//                            query= "use fabric.vsatmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
//                            String finalQuery2 = query;
//                            records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                        }
//                        System.out.println("xong" + new Date().toString());
//                        records.forEach(record -> {
//                            transform(record,listNode,listNodeId,indexNode,listRelationShipsMedia,listKeyMedia,deep);
//                        });
//                        processMonthSubDay(session,listNodeId,listRelationShipsMedia,startTime,endTime,typeRelation,typeData);
//                        processResult(records,listNode,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//
//                    } else {
//                        query = "use fabric.vsatyear ";
//                        List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                        query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                        //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                        String finalQuery1 = query;
//                        if(nodeImportant.isEmpty()){
//                            return new ResponseTopo();
//                        }
//                        var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                        int indexPage = 1;
//                        while (records.isEmpty()){
////                            if(ip==null|| ip.isEmpty())
////                                break;
//                            if(nodeImportant.size()==5){
//                                indexPage=0;
//                                nodeImportant = listNodeImportant(fromDate,toDate,50,indexPage);
//                                query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                String finalQuery2 = query;
//                                records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                indexPage++;
//                            }else {
//                                if(nodeImportant.size()==50){
//                                    indexPage=0;
//                                    nodeImportant = listNodeImportant(fromDate,toDate,100,indexPage);
//                                    query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                    String finalQuery2 = query;
//                                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                    indexPage++;
//                                } else  if(nodeImportant.size()==100){
//                                    indexPage=0;
//                                    nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                                    query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                    String finalQuery2 = query;
//                                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                    indexPage++;
//                                } else  if(nodeImportant.size()==200){
//                                    if(indexPage>50){
//                                        break;
//                                    }
//                                    nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                                    query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                    String finalQuery2 = query;
//                                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                    indexPage++;
//                                } else {
//                                    break;
//                                }
//                            }
//
//                        }
//                        if(records.isEmpty()){
//                            query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
//                            String finalQuery2 = query;
//                            records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                        }
//                        System.out.println("xong" + new Date().toString());
//                        records.forEach(record -> {
//                            transform(record,listNode,listNodeId,indexNode,listRelationShipsMedia,listKeyMedia,deep);
//                        });
//
//                        processYearSubMonth(session,listNodeId,listRelationShipsMedia,startTime,endTime,typeRelation,typeData);
//                        processMonthSubDay(session,listNodeId,listRelationShipsMedia,startTime,endTime,typeRelation,typeData);
//                        processResult(records,listNode,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//                    }
//                } else {
//
//                    //trên 2 năm
//                    query = "use fabric.vsatyear ";
//                    List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                    query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                    //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                    String finalQuery1 = query;
//                    if(nodeImportant.isEmpty()){
//                        return new ResponseTopo();
//                    }
//                    var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                    int indexPage = 1;
//                    while (records.isEmpty()){
////                        if(ip==null|| ip.isEmpty())
////                            break;
//                        if(nodeImportant.size()==5){
//                            indexPage=0;
//                            nodeImportant = listNodeImportant(fromDate,toDate,50,indexPage);
//                            query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                            String finalQuery2 = query;
//                            records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                            indexPage++;
//                        }else {
//                            if(nodeImportant.size()==50){
//                                indexPage=0;
//                                nodeImportant = listNodeImportant(fromDate,toDate,100,indexPage);
//                                query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                String finalQuery2 = query;
//                                records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                indexPage++;
//                            } else  if(nodeImportant.size()==100){
//                                indexPage=0;
//                                nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                                query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                String finalQuery2 = query;
//                                records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                indexPage++;
//                            } else  if(nodeImportant.size()==200){
//                                if(indexPage>50){
//                                    break;
//                                }
//                                nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
//                                query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                                String finalQuery2 = query;
//                                records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                                indexPage++;
//                            } else {
//                                break;
//                            }
//                        }
//
//                    }
//                    if(records.isEmpty()){
//                        query= "use fabric.vsatyear "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
//                        String finalQuery2 = query;
//                        records= session.readTransaction(tx -> tx.run(finalQuery2).list());
//                    }
//                    System.out.println("xong" + new Date().toString());
//                    records.forEach(record -> {
//                        transform(record,listNode,listNodeId,indexNode,listRelationShipsMedia,listKeyMedia,deep);
//                    });
//
//                    processYearSubMonth(session,listNodeId,listRelationShipsMedia,startTime,endTime,typeRelation,typeData);
//                    processMonthSubDay(session,listNodeId,listRelationShipsMedia,startTime,endTime,typeRelation,typeData);
//                    processResult(records,listNode,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//                }
//            }
//        }
//        result.setNodes(new ArrayList<>(setNode));
//        result.setRelationships(new ArrayList<>(relationshipsResponse));
//
//        return result;
//    }

//    public ResponseTopo getTopo1(String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
//            exactly) throws ParseException {
//
//        var nodes = new ArrayList<>();
//        var links = new ArrayList<>();
//        String key="use fabric.";
//        ResponseTopo result = new ResponseTopo();
//        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        DateFormat df = new SimpleDateFormat("yyyy-MM");
//        DateFormat dfYear = new SimpleDateFormat("yyyy");
//        DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
//        Set<Object> setNode = new LinkedHashSet<>();
//        Set<Relationships> relationshipsResponse = new LinkedHashSet<>();
//        Date startTime = dff.parse(fromDate);
//        Date endTime = dff.parse(toDate);
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(startTime);
//        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//        cal.set(Calendar.HOUR_OF_DAY,0);
//        cal.clear(Calendar.MINUTE);
//        cal.clear(Calendar.SECOND);
//        cal.clear(Calendar.MILLISECOND);
//        String query="";
//        Map<String, Object> listNode = new HashMap<>();
//        Map<String,Integer> listNodeId = new HashMap<>();
//        Map<String, Relationships> listRelationShipsAis = new HashMap<>();
//        Map<String, Relationships> listRelationShipsMedia = new HashMap<>();
//        Map<String,Integer> indexNode = new HashMap<>();
//        List<String> listKeyMedia = new ArrayList<>();
//        List<String> listKeyAis = new ArrayList<>();
//        indexNode.put("index",1);
//        try (Session session = sessionFor(database())) {
//            if (df.format(startTime).equals(df.format(endTime))) {
//                Calendar calStart = Calendar.getInstance();
//                calStart.setTime(startTime);
//                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                Calendar calEnd = Calendar.getInstance();
//                calEnd.setTime(endTime);
//                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                calEnd.set(Calendar.HOUR_OF_DAY, 0);
//                calEnd.clear(Calendar.MINUTE);
//                calEnd.clear(Calendar.SECOND);
//                calEnd.clear(Calendar.MILLISECOND);
//                query = "use fabric.vsatmonth ";
//                List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                String finalQuery1 = query;
//                var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use fabric.vsatmonth ");
//                records.forEach(record -> {
//                    transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//                });
//                processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//
//            } else {
//                Calendar calStart = Calendar.getInstance();
//                calStart.setTime(startTime);
//                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                Calendar calEnd = Calendar.getInstance();
//                calEnd.setTime(endTime);
//                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                calEnd.set(Calendar.HOUR_OF_DAY, 0);
//                calEnd.clear(Calendar.MINUTE);
//                calEnd.clear(Calendar.SECOND);
//                calEnd.clear(Calendar.MILLISECOND);
//                if (dfYear.format(startTime).equals(dfYear.format(endTime))) {
//                    if (endTime.getMonth() - startTime.getMonth() <= 6) {
//                        query = "use fabric.vsatmonth ";
//                        List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                        query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                        //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                        String finalQuery1 = query;
//                        var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                        records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use fabric.vsatmonth ");
//                        System.out.println("xong" + new Date().toString());
//                        records.forEach(record -> {
//                            transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//                        });
//                        processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                        processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//
//                    } else {
//                        query = "use fabric.vsatyear ";
//                        List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                        query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                        //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                        String finalQuery1 = query;
//                        var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                        int indexPage = 1;
//                        records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use fabric.vsatyear ");
//                        records.forEach(record -> {
//                            transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//
//                        });
//                        processYearSubMonth(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                        processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                        processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//                    }
//                } else {
//
//                    //trên 2 năm
//                    query = "use fabric.vsatyear ";
//                    List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                    query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                    //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                    String finalQuery1 = query;
//                    var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                    int indexPage = 1;
//                    records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use fabric.vsatyear ");
//                    System.out.println("xong" + new Date().toString());
//                    records.forEach(record -> {
//                        transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//
//                    });
//                    processYearSubMonth(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                            ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                    processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                            ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                    processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//                }
//            }
//        }
//        result.setNodes(new ArrayList<>(setNode));
//        result.setRelationships(new ArrayList<>(relationshipsResponse));
//
//        return result;
//    }
    public String test() throws ParseException {
        String a ="";
        try (Session session = sessionFor("metacenhour")) {
            var records = session.readTransaction(tx -> tx.run("match (a)-[r:Web]->(b) return r limit 1").list());
            long id;
            if(records!=null&&!records.isEmpty()){
                Record record= records.get(0);
                id = record.get("r").asRelationship().id();
//                session.readTransaction(tx -> tx.run("MATCH ()-[r:Web]->() WHERE id(r) ="+id+" delete r"));
                a="MATCH ()-[r:Web]->() WHERE id(r) ="+id+" delete r";
                String finalA = a;
                session.writeTransaction(tx -> tx.run(finalA));
//                session.run("MATCH ()-[r:Web]->() WHERE id(r) ="+id+" delete r");
//                session.writeTransaction(tx -> tx.run("MATCH ()-[r:Video]->() WHERE id(r) ="+id+ " delete r"));
            }

//            session.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return a;
    }

    public String test2(List<Long> ids) throws ParseException {
        String a ="";
        try (Session session = sessionFor("metacenhour")) {
            String id= ids.toString();
            var records = session.writeTransaction(tx -> tx.run("Adsad"));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return a;
    }

    public ResponseTopo getTopoTest1(String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly,String ids, Integer page) throws ParseException {

        var nodes = new ArrayList<>();
        var links = new ArrayList<>();
//        String key="use metacen.";
        String key="use metacen.";
        ResponseTopo result = new ResponseTopo();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat df = new SimpleDateFormat("yyyy-MM");
        DateFormat dfYear = new SimpleDateFormat("yyyy");
        DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
        Set<Object> setNode = new LinkedHashSet<>();
        Set<Relationships> relationshipsResponse = new LinkedHashSet<>();
        Date startTime = dff.parse(fromDate);
        Date endTime = dff.parse(toDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        String query="";
        try (Session session = sessionFor(database())) {
            if (df.format(startTime).equals(df.format(endTime))) {
                Calendar calStart = Calendar.getInstance();
                calStart.setTime(startTime);
                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(endTime);
                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                calEnd.set(Calendar.HOUR_OF_DAY, 0);
                calEnd.clear(Calendar.MINUTE);
                calEnd.clear(Calendar.SECOND);
                calEnd.clear(Calendar.MILLISECOND);
                List<String> nodeImportant = listNodeImportant(fromDate,toDate,10,0);
                if(nodeImportant.isEmpty()){
                    return new ResponseTopo();
                }
                if(calEnd.getTime().getDate()-calStart.getTime().getDate()>15) {
                    processMonthSubDay(session, fromDate, toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, ids, page, nodeImportant, setNode, relationshipsResponse);
                }else {
                    processDay(session, fromDate, toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, ids, page, nodeImportant, setNode, relationshipsResponse);

                }
            } else {
                Calendar calStart = Calendar.getInstance();
                calStart.setTime(startTime);
                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(endTime);
                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                calEnd.set(Calendar.HOUR_OF_DAY, 0);
                calEnd.clear(Calendar.MINUTE);
                calEnd.clear(Calendar.SECOND);
                calEnd.clear(Calendar.MILLISECOND);
                int day = 30 - startTime.getDate() + endTime.getDate() - 1;
                if (day < 40) {
                    List<String> nodeImportant = listNodeImportant(fromDate,toDate,10,0);
                    if(nodeImportant.isEmpty()){
                        return new ResponseTopo();
                    }
                    processDay(session, fromDate, toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, ids, page, nodeImportant, setNode, relationshipsResponse);
                } else {
                    if (dfYear.format(startTime).equals(dfYear.format(endTime))) {
                        if (endTime.getMonth() - startTime.getMonth() <= 6) {
                            List<String> nodeImportant = listNodeImportant(fromDate, toDate, 10, 0);
                            if (nodeImportant.isEmpty()) {
                                return new ResponseTopo();
                            }
                            processMonthSubDay(session, fromDate, toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, ids, page, nodeImportant, setNode, relationshipsResponse);
                        } else {
                            List<String> nodeImportant = listNodeImportant(fromDate, toDate, 10, 0);
                            if (nodeImportant.isEmpty()) {
                                return new ResponseTopo();
                            }
                            processMonthSubDay(session, fromDate, toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, ids, page, nodeImportant, setNode, relationshipsResponse);
                        }
                    } else {
                        List<String> nodeImportant = listNodeImportant(fromDate, toDate, 10, 0);
                        if (nodeImportant.isEmpty()) {
                            return new ResponseTopo();
                        }
                        processMonthSubDay(session, fromDate, toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, ids, page, nodeImportant, setNode, relationshipsResponse);
                    }
                }
            }
        }
        result.setNodes(new ArrayList<>(setNode));
        result.setRelationships(new ArrayList<>(relationshipsResponse));
        System.out.println("trả về xong");
        return result;
    }
    public void processFilterIDsPage(Session session, String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly,String ids, Set<java.lang.Object> objectList,Integer page, Boolean checkNode){
        String query= "use metacenv1.metacenmonth "+ createQueryUpdate(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
        int from = page*100;
        int to = page*100+100;
//        if(checkNode){
//            from = page*20;
//            to = page*20+20;
//        }
        if(deep==2) {
            query += " and a1.mmsi = '"+ids+"' ";
            query += " return a1 , COLLECT(DISTINCT a2.mmsi)["+from+".."+to+"] as a2 ";
        }else {
            query += " and a.mmsi = '"+ids+"' ";
            query += " return a , COLLECT(DISTINCT a1.mmsi)["+from+".."+to+"] as a1 ";
        }
        String finalQuery2 = query;
        var records= session.readTransaction(tx -> tx.run(finalQuery2).list());
        records.forEach(record -> {
            List<java.lang.Object> idss;
            if(deep==1) {
                idss= record.get("a1").asList();
            }else {
                idss = record.get("a2").asList();
            }
            objectList.addAll(idss);

        });
    }

    public void processFilterIDsPageDay(Session session, String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly,String ids, Set<java.lang.Object> objectList,Integer page, Boolean checkNode){
        String query= "use metacenv1.metacenday "+ createQueryUpdate(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
        if(page==null){
            page=0;
        }
        int from = page*100;
        int to = page*100+100;
//        if(checkNode){
//            from = page*20;
//            to = page*20+20;
//        }
        if(deep==2) {
            query += " and a1.mmsi = '"+ids+"' ";
            query += " return a1 , COLLECT(DISTINCT a2.mmsi)["+from+".."+to+"] as a2 ";
        }else {
            query += " and a.mmsi = '"+ids+"' ";
            query += " return a , COLLECT(DISTINCT a1.mmsi)["+from+".."+to+"] as a1 ";
        }
        String finalQuery2 = query;
        var records= session.readTransaction(tx -> tx.run(finalQuery2).list());
        records.forEach(record -> {
            List<java.lang.Object> idss;
            if(deep==1) {
                idss= record.get("a1").asList();
            }else {
                idss = record.get("a2").asList();
            }
            objectList.addAll(idss);

        });
    }

    private void processMonthSubDay(Session session, String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly,String ids,Integer page, List<String> nodeImportant, Set<Object> setNode , Set<Relationships> relationshipsResponse) throws ParseException {

        System.out.println(fromDate +" "+toDate+ " " +ip + " " +type+ " " +typeRelation+ " "+deep+ " " +typeData + " " +dataSource+ " "  +exactly+ " "  + ids+ " " + page);
        Set<java.lang.Object> objectList = new LinkedHashSet<>();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> listNode = new HashMap<>();
        Map<String,Integer> listNodeId = new HashMap<>();
        Map<String, Relationships> listRelationShipsMedia = new HashMap<>();
        Map<String,Integer> indexNode = new HashMap<>();
        Map<String,Long> countNode = new HashMap<>();
        List<String> listKeyMedia = new ArrayList<>();
        Date startTime = dff.parse(fromDate);
        Date endTime = dff.parse(toDate);
        indexNode.put("index",1);
        Set<String> nodes= new HashSet<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_MONTH,1);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        String query = "use metacenv1.metacenmonth ";
        List<String> nodeIds = new ArrayList<>();
        Boolean check = true;
        Boolean checkNodeId = false;
        countNode(session,fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,ids,page,countNode,"metacenmonth ");

        if(nodeImportant.size()>=10) {
            query += createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
            if (deep == 2) {
                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 1000";
            } else {
                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 1000";
            }
            //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
            String finalQuery1 = query;
            var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
            nodes= new HashSet<>();
            processAddIds(records, deep, ids, objectList,countNode,nodes);
            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
            if(nodeIds.size()>500){
                query ="use metacenv1.metacenmonth "+ createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                if (deep == 2) {
                    query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 1000";
                } else {
                    query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 1000";
                }
                //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
                String finalQuery2 = query;
                records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                objectList = new LinkedHashSet<>();
                nodes= new HashSet<>();
                processAddIds(records, deep, ids, objectList,countNode,nodes);
                nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                checkNodeId=true;
            }
            int indexPage = 1;
            while (records.isEmpty()) {
//                    if(ip==null|| ip.isEmpty())
//                        break;
                if (nodeImportant.size() == 10) {
                    indexPage = 0;
                    nodeImportant = listNodeImportant(dff.format(cal.getTime()), toDate, 50, 10);
                    query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                    if (deep == 2) {
                        query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 1000";
                    } else {
                        query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 1000";
                    }
                    String finalQuery2 = query;
                    records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                    objectList = new LinkedHashSet<>();
                    nodes= new HashSet<>();
                    processAddIds(records, deep, ids, objectList,countNode,nodes);
                    nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                    if(nodeIds.size()>500){
                        query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                        if (deep == 2) {
                            query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 1000";
                        } else {
                            query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 1000";
                        }
                        String finalQuery3 = query;
                        records = session.readTransaction(tx -> tx.run(finalQuery3).list());
                        objectList = new LinkedHashSet<>();
                        nodes= new HashSet<>();
                        processAddIds(records, deep, ids, objectList,countNode,nodes);
                        nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                        checkNodeId=true;
                    }
                    indexPage++;
                } else {
                    if (nodeImportant.size() == 50) {
                        indexPage = 0;
                        nodeImportant = listNodeImportant(dff.format(cal.getTime()), toDate, 100, 50);
                        query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                        if (deep == 2) {
                            query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 1000";
                        } else {
                            query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 1000";
                        }
                        String finalQuery2 = query;
                        records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                        nodes= new HashSet<>();
                        processAddIds(records, deep, ids, objectList,countNode,nodes);
                        nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                        if(nodeIds.size()>500){
                            query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                            if (deep == 2) {
                                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 1000";
                            } else {
                                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 1000";
                            }
                            String finalQuery3 = query;
                            records = session.readTransaction(tx -> tx.run(finalQuery3).list());
                            objectList = new HashSet<>();
                            nodes= new HashSet<>();
                            processAddIds(records, deep, ids, objectList,countNode,nodes);
                            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                            checkNodeId=true;
                        }

                        indexPage++;
                    } else if (nodeImportant.size() == 100) {
                        indexPage = 0;
                        nodeImportant = listNodeImportant(dff.format(cal.getTime()), toDate, 200, 100);
                        query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                        if (deep == 2) {
                            query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 1000";
                        } else {
                            query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 1000";
                        }

                        String finalQuery2 = query;
                        records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                        objectList = new HashSet<>();
                        nodes= new HashSet<>();
                        processAddIds(records, deep, ids, objectList,countNode,nodes);
                        nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                        if(nodeIds.size()>500){
                            query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                            if (deep == 2) {
                                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 1000";
                            } else {
                                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 1000";
                            }
                            String finalQuery3 = query;
                            records = session.readTransaction(tx -> tx.run(finalQuery3).list());
                            objectList = new HashSet<>();
                            nodes= new HashSet<>();
                            processAddIds(records, deep, ids, objectList,countNode,nodes);
                            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                            checkNodeId=true;
                        }
                        indexPage++;
                    } else {
                        break;
                    }
                }

            }
            if(records.isEmpty()){
                check = false;
                query= "use metacenv1.metacenmonth "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
                if(deep==2) {
                    query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 500";
                }else {
                    query += " with  a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1, count(distinct a1.mmsi) as co return a,a1 order by co desc  limit 100";
                }
            }else {
                if((nodeIds!=null && !nodeIds.isEmpty()&&nodeIds.size()<200)|| nodeIds.isEmpty()){
                    check = false;
                    query= "use metacenv1.metacenmonth "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
                    if(deep==2) {
                        query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 500";
                    }else {
                        query += " with  a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1, count(distinct a1.mmsi) as co return a,a1 order by co desc  limit 100";
                    }

                }
            }
        }else {
            check=false;
            query= "use metacenv1.metacenmonth "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
            if(deep==2) {
                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 500";
            }else {
                query += " with  a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1, count(distinct a1.mmsi) as co return a,a1 order by co desc  limit 100";
            }
        }


        String finalQuery3 = query;
        var records= session.readTransaction(tx -> tx.run(finalQuery3).list());
        objectList = new HashSet<>();
        nodes= new HashSet<>();
        processAddIds(records, deep, ids, objectList,countNode,nodes);
        nodeIds = objectList.stream().map((item) ->item.toString()).collect(Collectors.toList());
        if(nodeIds.size()>1000){
            if(check==false) {
                query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, null);
            }else {
                query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
            }
            if (deep == 2) {
                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 500 ";
            } else {
                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 1000";
            }
            String finalQuery4 = query;
            records = session.readTransaction(tx -> tx.run(finalQuery4).list());
            objectList = new LinkedHashSet<>();
            nodes= new HashSet<>();
            processAddIds(records, deep, ids, objectList,countNode,nodes);
            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
            checkNodeId=true;
        }

        if(ids!=null&& !ids.isEmpty()){
            processFilterIDsPage(session,dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,ids,objectList,page,checkNodeId);
            nodeIds = objectList.stream().map((item) ->item.toString()).collect(Collectors.toList());

        }
            String queryDeep ="";
         if(nodeIds!=null && !nodeIds.isEmpty()) {

            if (check == true) {
                nodeImportant.add(ids);
//                query= "use metacenv1.metacenmonth "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                if(deep==2) {
//                    query +=" return a1 , count( DISTINCT a2) as a2 ";
//                }else {
//                    query +=" return a , count(DISTINCT a1) as a1 ";
//                }
//
//                String finalQuery = query;
//                records= session.readTransaction(tx -> tx.run(finalQuery).list());
//                records.forEach(record -> {
//                    if(deep==2) {
//                        String idss = record.get("a1").get("mmsi").asString();
//                        Long count = record.get("a2").asLong();
//                        countNode.put(idss,count);
//                    }else {
//                        String idss = record.get("a").get("mmsi").asString();
//                        Long count = record.get("a1").asLong();
//                        countNode.put(idss,count);
//                    }
//                });
                query = "use metacenv1.metacenmonth " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant, nodeIds);
                queryDeep = query;
            } else {
//                query= "use metacenv1.metacenmonth "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
//                if(deep==2) {
//                    query +=" return a1 , count(DISTINCT a2) as a2  ";
//                }else {
//                    query +=" return a , count(DISTINCT a1) as a1 ";
//                }
//
//                String finalQuery = query;
//                records= session.readTransaction(tx -> tx.run(finalQuery).list());
//                records.forEach(record -> {
//                    if(deep==2) {
//                        String idss = record.get("a1").get("mmsi").asString();
//                        Long count = record.get("a2").asLong();
//                        countNode.put(idss,count);
//                    }else {
//                        String idss = record.get("a").get("mmsi").asString();
//                        Long count = record.get("a1").asLong();
//                        countNode.put(idss,count);
//                    }
//                });
                if(ip==null||ip.isEmpty()) {
                    query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, new ArrayList<>(nodes), nodeIds);
                }else {
                    query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, new ArrayList<>(nodes), nodeIds);
                }
                queryDeep = query;
            }

             if(deep==1){
                 if(check==false){
                     queryDeep += " limit 2000";
                 }else {
                     queryDeep += " limit 2000";
                 }
                 String finalQueryDeep = queryDeep;
                records = session.readTransaction(tx -> tx.run(finalQueryDeep).list());
             }
        }
         if(deep==1) {
             records.forEach(record -> {
                 transform(record, listNode, listNodeId, indexNode, listRelationShipsMedia, listKeyMedia, deep);
             });
             processMonthSubDay(session, listNodeId, listRelationShipsMedia, startTime, endTime, typeRelation, typeData);
             processResultNew(records, listNode, listRelationShipsMedia, setNode, relationshipsResponse, deep, countNode, indexNode, ids, page, checkNodeId);
         }else {
             Map<Long,String> relationIdToKey = new HashMap<>();
             if(!queryDeep.isEmpty()){
                 Set<Integer> idNode = new HashSet<>();
                 Set<java.lang.Object> listKeyDeep2= new HashSet<>();

                 String queryRelation = queryDeep + " return distinct id(r1) as r1 ,collect(id(r2)) as r2 limit 1000";
                 records = session.readTransaction(tx -> tx.run(queryRelation).list());
                 records.forEach(record -> {
                     transform(record, listRelationShipsMedia, listKeyMedia, listKeyDeep2,idNode,relationIdToKey);
                 });
                 String queryDeep2;
                 List<java.lang.Object> idList = new ArrayList<>(listKeyDeep2);
                 if(typeData!=null&&!typeData.isEmpty()) {
                     queryDeep2 = "  Match (a:Object)-[r1:" + typeMedia(typeData) + "]->(b:Object) where id(r1) in "+ idList.toString() + " return  id(a) as start ,r1,id(b) as end ,id(r1) as id";
                 }else {
                      queryDeep2 = " Match (a:Object)-[r1]->(b:Object) where id(r1) in "+ idList.toString() +" return  id(a) as start ,r1,id(b) as end ,id(r1) as id";
                 }

                 String finalQueryRelation = "use metacenv1.metacenmonth "+ queryDeep2;
                 var  recordRelation= session.readTransaction(tx -> tx.run(finalQueryRelation).list());

                 recordRelation.stream().forEach(
                         record -> {
                             transform(record, listRelationShipsMedia, listKeyMedia,idNode,relationIdToKey);
                         }
                 );
                 String queryNode = "use metacenv1.metacenmonth Match (a:Object) where id(a) in "+ idNode.toString() +" return a,id(a)as id";
                 var  recordNodes= session.readTransaction(tx -> tx.run(queryNode).list());
                 Map<Integer,Object> mapNode = new HashMap<>();
                 recordNodes.stream().forEach(
                         record -> {
                             transform(record, listNode);
                         }
                 );
                 processMonthSubDay(session, listNodeId, listRelationShipsMedia, startTime, endTime, typeRelation, typeData);
                 processDeep2(records, listNode, listRelationShipsMedia, setNode, relationshipsResponse, deep, countNode, indexNode, ids, page,relationIdToKey);
             }
         }
        //        processResultNew(records,listNode,listRelationShipsMedia,setNode,relationshipsResponse,deep);
    }


    private void countNode(Session session, String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly,String ids,Integer page, Map<String,Long> countNode,String key) throws ParseException {
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = dff.parse(fromDate);
        Date endTime = dff.parse(toDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        if(key.contains("metacenmonth")) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.DAY_OF_MONTH,1);
        }
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        String query= "use metacenv1."+key+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
        if(deep==2) {
            query +="with a1 , count(DISTINCT a2) as a2 where a1>100 return a1,a2 limit 500 ";
        }else {
            query +=" with a , count(DISTINCT a1) as a1 where a1>100 return a,a1 limit 500  ";
        }

        String finalQuery = query;
        var records= session.readTransaction(tx -> tx.run(finalQuery).list());
        records.forEach(record -> {
            if(deep==2) {
                String idss = record.get("a1").get("mmsi").asString();
                Long count = record.get("a2").asLong();
                countNode.put(idss,count);
            }else {
                String idss = record.get("a").get("mmsi").asString();
                Long count = record.get("a1").asLong();
                countNode.put(idss,count);
            }
        });
    }

    private void processDay(Session session, String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly,String ids,Integer page, List<String> nodeImportant, Set<Object> setNode , Set<Relationships> relationshipsResponse) throws ParseException {

        System.out.println(fromDate +" "+toDate+ " " +ip + " " +type+ " " +typeRelation+ " "+deep+ " " +typeData + " " +dataSource+ " "  +exactly+ " "  + ids+ " " + page);
        Set<java.lang.Object> objectList = new HashSet<>();
        Set<String> nodes= new HashSet<>();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> listNode = new HashMap<>();
        Map<String,Integer> listNodeId = new HashMap<>();
        Map<String, Relationships> listRelationShipsMedia = new HashMap<>();
        Map<String,Integer> indexNode = new HashMap<>();
        Map<String,Long> countNode = new HashMap<>();
        List<String> listKeyMedia = new ArrayList<>();
        Date startTime = dff.parse(fromDate);
        Date endTime = dff.parse(toDate);
        indexNode.put("index",1);

        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.DAY_OF_MONTH,1);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        String query = "use metacenv1.metacenday ";
        List<String> nodeIds = new ArrayList<>();
        Boolean check = true;
        Boolean checkNodeId = false;
        countNode(session,fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,ids,page,countNode,"metacenday ");


        if(nodeImportant.size()>=10) {
            query += createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
            if (deep == 2) {
                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 100";
            } else {
                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 100";
            }
            //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
            String finalQuery1 = query;
            var records = session.readTransaction(tx -> tx.run(finalQuery1).list());

            processAddIds(records, deep, ids, objectList,countNode,nodes);
            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
            if(nodeIds.size()>1000){
                query ="use metacenv1.metacenday "+ createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                if (deep == 2) {
                    query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 100";
                } else {
                    query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 100";
                }
                //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
                String finalQuery2 = query;
                records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                objectList = new LinkedHashSet<>();
                processAddIds(records, deep, ids, objectList,countNode,nodes);
                nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                checkNodeId=true;
            }
            int indexPage = 1;
            while (records.isEmpty()) {
//                    if(ip==null|| ip.isEmpty())
//                        break;
                if (nodeImportant.size() == 10) {
                    indexPage = 0;
                    nodeImportant = listNodeImportant(dff.format(cal.getTime()), toDate, 50, 10);
                    query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                    if (deep == 2) {
                        query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 100";
                    } else {
                        query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 100";
                    }
                    String finalQuery2 = query;
                    records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                    objectList = new LinkedHashSet<>();
                    processAddIds(records, deep, ids, objectList,countNode,nodes);
                    nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                    if(nodeIds.size()>1000){
                        query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                        if (deep == 2) {
                            query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 100";
                        } else {
                            query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 100";
                        }
                        String finalQuery3 = query;
                        records = session.readTransaction(tx -> tx.run(finalQuery3).list());
                        objectList = new LinkedHashSet<>();
                        processAddIds(records, deep, ids, objectList,countNode,nodes);
                        nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                        checkNodeId=true;
                    }
                    indexPage++;
                } else {
                    if (nodeImportant.size() == 50) {
                        indexPage = 0;
                        nodeImportant = listNodeImportant(dff.format(cal.getTime()), toDate, 100, 50);
                        query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                        if (deep == 2) {
                            query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 100";
                        } else {
                            query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 100";
                        }
                        String finalQuery2 = query;
                        records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                        processAddIds(records, deep, ids, objectList,countNode,nodes);
                        nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                        if(nodeIds.size()>1000){
                            query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                            if (deep == 2) {
                                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 100";
                            } else {
                                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 100";
                            }
                            String finalQuery3 = query;
                            records = session.readTransaction(tx -> tx.run(finalQuery3).list());
                            objectList = new LinkedHashSet<>();
                            processAddIds(records, deep, ids, objectList,countNode,nodes);
                            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                            checkNodeId=true;
                        }

                        indexPage++;
                    } else if (nodeImportant.size() == 100) {
                        indexPage = 0;
                        nodeImportant = listNodeImportant(dff.format(cal.getTime()), toDate, 200, 100);
                        query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                        if (deep == 2) {
                            query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 100";
                        } else {
                            query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 100";
                        }

                        String finalQuery2 = query;
                        records = session.readTransaction(tx -> tx.run(finalQuery2).list());
                        objectList = new LinkedHashSet<>();
                        processAddIds(records, deep, ids, objectList,countNode,nodes);
                        nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                        if(nodeIds.size()>1000){
                            query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
                            if (deep == 2) {
                                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..20] as a2 limit 100";
                            } else {
                                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..20] as a1 limit 100";
                            }
                            String finalQuery3 = query;
                            records = session.readTransaction(tx -> tx.run(finalQuery3).list());
                            objectList = new LinkedHashSet<>();
                            processAddIds(records, deep, ids, objectList,countNode,nodes);
                            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
                            checkNodeId=true;
                        }
                        indexPage++;
                    } else {
                        break;
                    }
                }

            }
            if(records.isEmpty()){
                check = false;
                query= "use metacenv1.metacenday "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
                if(deep==2) {
                    query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 500 ";
                }else {
                    query += "  with  a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1, count(distinct a1.mmsi) as co return a,a1 order by co desc  limit 100";
                }
            }else {
                if((nodeIds!=null && !nodeIds.isEmpty()&&nodeIds.size()<200)|| nodeIds.isEmpty()){
                    check = false;
                    query= "use metacenv1.metacenday "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
                    if(deep==2) {
                        query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 500";
                    }else {
                        query += " with  a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1, count(distinct a1.mmsi) as co return a,a1 order by co desc  limit 100";
                    }

                }
            }
        }else {
            check=false;
            query= "use metacenv1.metacenday "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
            if(deep==2) {
                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 500";
            }else {
                query += " with  a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1, count(distinct a1) as co return a,a1 order by co desc  limit 100";
            }
        }


        String finalQuery3 = query;
        var records= session.readTransaction(tx -> tx.run(finalQuery3).list());
        objectList = new HashSet<>();
        nodes=new HashSet<>();
        processAddIds(records,deep,ids,objectList,countNode,nodes);
        nodeIds = objectList.stream().map((item) ->item.toString()).collect(Collectors.toList());
        if(nodeIds.size()>1000){
            if(check) {
                query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant);
            }else {
                query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, null);
            }
            if (deep == 2) {
                query += " return a1 , COLLECT(DISTINCT a2.mmsi)[0..100] as a2 limit 50";
            } else {
                query += " return a , COLLECT(DISTINCT a1.mmsi)[0..100] as a1 limit 50";
            }
            String finalQuery4 = query;
            records = session.readTransaction(tx -> tx.run(finalQuery4).list());
            objectList = new LinkedHashSet<>();
            processAddIds(records, deep, ids, objectList,countNode,nodes);
            nodeIds = objectList.stream().map((item) -> item.toString()).collect(Collectors.toList());
            checkNodeId=true;
        }

        if(ids!=null&& !ids.isEmpty()){
            processFilterIDsPageDay(session,dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,ids,objectList,page,checkNodeId);
            nodeIds = objectList.stream().map((item) ->item.toString()).collect(Collectors.toList());

        }
        String queryDeep = "";

        if(nodeIds!=null && !nodeIds.isEmpty()) {
            if (check == true) {
//                query= "use metacenv1.metacenday "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                if(deep==2) {
//                    query +=" return a1 , count(DISTINCT a2) as a2 ";
//                }else {
//                    query +=" return a , count(DISTINCT a1) as a1 ";
//                }
//
//                String finalQuery = query;
//                records= session.readTransaction(tx -> tx.run(finalQuery).list());
//                records.forEach(record -> {
//                    if(deep==2) {
//                        String idss = record.get("a1").get("id").asString();
//                        Long count = record.get("a2").asLong();
//                        countNode.put(idss,count);
//                    }else {
//                        String idss = record.get("a").get("id").asString();
//                        Long count = record.get("a1").asLong();
//                        countNode.put(idss,count);
//                    }
//
//
//                });

                query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, nodeImportant, nodeIds);
                queryDeep=query;
            } else {
//                query= "use metacenv1.metacenday "+ createQueryUpdate(dff.format(cal.getTime()),toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
//                if(deep==2) {
//                    query +=" return a1 , count(DISTINCT a2) as a2 ";
//                }else {
//                    query +=" return a , count(DISTINCT a1) as a1 ";
//                }
//
//                String finalQuery = query;
//                records= session.readTransaction(tx -> tx.run(finalQuery).list());
//                records.forEach(record -> {
//                    if(deep==2) {
//                        String idss = record.get("a1").get("mmsi").asString();
//                        Long count = record.get("a2").asLong();
//                        countNode.put(idss,count);
//                    }else {
//                        String idss = record.get("a").get("mmsi").asString();
//                        Long count = record.get("a1").asLong();
//                        countNode.put(idss,count);
//                    }
//                });
                if(ip==null||ip.isEmpty()) {
                    query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, new ArrayList<>(nodes), nodeIds);
                }else {
                    query = "use metacenv1.metacenday " + createQueryUpdate(dff.format(cal.getTime()), toDate, ip, type, typeRelation, deep, typeData, dataSource, exactly, new ArrayList<>(nodes), nodeIds);
                }
                queryDeep=query;
            }
            if(deep==1){
                if(check==false){
                    queryDeep += " limit 2000";
                }else {
                    queryDeep += " limit 2000";
                }
                String finalQueryDeep = queryDeep;
                records = session.readTransaction(tx -> tx.run(finalQueryDeep).list());
            }
        }
        if(deep==1) {
            records.forEach(record -> {
                transform(record, listNode, listNodeId, indexNode, listRelationShipsMedia, listKeyMedia, deep);
            });
            processResultNew(records, listNode, listRelationShipsMedia, setNode, relationshipsResponse, deep, countNode, indexNode, ids, page, checkNodeId);
            System.out.println("xong" + new Date().toString());
        }else {
            Map<Long, String> relationIdToKey = new HashMap<>();
            if (!queryDeep.isEmpty()) {
                Set<Integer> idNode = new HashSet<>();
                Set<java.lang.Object> listKeyDeep2 = new HashSet<>();
                String queryRelation = queryDeep + " return distinct id(r1) as r1,collect(id(r2)) as r2 ";
                records = session.readTransaction(tx -> tx.run(queryRelation).list());
                records.forEach(record -> {
                    transform(record, listRelationShipsMedia, listKeyMedia, listKeyDeep2, idNode, relationIdToKey);
                });
                String queryDeep2;
                if (typeData != null && !typeData.isEmpty()) {
                    queryDeep2 = "use metacenv1.metacenday Match (a:Object)-[r1:" + typeMedia(typeData) + "]->(b:Object) where id(r1) in " + listKeyDeep2.toString() + " return id(a) as start ,r1,id(b) as end ,id(r1) as id";
                } else {
                    queryDeep2 = "use metacenv1.metacenday Match (a:Object)-[r1]->(b:Object) where id(r1) in " + listKeyDeep2.toString() + " return id(a) as start ,r1,id(b) as end ,id(r1) as id";
                }

                var recordRelation = session.readTransaction(tx -> tx.run(queryDeep2).list());
                recordRelation.stream().forEach(
                        record -> {
                            transform(record, listRelationShipsMedia, listKeyMedia, idNode, relationIdToKey);
                        }
                );
                String queryNode = " Match (a:Object) where id(a) in " + idNode.toString() + " return a,id(a)as id";
                var recordNodes = session.readTransaction(tx -> tx.run(queryNode).list());
                Map<Integer, Object> mapNode = new HashMap<>();
                recordNodes.stream().forEach(
                        record -> {
                            transform(record, listNode);
                        }
                );
                processDeep2(records, listNode, listRelationShipsMedia, setNode, relationshipsResponse, deep, countNode, indexNode, ids, page, relationIdToKey);

            }
        }
        //        processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//        processResult(records,listNode,listRelationShipsMedia,setNode,relationshipsResponse,deep);
    }
    public void processAddIds(List<Record> records, Integer deep, String ids,Set<java.lang.Object> objectList ){
        records.forEach(record -> {
            List<java.lang.Object> idss;
            String id ;
            if(deep==1) {
                id =record.get("a").get("mmsi").asString();
                idss= record.get("a1").asList();
            }else {
                id = record.get("a1").get("mmsi").asString();
                idss = record.get("a2").asList();
            }
            if(ids!=null&&!ids.isEmpty()) {
                if (!id.equals(ids)) {
                    objectList.addAll(idss);
                }
            }else {
                objectList.addAll(idss);
            }
//            objectList.add(id);


        });
    }

    public void processAddIds(List<Record> records, Integer deep, String ids,Set<java.lang.Object> objectList,Map<String,Long> countNode ,Set<String> nodes){
        records.forEach(record -> {
            List<java.lang.Object> idss;
            String id ;
            if(deep==1) {
                id =record.get("a").get("mmsi").asString();
                idss= record.get("a1").asList();
            }else {
                id = record.get("a1").get("mmsi").asString();
                idss = record.get("a2").asList();
            }

            if(countNode.get(id)!=null){
                if(idss.size()>15)
                    idss=idss.subList(0,15);
            }
            if(ids!=null&&!ids.isEmpty()) {
                if (!id.equals(ids)) {
                    objectList.addAll(idss);
                }
            }else {
                objectList.addAll(idss);
            }
            nodes.add(id);
//            objectList.add(id);


        });
    }

//    public ResponseTopo getTopoTest(String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
//            exactly) throws ParseException {
//
//        var nodes = new ArrayList<>();
//        var links = new ArrayList<>();
//        String key="use metacenv1.";
//        ResponseTopo result = new ResponseTopo();
//        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        DateFormat df = new SimpleDateFormat("yyyy-MM");
//        DateFormat dfYear = new SimpleDateFormat("yyyy");
//        DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
//        Set<Object> setNode = new LinkedHashSet<>();
//        Set<Relationships> relationshipsResponse = new LinkedHashSet<>();
//        Date startTime = dff.parse(fromDate);
//        Date endTime = dff.parse(toDate);
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(startTime);
//        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//        cal.set(Calendar.HOUR_OF_DAY,0);
//        cal.clear(Calendar.MINUTE);
//        cal.clear(Calendar.SECOND);
//        cal.clear(Calendar.MILLISECOND);
//        String query="";
//        Map<String, Object> listNode = new HashMap<>();
//        Map<String,Integer> listNodeId = new HashMap<>();
//        Map<String, Relationships> listRelationShipsAis = new HashMap<>();
//        Map<String, Relationships> listRelationShipsMedia = new HashMap<>();
//        Map<String,Integer> indexNode = new HashMap<>();
//        List<String> listKeyMedia = new ArrayList<>();
//        List<String> listKeyAis = new ArrayList<>();
//        indexNode.put("index",1);
//        try (Session session = sessionFor(database())) {
//            if (df.format(startTime).equals(df.format(endTime))) {
//                Calendar calStart = Calendar.getInstance();
//                calStart.setTime(startTime);
//                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                Calendar calEnd = Calendar.getInstance();
//                calEnd.setTime(endTime);
//                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                calEnd.set(Calendar.HOUR_OF_DAY, 0);
//                calEnd.clear(Calendar.MINUTE);
//                calEnd.clear(Calendar.SECOND);
//                calEnd.clear(Calendar.MILLISECOND);
//                query = "use metacenv1.vsatmonth ";
//                List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                query+= createQueryUpdate(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
////                query = "use metacenv1.vsatmonth match p= (a:Object)-[r1] -(a1:Object)  where  ";
////                query += " a.names in [";
////                for (String node : nodeImportant
////                ) {
////                    query += "'" + node + "',";
////                }
////                query = query.substring(0, query.length() - 1);
////                query += "] ";
//                query +=" return a , COLLECT(a1.names)[0..50] as a1 ";
//                String finalQuery1 = query;
//
//
//                var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//
//                Set<java.lang.Object> objectList = new LinkedHashSet<>();
//                records.forEach(record -> {
//                    String ids = record.get("a").get("ids").asString();
//                    List<java.lang.Object> idss = record.get("a1").asList();
//                    objectList.addAll(idss);
//
//                });
//                List<String> nodeIds = objectList.stream().map((item) ->item.toString()).collect(Collectors.toList());
//                records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use metacenv1.vsatmonth ");
//                records.forEach(record -> {
//                    transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//                });
//                processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                        ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//
//            } else {
//                Calendar calStart = Calendar.getInstance();
//                calStart.setTime(startTime);
//                calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                Calendar calEnd = Calendar.getInstance();
//                calEnd.setTime(endTime);
//                calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
//                calEnd.set(Calendar.HOUR_OF_DAY, 0);
//                calEnd.clear(Calendar.MINUTE);
//                calEnd.clear(Calendar.SECOND);
//                calEnd.clear(Calendar.MILLISECOND);
//                if (dfYear.format(startTime).equals(dfYear.format(endTime))) {
//                    if (endTime.getMonth() - startTime.getMonth() <= 6) {
//                        query = "use metacenv1.vsatmonth ";
//                        List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                        query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                        //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                        String finalQuery1 = query;
//                        var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                        records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use metacenv1.vsatmonth ");
//                        System.out.println("xong" + new Date().toString());
//                        records.forEach(record -> {
//                            transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//                        });
//                        processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                        processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//
//                    } else {
//                        query = "use metacenv1.vsatyear ";
//                        List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                        query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                        //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                        String finalQuery1 = query;
//                        var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                        int indexPage = 1;
//                        records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use metacenv1.vsatyear ");
//                        records.forEach(record -> {
//                            transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//
//                        });
//                        processYearSubMonth(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                        processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                                ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                        processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//                    }
//                } else {
//
//                    //trên 2 năm
//                    query = "use metacenv1.vsatyear ";
//                    List<String> nodeImportant = listNodeImportant(fromDate,toDate,5,0);
//                    query+= createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
//                    //"MATCH (a:Object) - [r1:MEDIA] -> (a1:Object) - [r2:MEDIA] -> (a2:Object)  where r1.startTime >= '" + fromDate + "' and r1.startTime < '" + toDate + "' and a<>a2  RETURN a,r1,a1,r2,a2 limit 20000 ";
//                    String finalQuery1 = query;
//                    var records = session.readTransaction(tx -> tx.run(finalQuery1).list());
//                    int indexPage = 1;
//                    records = processQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,session,records,nodeImportant," use metacenv1.vsatyear ");
//                    System.out.println("xong" + new Date().toString());
//                    records.forEach(record -> {
//                        transform(record, listNode, listNodeId, indexNode, listRelationShipsAis, listRelationShipsMedia, listKeyMedia, listKeyAis,deep);
//
//                    });
//                    processYearSubMonth(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                            ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                    processMonthSubDay(session,listNode,listNodeId,indexNode,listRelationShipsAis,listRelationShipsMedia,listKeyMedia
//                            ,listKeyAis,startTime,endTime,typeRelation,typeData);
//                    processResult(records,listNode,listRelationShipsAis,listRelationShipsMedia,setNode,relationshipsResponse,deep);
//                }
//            }
//        }
//        result.setNodes(new ArrayList<>(setNode));
//        result.setRelationships(new ArrayList<>(relationshipsResponse));
//
//        return result;
//    }

    private List<String> listNodeImportant(String startTime, String endTime, Integer size, Integer page){
        return metaCenMediaService.findNodeImportant(startTime,endTime,size,page);
    }

    private List<Record> processQuery(String fromDate, String toDate, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly,Session session, List<Record> records,List<String> nodeImportant, String dbName){
        int indexPage =0;
        String query="";
        while (records.isEmpty()){
//                    if(ip==null|| ip.isEmpty())
//                        break;
            if(nodeImportant.size()==5){
                indexPage=0;
                nodeImportant = listNodeImportant(fromDate,toDate,50,indexPage);
                query= dbName+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
                String finalQuery2 = query;
                records= session.readTransaction(tx -> tx.run(finalQuery2).list());
                indexPage++;
            }else {
                if(nodeImportant.size()==50){
                    indexPage=0;
                    nodeImportant = listNodeImportant(fromDate,toDate,100,indexPage);
                    query= dbName+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
                    String finalQuery2 = query;
                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
                    indexPage++;
                } else  if(nodeImportant.size()==100){
                    indexPage=0;
                    nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
                    query= dbName+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
                    String finalQuery2 = query;
                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
                    indexPage++;
                } else  if(nodeImportant.size()==200){
                    if(indexPage>10){
                        break;
                    }
                    nodeImportant = listNodeImportant(fromDate,toDate,200,indexPage);
                    query= dbName+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,nodeImportant);
                    String finalQuery2 = query;
                    records= session.readTransaction(tx -> tx.run(finalQuery2).list());
                    indexPage++;
                } else {
                    break;
                }
            }

        }
        if(records.isEmpty()){
            query= "use metacenv1.metacenmonth "+ createQuery(fromDate,toDate,ip,type,typeRelation,deep,typeData,dataSource,exactly,null);
            String finalQuery2 = query;
            records= session.readTransaction(tx -> tx.run(finalQuery2).list());
        }
        return records;
    }

    private String createQuery(String startTime, String endTime, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly, List<String> nodes){
        String query="";
        if(typeRelation==0){
            query = "match  (a:Object)";
            if(type==2){
                for (int i=1;i<=deep;i++){
                    query += "-[r"+i+":MEDIA] -(a"+i+":Object)";
                }
            }else {
                for (int i=1;i<=deep;i++){
                    query += "-[r"+i+":MEDIA] -(a"+i+":Object)";
                }
            }
        } else if(typeRelation==1){
            query = "match p= (a:Object)";
            if(type==2){
                for (int i=1;i<=deep;i++){
                    query += "-[r"+i+":AIS] -(a"+i+":Object)";
                }
            }else {
                for (int i=1;i<=deep;i++){
                    query += "-[r"+i+":AIS] ->(a"+i+":Object)";
                }
            }
        } else {
            query = "match p= (a:Object)";
            if(type==2){
                for (int i=1;i<=deep;i++){
                    query += "-[r"+i+"] -(a"+i+":Object)";
                }
            }else {
                for (int i=1;i<=deep;i++){
                    query += "-[r"+i+"] ->(a"+i+":Object)";
                }
            }
        }
        query+= "  where  ";
        if(nodes!=null && !nodes.isEmpty()) {
            if (ip != null && !ip.isEmpty()) {
                query += " a1.mmsi in [";
                for (String node : nodes
                ) {
                    query += "'" + node + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "] ";
            } else {
//            query += " 1 =1";
                query += " a.mmsi in [";
                for (String node : nodes
                ) {
                    query += "'" + node + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "] ";
            }
        }else {
            query += " 1 =1";
        }


        for (int i=1;i<=deep;i++){
            if(type!=2) {
                query += " and a<>a" + i;
                if(i>=2){
                    query += " and a"+(i-1)+"<>a" + i;
                }
            }

            query+= " and  r"+i+".startTime>='" +startTime+"' and r"+i+".startTime <'"+endTime+"' ";
        }
        if(type==0){
            if(!StringUtil.isNullOrEmpty(ip) && dataSource!=null){
                if(exactly) {
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  a.ips contains '" + ip + "-" + dataSource.get(i) + "' ";
                        else
                            query += " or a.ips contains '" + ip + "-" + dataSource.get(i) + "' ";
                    }
                    query += " )";
                }else{
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\\\.");
                    ip=ip.replace("x","\\\\..*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
                    }
                    if(!ip.endsWith(".*")){
                        ip =ip+".*";
                    }
                    query+= " and(";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=  " a.ips =~ '"+ip+ "-" + dataSource.get(i) + ".*' ";
                        else
                            query +=  " or a.ips =~ '"+ip+ "-" + dataSource.get(i) + ".*' ";
                    }
                    query += " )";

                }
            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\\\.");
                    ip=ip.replace("x","\\\\..*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
                    }
                    if(!ip.endsWith(".*")){
                        ip +=".*";
                    }

                    query +=" and a.ips =~ '"+ip+ "' ";
                } else if(dataSource!=null){
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  a.ips =~ '"  + ".*-" + dataSource.get(i) + ".*' ";
                        else
                            query += " or a.ips =~ '" + ".*-" + dataSource.get(i) + ".*' ";
                    }
                    query += " )";
                }
            }
        } else if(type==1){
            if(!StringUtil.isNullOrEmpty(ip) && dataSource!=null){
                if(exactly) {
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=" a"+deep+".ips contains '"+ip+ "-"+dataSource.get(i)+"' ";
                        else
                            query +=" or a"+deep+".ips contains '"+ip+ "-"+dataSource.get(i)+"' ";
                    }
                    query += " )";

                }else {

                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\\\.");
                    ip=ip.replace("x","\\\\..*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
                    }
                    if(!ip.endsWith(".*")){
                        ip =ip+".*";
                    }
                    query+= " and(";
//                    ip=ip.replace(".","\\.");
//                    query+= " and  a"+deep+".ips contains '" + ip + "' and(";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=  " a"+deep+".ips =~ '"+ip+"-" + dataSource.get(i) + ".*' ";
                        else
                            query +=  " or a"+deep+".ips =~ '"+ip+"-" + dataSource.get(i) + ".*' ";
                    }
                    query += " )";

                }

            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\\\.");
                    ip=ip.replace("x","\\\\..*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
                    }
                    if(!ip.endsWith(".*")){
                        ip +=".*";
                    }
                    query +=" and a"+deep+".ips =~ '"+ip+ "' ";
                } else if(dataSource!=null){
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  a"+deep+".ips =~ '.*"  + "-" + dataSource.get(i) + ".*' ";
                        else
                            query += " or a"+deep+".ips =~ '.*" + "-" + dataSource.get(i) + ".*' ";
                    }
                    query += " )";
                }
            }
        } else {
            if(!StringUtil.isNullOrEmpty(ip) && dataSource!=null){
                if(exactly) {
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=" a.ips contains '"+ip+ "-"+dataSource.get(i)+"'";
                        else
                            query +=" or a.ips contains '"+ip+ "-"+dataSource.get(i)+"'";
                    }
                    query += " )";

                }else {
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\\\.");
                    ip=ip.replace("x","\\\\..*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
                    }
                    if(!ip.endsWith(".*")){
                        ip =ip+".*";
                    }
                    ip=ip.trim();
//                    query +=" and a"+deep+".ips =~ '"+ip+ "' ";
//                    ip=ip.replace(".","\\.");
                    query+= " and(";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=  " a.ips =~ '"+ip+ "-" + dataSource.get(i) + ".*' ";
                        else
                            query +=  " or a.ips =~ '"+ip+ "-" + dataSource.get(i) + ".*' ";
                    }
                    query += " )";
                }

            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\\\.");
                    ip=ip.replace("x","\\\\..*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
                    }
                    if(!ip.endsWith(".*")){
                        ip +=".*";
                    }

                    query +=" and a.ips =~ '"+ip+ "' ";
                } else if(dataSource!=null){
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=" a.ips =~ '.*-"+dataSource.get(i)+".*' ";
                        else
                            query +=" or a.ips =~ '.*-"+dataSource.get(i)+".*' ";
                    }
                    query += " )";

                }
            }
        }

        query += "  return a ";
        for(int i=1;i<=deep;i++){
            query += ",r"+i+",a"+i;
        }

        query += " limit 2000";
        //Payload request dbm.root.url//v1.0/dbm/management/mons/recognitions
     return query;
    }
    private String typeMedia(List<Integer> typeData){
        String query = "";
        if(typeData!=null) {
            for (Integer data : typeData
            ) {
                switch (data) {
                    case 0:
                        query += "Ais|";
                        break;
                    case 1:
                        query += "Voice|";
                        break;
                    case 2:
                        query += "Video|";
                        break;
                    case 3:
                        query += "Web|";
                        break;
                    case 4:
                        query += "Email|";
                        break;
                    case 5:
                        query += "TransferFile|";
                        break;
                    case 6:
                        query += "khac|";
                        break;
                    case 8:
                        query += "UNDEFINED|";
                        break;

                }
            }
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }

    private String createQueryUpdate(String startTime, String endTime, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly, List<String> nodes){
        String query="";
        if(typeData!=null&&!typeData.isEmpty()){
            query = "match  (a:Object)";
            for (int i=1;i<=deep;i++){
                query += "-[r"+i+":"+typeMedia(typeData)+"] -(a"+i+":Object)";
            }
        }else {
            query = "match  (a:Object)";
            for (int i=1;i<=deep;i++){
                query += "-[r"+i+"] -(a"+i+":Object)";
            }
        }
        query+= "  where  ";
        if(nodes!=null && !nodes.isEmpty()) {
            if (ip != null && !ip.isEmpty()) {
                query += " a1.mmsi in [";
                for (String node : nodes
                ) {
                    query += "'" + node + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "] ";
            } else {
//            query += " 1 =1";
                query += " a.mmsi in [";
                for (String node : nodes
                ) {
                    query += "'" + node + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "] ";
            }
        }else {
            query += " 1 =1";
        }

        String nodeIndex ="";
        if(type==1){
            nodeIndex =String.valueOf(deep);
        }
        for (int i=1;i<=deep;i++){
            query += " and a<>a" + i;
            if(i>=2){
                query += " and a"+(i-1)+"<>a" + i;
            }

            query+= " and  r"+i+".startTime>='" +startTime+"' and r"+i+".startTime <='"+endTime+"' ";
            if(dataSource!=null&&!dataSource.isEmpty()){
                for (int j=0;j<dataSource.size();j++) {
                    if(j==0) {
                        query += " and (( r" + i + ".dataSource CONTAINS '" + dataSource.get(j)+"')";
                    }else {
                        query += " or ( r" + i + ".dataSource CONTAINS '" + dataSource.get(j)+"')";
                    }
                    if(j==dataSource.size()-1){
                        query+=")";
                    }
                }
            }

        }

        if(!StringUtil.isNullOrEmpty(ip)){
            ip=ip.trim();
            ip=ip.replace(".x","#");
            ip=ip.replace(".","\\\\.");
            ip=ip.replace("x",".*");
            ip=ip.replace("#","\\\\..*");
            if(!ip.startsWith(".*")){
                ip =".*"+ip;
            }
            if(!ip.endsWith(".*")){
                ip +=".*";
            }
            query +=" and (( r"+deep+".srcIp =~ '"+ip+ "') ";
            query +=" or ( r"+deep+".destIp =~ '"+ip+ "')) ";
        }
//
//        if(typeData!=null && !typeData.isEmpty()){
//            for (int i=1;i<=deep;i++){
//                query=queryDeepRelation(query,typeData,"r"+i);
//            }
//        }
//        query += "  return a ";
//        for(int i=1;i<=deep;i++){
//            query += ",r"+i+",a"+i;
//        }
//
//        query += " limit 2000";
        //Payload request dbm.root.url//v1.0/dbm/management/mons/recognitions
        return query;
    }
    private String createQueryUpdate(String startTime, String endTime, String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData, List<Integer> dataSource, Boolean
            exactly, List<String> nodes,List<String> node2){
        String query="";
        if(typeData!=null&&!typeData.isEmpty()){
            query = "match  (a:Object)";
            for (int i=1;i<=deep;i++){
                query += "-[r"+i+":"+typeMedia(typeData)+"] -(a"+i+":Object)";
            }
        }else {
            query = "match  (a:Object)";
            for (int i=1;i<=deep;i++){
                query += "-[r"+i+"] -(a"+i+":Object)";
            }
        }
        query+= "  where  ";
        if(nodes!=null && !nodes.isEmpty()) {
                query += " a.mmsi in [";
                for (String node : nodes
                ) {
                    query += "'" + node + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "] ";
        }else {
            query += " 1 =1";
        }
        query += " and ";
        if(node2!=null && !node2.isEmpty()) {
            if (ip != null && !ip.isEmpty()) {
                if(deep==1){
                    query += " a1.mmsi in [";
                }else {
                    query += " a2.mmsi in [";
                }

                for (String node : node2
                ) {
                    query += "'" + node + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "] ";
            } else {
                if(deep==1){
                    query += " a1.mmsi in [";
                }else {
                    query += " a2.mmsi in [";
                }
//            query += " 1 =1";
                for (String node : node2
                ) {
                    query += "'" + node + "',";
                }
                query = query.substring(0, query.length() - 1);
                query += "] ";
            }
        }else {
            query += " 1 =1";
        }
        String nodeIndex ="1";
        if(type==1){
            nodeIndex =String.valueOf(deep);
        }
        for (int i=1;i<=deep;i++){
            query += " and a<>a" + i;
            if(i>=2){
                query += " and a"+(i-1)+"<>a" + i;
            }

            query+= " and  r"+i+".startTime>='" +startTime+"' and r"+i+".startTime <='"+endTime+"' ";
            if(dataSource!=null&&!dataSource.isEmpty()){
                for (int j=0;j<dataSource.size();j++) {
                    if(j==0) {
                        query += " and (( r" + i + ".dataSource CONTAINS '" + dataSource.get(j)+"')";
                    }else {
                        query += " or ( r" + i + ".dataSource CONTAINS '" + dataSource.get(j)+"')";
                    }
                    if(j==dataSource.size()-1){
                        query+=")";
                    }
                }
            }

        }

        if(!StringUtil.isNullOrEmpty(ip)){
            ip=ip.trim();
            ip=ip.replace(".x","#");
            ip=ip.replace(".","\\\\.");
            ip=ip.replace("x",".*");
            ip=ip.replace("#","\\\\..*");
            if(!ip.startsWith(".*")){
                ip =".*"+ip;
            }
            if(!ip.endsWith(".*")){
                ip +=".*";
            }
            query +=" and (( r"+nodeIndex+".srcIp =~ '"+ip+ "') ";
            query +=" or ( r"+nodeIndex+".destIp =~ '"+ip+ "')) ";
        }
        if(deep==1) {
            query += "  return a ";
            for (int i = 1; i <= deep; i++) {
                query += ",r" + i + ",a" + i;
            }
        }
        return query;
    }

    private String queryDeepRelation(String query, List<Integer> typeData, String r){
        for (Integer i=0; i<typeData.size();i++
        ) {

            Integer tmp = typeData.get(i);
            switch (tmp){
                case 0:
                    if(i==0){
                        query +=  " and ( ( type("+r+")='AIS' and "+r+".count >0)";
                    }else
                        query +=  " or( type("+r+")='AIS' and "+r+".count >0)";
                    break;
                case 1:
                    if(i==0){
                        query +=  " and (  "+r+".VoiceCount >0";
                    }else
                        query +=  " or "+r+".VoiceCount >0";
                    break;
                case 2:
                    if(i==0){
                        query +=  " and (  "+r+".VideoCount >0";
                    }else
                        query +=  " or "+r+".VideoCount >0";
                    break;
                case 3:
                    if(i==0){
                        query +=  " and (  "+r+".WebCount >0";
                    }else
                        query +=  " or "+r+".WebCount >0";
                    break;
                case 4:
                    if(i==0){
                        query +=  " and (  "+r+".EmailCount >0";
                    }else
                        query +=  " or "+r+".EmailCount >0";
                    break;
                case 5:
                    if(i==0){
                        query +=  " and (  "+r+".TransferFileCount >0";
                    }else
                        query +=  " or "+r+".TransferFileCount >0";
                    break;
                case 6:
                    if(i==0){
                        query +=  " and (  "+r+".khac >0";
                    }else
                        query +=  " or "+r+".khac >0";
                    break;
                case 8:
                    if(i==0){
                        query +=  " and (  "+r+".UNDEFINEDCount >0";
                    }else
                        query +=  " or "+r+".UNDEFINEDCount >0";
                break;
            }

        }
        query +=")";
        return query;
    }

    private void processMonthSubDay(Session session, Map<String,Integer> listNodeId
            , Map<String, Relationships> listRelationShips,Date startTime,Date endTime,Integer typeRelation , List<Integer> typeData) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);
            cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String query ="";
            Calendar calStart = Calendar.getInstance();
            calStart.setTime(startTime);
            calStart.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endTime);
            calEnd.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calEnd.set(Calendar.HOUR_OF_DAY, 0);
            calEnd.clear(Calendar.MINUTE);
            calEnd.clear(Calendar.SECOND);
            calEnd.clear(Calendar.MILLISECOND);

            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(startTime);
            calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.DAY_OF_MONTH, 1);
            calFrom.clear(Calendar.MINUTE);
            calFrom.clear(Calendar.SECOND);
            calFrom.clear(Calendar.MILLISECOND);
            int date = calFrom.getTime().getDate();
            if (startTime.getDate() != calFrom.getTime().getDate()) {
                if(typeData!=null&&!typeData.isEmpty()) {
                    query = "use metacenv1.metacenday MATCH (a:Object) - [r1:" + typeMedia(typeData) + "] -> (a1:Object)  where r1.startTime >= '" + dff.format(calFrom.getTime()) + "' and r1.startTime < '" + dff.format(cal.getTime()) + "' ";
                }else {
                    query = "use metacenv1.metacenday MATCH (a:Object) - [r1] -> (a1:Object)  where r1.startTime >= '" + dff.format(calFrom.getTime()) + "' and r1.startTime < '" + dff.format(cal.getTime()) + "' ";
                }
                query += " return r1";
                String finalQuery2 = query;
                var recordStart = session.readTransaction(tx -> tx.run(finalQuery2).list());
                recordStart.forEach(record -> {
                    transformSub(record,listNodeId,listRelationShips);
                });
            }

            calFrom.setTime(endTime);
            calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.DAY_OF_MONTH, 1);
            calFrom.clear(Calendar.MINUTE);
            calFrom.clear(Calendar.SECOND);
            calFrom.clear(Calendar.MILLISECOND);
            calFrom.add(Calendar.MONTH, 1);
            calFrom.add(Calendar.DAY_OF_MONTH, -1);
            if (calFrom.getTime().getDate() != endTime.getDate() && calEnd.getTime().getDate()!=1) {
                if(typeData!=null&&!typeData.isEmpty()) {
                    query = "use metacenv1.metacenday MATCH (a:Object) - [r1:"+typeMedia(typeData)+"] -> (a1:Object)  where r1.startTime >= '" + dff.format(calEnd.getTime()) + "' and r1.startTime < '" +  dff.format(calFrom.getTime()) + "' ";
                }else {
                    query = "use metacenv1.metacenday MATCH (a:Object) - [r1] -> (a1:Object)  where r1.startTime >= '" + dff.format(calFrom.getTime()) + "' and r1.startTime < '" + dff.format(cal.getTime()) + "' ";
                }
                query += " return r1";
//                query = "use metacenv1.metacenday MATCH (a:Object) - [r1:MEDIA] -> (a1:Object)  where r1.startTime > '" + dff.format(calEnd.getTime()) + "' and r1.startTime <= '" + dff.format(calFrom.getTime()) + "' return r1";
                String finalQuery2 = query;
                var recordStart = session.readTransaction(tx -> tx.run(finalQuery2).list());
                recordStart.forEach(record -> {
                    transformSub(record,listNodeId,listRelationShips);
                });
            }
        }

    private void processYearSubMonth(Session session, Map<String,Integer> listNodeId
            , Map<String, Relationships> listRelationShips,Date startTime,Date endTime,Integer typeRelation , List<Integer> typeData) {

        String query = "";
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calFrom = Calendar.getInstance();
        //trù tháng thừa
        calFrom.setTime(startTime);
        calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        calFrom.set(Calendar.MONTH, 0);
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.DAY_OF_MONTH, 1);
        calFrom.clear(Calendar.MINUTE);
        calFrom.clear(Calendar.SECOND);
        calFrom.clear(Calendar.MILLISECOND);
//                    int month = calFrom.getTime().getMonth();
//                    int m = startTime.getMonth();
        if (calFrom.getTime().getMonth() <= startTime.getMonth() - 1) {
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(startTime);
            calTo.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calTo.set(Calendar.HOUR_OF_DAY, 0);
            calTo.set(Calendar.DAY_OF_MONTH, 1);
            calTo.add(Calendar.MONTH, -1);
            calTo.clear(Calendar.MINUTE);
            calTo.clear(Calendar.SECOND);
            calTo.clear(Calendar.MILLISECOND);
            if(typeRelation==0) {
                query = "use metacenv1.metacenmonth MATCH (a:Object) - [r1:MEDIA] -> (a1:Object)  where r1.startTime >= '" + dff.format(calFrom.getTime()) + "' and r1.startTime < '" +  dff.format(calTo.getTime()) + "' ";
            }else if(typeRelation==1) {
                query = "use metacenv1.metacenmonth MATCH (a:Object) - [r1:AIS] -> (a1:Object)  where r1.startTime >= '" + dff.format(calFrom.getTime()) + "' and r1.startTime < '" +  dff.format(calTo.getTime()) + "' ";
            }if(typeRelation==2) {
                query = "use metacenv1.metacenmonth MATCH (a:Object) - [r1] -> (a1:Object)  where r1.startTime >= '" + dff.format(calFrom.getTime()) + "' and r1.startTime < '" +  dff.format(calTo.getTime()) + "' ";
            }
            if(typeData!=null&&!typeData.isEmpty()){
                query= queryDeepRelation(query,typeData,"r1");
            }

            query += " return r1";
            String finalQuery2 = query;
            var recordStart = session.readTransaction(tx -> tx.run(finalQuery2).list());
            recordStart.forEach(record -> {
                transformSub(record,listNodeId,listRelationShips);

            });
        }

        //trù tháng thừa cuối
        calFrom.setTime(endTime);
        calFrom.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.DAY_OF_MONTH, 1);
        calFrom.set(Calendar.MONTH, 11);
        calFrom.clear(Calendar.MINUTE);
        calFrom.clear(Calendar.SECOND);
        calFrom.clear(Calendar.MILLISECOND);
        if (endTime.getMonth() < 11) {
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(endTime);
            calTo.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            calTo.set(Calendar.HOUR_OF_DAY, 0);
            calTo.set(Calendar.DAY_OF_MONTH, 1);
            calTo.add(Calendar.MONTH, 1);
            calTo.clear(Calendar.MINUTE);
            calTo.clear(Calendar.SECOND);
            calTo.clear(Calendar.MILLISECOND);
            if(typeRelation==0) {
                query = "use metacenv1.metacenday MATCH (a:Object) - [r1:MEDIA] -> (a1:Object)  where r1.startTime >= '" + dff.format(calTo.getTime()) + "' and r1.startTime < '" +  dff.format(calFrom.getTime()) + "' ";
            }else if(typeRelation==1) {
                query = "use metacenv1.metacenday MATCH (a:Object) - [r1:AIS] -> (a1:Object)  where r1.startTime >= '" + dff.format(calTo.getTime()) + "' and r1.startTime < '" +  dff.format(calFrom.getTime()) + "' ";
            }if(typeRelation==2) {
                query = "use metacenv1.metacenday MATCH (a:Object) - [r1] -> (a1:Object)  where r1.startTime >= '" + dff.format(calTo.getTime()) + "' and r1.startTime < '" +  dff.format(calFrom.getTime()) + "' ";
            }
            if(typeData!=null&&!typeData.isEmpty()){
                query= queryDeepRelation(query,typeData,"r1");
            }
            query += " return r1";
//            query = "use fabric.metacenmonth MATCH (a:Object) - [r1:MEDIA] -> (a1:Object)  where r1.startTime >= '" + dff.format(calTo.getTime()) + "' and r1.startTime <= '" + dff.format(calFrom.getTime()) + "' return r1";
            String finalQuery2 = query;
            var recordStart = session.readTransaction(tx -> tx.run(finalQuery2).list());
            recordStart.forEach(record -> {
                transformSub(record,listNodeId,listRelationShips);

            });
        }
    }
        private void processResult(List<Record> records,Map<String, Object> listNode
                , Map<String, Relationships> listRelationShipsMedia, Set<Object> setNode , Set<Relationships> relationshipsResponse ,Integer deep){
            records.forEach(record -> {
                String type = record.get("r1").asRelationship().type();
                String key1 = record.get("r1").get("src").asString() + record.get("r1").get("dest").asString()+type;
                    if (listRelationShipsMedia.get(key1) != null && listRelationShipsMedia.get(key1).getCount() > 0) {
                        if(deep<2){
                            setNode.add(listNode.get(record.get("a").get("id").asString()));
                            setNode.add(listNode.get(record.get("a1").get("id").asString()));
                            relationshipsResponse.add(listRelationShipsMedia.get(key1));
                        } else if (deep<3){
                            type = record.get("r2").asRelationship().type();
                            String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString()+type;
                            if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                    setNode.add(listNode.get(record.get("a").get("id").asString()));
                                    setNode.add(listNode.get(record.get("a1").get("id").asString()));
                                    setNode.add(listNode.get(record.get("a2").get("id").asString()));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key2));

                            }
                        }else if (deep<4){
                            type = record.get("r2").asRelationship().type();
                            String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString()+type;
                            if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                type = record.get("r3").asRelationship().type();
                                String key3 = record.get("r3").get("src").asString() + record.get("r3").get("dest").asString()+type;
                                    if (listRelationShipsMedia.get(key3) != null && listRelationShipsMedia.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("id").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("id").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("id").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("id").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key3));

                                    }
                            }
                        }

                    }
            });


        }
    private void proceeResultFilter(List<Record> records,Map<String, Object> listNode
            , Map<String, Relationships> listRelationShipsMedia, Set<Object> setNode , Set<Relationships> relationshipsResponse ,Integer deep,
                                    Map<String, Long> countNode,Set<String> nodeIds, String ids){
        records.forEach(record -> {
            String type = record.get("r1").asRelationship().type();
            String key1 = record.get("r1").get("src").asString() + record.get("r1").get("dest").asString()+type;
            if (listRelationShipsMedia.get(key1) != null && listRelationShipsMedia.get(key1).getCount() > 0) {
                    if(deep<2){
                        setNode.add(listNode.get(record.get("a").get("mmsi").asString()));
                        Long count = countNode.get(record.get("a").get("mmsi").asString());
                        if(count!=null&& count>100){
                            if(record.get("a").get("mmsi").asString().equals(ids)) {

                                setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                                relationshipsResponse.add(listRelationShipsMedia.get(key1));
                            }else {
                                if(nodeIds.contains(record.get("a1").get("id").asString())){
                                    setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                }
                            }
//                            nodeIndex++;

                        }else {
                            setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                            relationshipsResponse.add(listRelationShipsMedia.get(key1));
                        }
                    } else if (deep<3){
                        type = record.get("r2").asRelationship().type();
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString()+type;
                        if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                setNode.add(listNode.get(record.get("a").get("mmsi").asString()));
                                setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                                Long count = countNode.get(record.get("a1").get("mmsi").asString());
                                if(count!=null&& count>100){
                                    if(record.get("a").get("mmsi").asString().equals(ids)) {
                                        setNode.add(listNode.get(record.get("a2").get("mmsi").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                    }else {
                                        if(nodeIds.contains(record.get("a2").get("mmsi").asString())){
                                            setNode.add(listNode.get(record.get("a2").get("mmsi").asString()));
                                            relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                        }
                                    }

                                }else {
                                    setNode.add(listNode.get(record.get("a2").get("mmsi").asString()));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                }
                                relationshipsResponse.add(listRelationShipsMedia.get(key1));

                            }
                    }else if (deep<4){
                        type = record.get("r2").asRelationship().type();
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString()+type;

                        if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                type = record.get("r3").asRelationship().type();
                                String key3 = record.get("r3").get("src").asString() + record.get("r3").get("dest").asString()+type;
                                if (listRelationShipsMedia.get(key3) != null && listRelationShipsMedia.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("mmsi").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("mmsi").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("mmsi").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key3));

                                }

                        }
                    }

                }

        });
    }

    private void processResultFilter2(List<Record> records,Map<String, Object> listNode
            , Map<String, Relationships> listRelationShipsMedia, Set<Object> setNode , Set<Relationships> relationshipsResponse ,Integer deep,
                                    Map<String, Long> countNode,Set<String> nodeIds, String ids,Map<Long,String> relationIdToKey){
        records.forEach(record -> {
            Long idR1 = record.get("r1").asLong();
            String key = relationIdToKey.get(idR1);
            if (listRelationShipsMedia.get(key) != null && listRelationShipsMedia.get(key).getCount() > 0) {
                if (deep == 2) {
                    List<java.lang.Object> listRelationDeep2 = record.get("r2").asList();
                    listRelationDeep2.stream().forEach((id) -> {
                        String keyR2 = relationIdToKey.get((Long) id);
                        Relationships relationships = listRelationShipsMedia.get(keyR2);
                        if (relationships != null && relationships.getCount() > 0) {
                            setNode.add(listNode.get(listRelationShipsMedia.get(key).getSrc()));
                            setNode.add(listNode.get(listRelationShipsMedia.get(key).getDest()));
                            List<String> a1 = new ArrayList<>();
                            a1.add(listRelationShipsMedia.get(key).getSrc());
                            a1.add(listRelationShipsMedia.get(key).getDest());
                            Long count = 0L;
                            if (a1.contains(relationships.getDest())) {
                                count = countNode.get(relationships.getDest());
                                if (count != null && count > 100) {
                                    String mmsiNode = "";
                                    if (listRelationShipsMedia.get(key).getSrc().equals(relationships.getDest())) {
                                        mmsiNode = listRelationShipsMedia.get(key).getDest();
                                    } else {
                                        mmsiNode = listRelationShipsMedia.get(key).getSrc();
                                    }
                                    if (mmsiNode.equals(ids)) {
                                        setNode.add(listNode.get(relationships.getSrc()));
                                        relationshipsResponse.add(relationships);
                                    } else {
                                        if (nodeIds.contains(relationships.getSrc())) {
                                            setNode.add(listNode.get(relationships.getSrc()));
                                            relationshipsResponse.add(relationships);
                                        }
                                    }

                                } else {
                                    setNode.add(listNode.get(relationships.getSrc()));
                                    relationshipsResponse.add(relationships);
                                }
                            } else {
                                count = countNode.get(relationships.getSrc());
                                if (count != null && count > 100) {
                                    String mmsiNode = "";
                                    if (listRelationShipsMedia.get(key).getSrc().equals(relationships.getSrc())) {
                                        mmsiNode = listRelationShipsMedia.get(key).getDest();
                                    } else {
                                        mmsiNode = listRelationShipsMedia.get(key).getSrc();
                                    }
                                    if (mmsiNode.equals(ids)) {
                                        setNode.add(listNode.get(relationships.getDest()));
                                        relationshipsResponse.add(relationships);
                                    } else {
                                        if (nodeIds.contains(relationships.getDest())) {
                                            setNode.add(listNode.get(relationships.getDest()));
                                            relationshipsResponse.add(relationships);
                                        }
                                    }

                                } else {
                                    setNode.add(listNode.get(relationships.getDest()));
                                    relationshipsResponse.add(relationships);
                                }
                            }

                            relationshipsResponse.add(listRelationShipsMedia.get(key));

                        }
                    });
                }
            }

        });
    }
    private void processResultNew(List<Record> records,Map<String, Object> listNode
            , Map<String, Relationships> listRelationShipsMedia, Set<Object> setNode , Set<Relationships> relationshipsResponse ,Integer deep, Map<String, Long> countNode,Map<String,Integer> nodeIndex, String ids,Integer page, Boolean checkNodeIds){
        List<Record> recordList = new ArrayList<>();
        Set<String> nodeIds = new LinkedHashSet<>();
        Set<String> nodeCheck = new LinkedHashSet<>();
        Integer size =100;
//        if(checkNodeIds){
//            size=20;
//        }
        Integer finalSize = size;
        records.forEach(record -> {
            String type = record.get("r1").asRelationship().type();
            String key1 = record.get("r1").get("src").asString() + record.get("r1").get("dest").asString()+type;
            if (listRelationShipsMedia.get(key1) != null && listRelationShipsMedia.get(key1).getCount() > 0) {
                    if(deep<2){
                        setNode.add(listNode.get(record.get("a").get("mmsi").asString()));
                        nodeCheck.add(record.get("a").get("mmsi").asString());

                        Long count = countNode.get(record.get("a").get("mmsi").asString());
                        if(count!=null&& count> finalSize){
                            if(record.get("a").get("mmsi").asString().equals(ids)) {
                                nodeIds.add(record.get("a1").get("mmsi").asString());
                                setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                                relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                nodeCheck.add(record.get("a1").get("mmsi").asString());
                            }else {
                                recordList.add(record);
                            }
//                            nodeIndex++;

                        }else {
                            nodeCheck.add(record.get("a1").get("mmsi").asString());
                            nodeIds.add(record.get("a1").get("mmsi").asString());
                            setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                            relationshipsResponse.add(listRelationShipsMedia.get(key1));
                        }
                    } else if (deep<3){
                        type = record.get("r2").asRelationship().type();
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString()+type;

                        if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                            nodeCheck.add(record.get("a1").get("mmsi").asString());
                            setNode.add(listNode.get(record.get("a").get("mmsi").asString()));
                            setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                            Long count = countNode.get(record.get("a1").get("mmsi").asString());
                            if(count!=null&& count>finalSize){
                                if(record.get("a1").get("mmsi").asString().equals(ids)) {
                                    nodeIds.add(record.get("a2").get("mmsi").asString());
                                    setNode.add(listNode.get(record.get("a2").get("mmsi").asString()));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                }else {
                                    recordList.add(record);
                                }

                            }else {
                                nodeIds.add(record.get("a2").get("mmsi").asString());
                                setNode.add(listNode.get(record.get("a2").get("mmsi").asString()));
                                relationshipsResponse.add(listRelationShipsMedia.get(key2));
                            }
                            relationshipsResponse.add(listRelationShipsMedia.get(key1));

                        }
                    }else if (deep<4){
                        type = record.get("r2").asRelationship().type();
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString()+type;
                        if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                            type = record.get("r3").asRelationship().type();
                            String key3 = record.get("r3").get("src").asString() + record.get("r3").get("dest").asString()+type;
                            if (listRelationShipsMedia.get(key3) != null && listRelationShipsMedia.get(key3).getCount() > 0) {
                                    nodeCheck.add(record.get("a2").get("mmsi").asString());
                                    setNode.add(listNode.get(record.get("a").get("mmsi").asString()));
                                    setNode.add(listNode.get(record.get("a1").get("mmsi").asString()));
                                    setNode.add(listNode.get(record.get("a2").get("mmsi").asString()));
                                Long count = countNode.get(record.get("a2").get("mmsi").asString());
                                if(count!=null&& count>finalSize){
                                    if(record.get("a2").get("mmsi").asString().equals(ids)) {
                                        nodeIds.add(record.get("a3").get("mmsi").asString());
                                        setNode.add(listNode.get(record.get("a3").get("mmsi").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key3));
                                    }else {
                                        recordList.add(record);
                                    }

                                }else {
                                    nodeIds.add(record.get("a3").get("mmsi").asString());
                                    setNode.add(listNode.get(record.get("a3").get("mmsi").asString()));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key3));
                                }
                                    relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key2));

                            }
                        }
                    }

                }

        });

        if(nodeIds!=null&&!nodeIds.isEmpty()&&recordList!=null&&!recordList.isEmpty()){
            proceeResultFilter(recordList,listNode,listRelationShipsMedia,setNode,relationshipsResponse
            ,deep,countNode,nodeIds,ids);
        }
//        if(ids==null ){
//            page =-1;
//        }
        Integer finalPage = page;

        countNode.forEach((k, v)->{
            int sizePage=100;
            if(nodeCheck.contains(k)) {
                Object object = new Object();
                object.setName("...");
                object.setId(nodeIndex.get("index"));

                int pages = 0;
                Long pageSize = v;
//                if(v>1000){
//                    sizePage=500;
//                }
                while (v > sizePage) {
                    if (pages != page) {
                        Relationships relationships = new Relationships();
                        relationships.setStart(listNode.get(k).getId());
                        relationships.setEnd(nodeIndex.get("index"));
                        relationships.setSrc(k);
                        relationships.setCount(pageSize);
                        relationships.setType("ALL");
                        relationships.setPage(pages);
                        relationships.setSize(sizePage);
                        relationshipsResponse.add(relationships);
                    } else {
                        if (ids == null) {
                            Relationships relationships = new Relationships();
                            relationships.setStart(listNode.get(k).getId());
                            relationships.setEnd(nodeIndex.get("index"));
                            relationships.setSrc(k);
                            relationships.setCount(pageSize);
                            relationships.setType("ALL");
                            relationships.setPage(pages);
                            relationships.setSize(sizePage);
                            relationshipsResponse.add(relationships);
                        } else {
                            if (!k.equals(ids)) {
                                Relationships relationships = new Relationships();
                                relationships.setStart(listNode.get(k).getId());
                                relationships.setEnd(nodeIndex.get("index"));
                                relationships.setSrc(k);
                                relationships.setCount(pageSize);
                                relationships.setType("ALL");
                                relationships.setPage(pages);
                                relationships.setSize(sizePage);
                                relationshipsResponse.add(relationships);
                            }
                        }
                    }
                    pages++;
                    v = v - sizePage;

                }
                if (pages > 0) {
                    if(v>0){
                        if (pages != page) {
                            Relationships relationships = new Relationships();
                            relationships.setStart(listNode.get(k).getId());
                            relationships.setEnd(nodeIndex.get("index"));
                            relationships.setSrc(k);
                            relationships.setCount(pageSize);
                            relationships.setType("ALL");
                            relationships.setPage(pages);
                            relationships.setSize(sizePage);
                            relationshipsResponse.add(relationships);
                        } else {
                            if (ids == null) {
                                Relationships relationships = new Relationships();
                                relationships.setStart(listNode.get(k).getId());
                                relationships.setEnd(nodeIndex.get("index"));
                                relationships.setSrc(k);
                                relationships.setCount(pageSize);
                                relationships.setType("ALL");
                                relationships.setPage(pages);
                                relationships.setSize(sizePage);
                                relationshipsResponse.add(relationships);
                            } else {
                                if (!k.equals(ids)) {
                                    Relationships relationships = new Relationships();
                                    relationships.setStart(listNode.get(k).getId());
                                    relationships.setEnd(nodeIndex.get("index"));
                                    relationships.setSrc(k);
                                    relationships.setCount(pageSize);
                                    relationships.setType("ALL");
                                    relationships.setPage(pages);
                                    relationships.setSize(sizePage);
                                    relationshipsResponse.add(relationships);
                                }
                            }
                        }
                    }
                    setNode.add(object);
                    nodeIndex.replace("index", nodeIndex.get("index") + 1);
                }
            }


        });
    }

    private void processDeep2(List<Record> records,Map<String, Object> listNode
            , Map<String, Relationships> listRelationShipsMedia, Set<Object> setNode , Set<Relationships> relationshipsResponse ,Integer deep, Map<String, Long> countNode,Map<String,Integer> nodeIndex, String ids,Integer page, Map<Long,String> relationIdToKey){
        Set<Record> recordList = new HashSet<>();
        Set<String> nodeIds = new LinkedHashSet<>();
        Set<String> nodeCheck = new LinkedHashSet<>();
        Integer size =100;
//        if(checkNodeIds){
//            size=20;
//        }
        Integer finalSize = size;
        records.forEach(record -> {
            Long idR1 = record.get("r1").asLong();
            String key = relationIdToKey.get(idR1);

            if (listRelationShipsMedia.get(key) != null && listRelationShipsMedia.get(key).getCount() > 0) {
                if (deep==2){
                    List<java.lang.Object> listRelationDeep2 = record.get("r2").asList();
                    listRelationDeep2.stream().forEach((id)->{
                        String keyR2 = relationIdToKey.get(id);
                        Relationships relationships= listRelationShipsMedia.get(keyR2);
                        if (relationships!=null&&relationships.getCount()>0) {
                            setNode.add(listNode.get( listRelationShipsMedia.get(key).getSrc()));
                            setNode.add(listNode.get(listRelationShipsMedia.get(key).getDest()));
                            List<String> a1 = new ArrayList<>();
                            a1.add(listRelationShipsMedia.get(key).getSrc());
                            a1.add(listRelationShipsMedia.get(key).getDest());
                            Long count=0L;
                            if(a1.contains(relationships.getDest())){
                                count=countNode.get(relationships.getDest());
                                nodeCheck.add(relationships.getDest());
                                if(count!=null&& count>finalSize){
                                    if(relationships.getDest().equals(ids)) {
                                        nodeIds.add(relationships.getSrc());
                                        setNode.add(listNode.get(relationships.getSrc()));
                                        relationshipsResponse.add(relationships);
                                    }else {
                                        recordList.add(record);
                                    }

                                }else {
                                    nodeIds.add(relationships.getSrc());
                                    setNode.add(listNode.get(relationships.getSrc()));
                                    relationshipsResponse.add(relationships);
                                }
                            }else {
                                count=countNode.get(relationships.getSrc());
                                nodeCheck.add(relationships.getSrc());
                                if(count!=null&& count>finalSize){
                                    if(relationships.getDest().equals(ids)) {
                                        nodeIds.add(relationships.getDest());
                                        setNode.add(listNode.get(relationships.getDest()));
                                        relationshipsResponse.add(relationships);
                                    }else {
                                        recordList.add(record);
                                    }

                                }else {
                                    nodeIds.add(relationships.getDest());
                                    setNode.add(listNode.get(relationships.getDest()));
                                    relationshipsResponse.add(relationships);
                                }
                            }

                            relationshipsResponse.add(listRelationShipsMedia.get(key));

                        }
                    });
                }

        }});

        if(nodeIds!=null&&!nodeIds.isEmpty()&&recordList!=null&&!recordList.isEmpty()){
            List<Record> records1 = new ArrayList<>(recordList);
            processResultFilter2(records1,listNode,listRelationShipsMedia,setNode,relationshipsResponse
                    ,deep,countNode,nodeIds,ids,relationIdToKey);
        }
        Integer finalPage = page;
        nodeIndex= new HashMap<>();
        nodeIndex.put("index",-1);
        Map<String, Integer> finalNodeIndex = nodeIndex;
        countNode.forEach((k, v)->{
            int sizePage=100;
            if(nodeCheck.contains(k)) {
                Object object = new Object();
                object.setName("...");
                object.setId(finalNodeIndex.get("index"));

                int pages = 0;
                Long pageSize = v;
//                if(v>1000){
//                    sizePage=500;
//                }
                while (v > sizePage) {
                    if (pages != page) {
                        Relationships relationships = new Relationships();
                        relationships.setStart(listNode.get(k).getId());
                        relationships.setEnd(finalNodeIndex.get("index"));
                        relationships.setSrc(k);
                        relationships.setCount(pageSize);
                        relationships.setType("ALL");
                        relationships.setPage(pages);
                        relationships.setSize(sizePage);
                        relationshipsResponse.add(relationships);
                    } else {
                        if (ids == null) {
                            Relationships relationships = new Relationships();
                            relationships.setStart(listNode.get(k).getId());
                            relationships.setEnd(finalNodeIndex.get("index"));
                            relationships.setSrc(k);
                            relationships.setCount(pageSize);
                            relationships.setType("ALL");
                            relationships.setPage(pages);
                            relationships.setSize(sizePage);
                            relationshipsResponse.add(relationships);
                        } else {
                            if (!k.equals(ids)) {
                                Relationships relationships = new Relationships();
                                relationships.setStart(listNode.get(k).getId());
                                relationships.setEnd(finalNodeIndex.get("index"));
                                relationships.setSrc(k);
                                relationships.setCount(pageSize);
                                relationships.setType("ALL");
                                relationships.setPage(pages);
                                relationships.setSize(sizePage);
                                relationshipsResponse.add(relationships);
                            }
                        }
                    }
                    pages++;
                    v = v - sizePage;

                }
                if (pages > 0) {
                    if(v>0){
                        if (pages != page) {
                            Relationships relationships = new Relationships();
                            relationships.setStart(listNode.get(k).getId());
                            relationships.setEnd(finalNodeIndex.get("index"));
                            relationships.setSrc(k);
                            relationships.setCount(pageSize);
                            relationships.setType("ALL");
                            relationships.setPage(pages);
                            relationships.setSize(sizePage);
                            relationshipsResponse.add(relationships);
                        } else {
                            if (ids == null) {
                                Relationships relationships = new Relationships();
                                relationships.setStart(listNode.get(k).getId());
                                relationships.setEnd(finalNodeIndex.get("index"));
                                relationships.setSrc(k);
                                relationships.setCount(pageSize);
                                relationships.setType("ALL");
                                relationships.setPage(pages);
                                relationships.setSize(sizePage);
                                relationshipsResponse.add(relationships);
                            } else {
                                if (!k.equals(ids)) {
                                    Relationships relationships = new Relationships();
                                    relationships.setStart(listNode.get(k).getId());
                                    relationships.setEnd(finalNodeIndex.get("index"));
                                    relationships.setSrc(k);
                                    relationships.setCount(pageSize);
                                    relationships.setType("ALL");
                                    relationships.setPage(pages);
                                    relationships.setSize(sizePage);
                                    relationshipsResponse.add(relationships);
                                }
                            }
                        }
                    }
                    setNode.add(object);
                    finalNodeIndex.replace("index", finalNodeIndex.get("index") - 1);
                }
            }


        });
    }

}
