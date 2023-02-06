package elcom.com.neo4j.service.impl;

import elcom.com.neo4j.node.Object;
import elcom.com.neo4j.node.Relationships;
import elcom.com.neo4j.node.ValueReport;
import org.apache.flink.types.Row;
//import org.apache.spark.sql.Row;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.internal.value.NullValue;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class SaveMutil {
    private final ObjectRepository movieRepository;

    private final Neo4jClient neo4jClient;

    private final Driver driver;

    private final DatabaseSelectionProvider databaseSelectionProvider;

    SaveMutil(ObjectRepository movieRepository,
              Neo4jClient neo4jClient,
              Driver driver,
              DatabaseSelectionProvider databaseSelectionProvider) {

        this.movieRepository = movieRepository;
        this.neo4jClient = neo4jClient;
        this.driver = driver;
        this.databaseSelectionProvider = databaseSelectionProvider;
    }

    @Async("ReProcessing")
    public CompletableFuture<String> saveData(List<ValueReport> valueReports, String timeStart, String timeEnd , Row row, String key){
//        ValueReport valueReport = valueReports.get(0);
//        String query = " MATCH (m:Object) - [r:MEDIA] -> (p:Object) where r.startTime >= '"+timeStart+"' and r.startTime < '"+timeEnd+"' and r.src='"+row.getAs("idsSrc")+"' and r.dest='"+row.getAs("idsDest")+"'";
//        query += " set r +={";
//        query += "count:" + row.getAs("count") +valueReport.getCount();
//        Long value = (Long) row.getAs("WebCount") + valueReport.getWebCount();
//        if (value > 0) {
//            query += ",WebCount" + ":" + value;
//            value = ((Long) row.getAs("WebFileSize") +valueReport.getWebFileSize());
//            query += ",WebFileSize" + ":" +value ;
//        }
//        value = (Long) row.getAs("VoiceCount") + valueReport.getVoiceCount();
//        if (value > 0) {
//            query += ",VoiceCount" + ":" + value;
//            value = ((Long) row.getAs("VoiceFileSize")+ valueReport.getVoiceFileSize());
//            query += ",VoiceFileSize" + ":" + value;
//        }
//        value =  (Long) row.getAs("TransferFileCount") + valueReport.getTransferFileCount();
//        if (value > 0) {
//            query += ",TransferFileCount" + ":" + value;
//            value = ((Long)row.getAs("TransferFileFileSize") + valueReport.getTransferFileFileSize());
//            query += ",TransferFileFileSize" + ":" + value;
//        }
//        value = (Long) row.getAs("VideoCount") + valueReport.getVideoCount();
//        if (value > 0) {
//            query += ",VideoCount" + ":" + value;
//            value = ((Long) row.getAs("VideoFileSize") +valueReport.getVideoFileSize());
//            query += ",VideoFileSize" + ":" + value ;
//        }
//        value = (Long) row.getAs("EmailCount") + valueReport.getEmailCount();
//        if (value > 0) {
//            query += ",EmailCount" + ":" + value;
//            value = ((Long)row.getAs("EmailFileSize") + valueReport.getEmailFileSize());
//            query += ",EmailFileSize" + ":" + value ;
//        }
//        query += "} ";
//        query = " USE fabric."+key + query;
//        this.neo4jClient
//                .query(query)
//                .in(database())
//                .run()
//                .counters()
//                .propertiesSet();
        return CompletableFuture.completedFuture("oke");
    }

    @Async("updateMulti")
    public CompletableFuture<ValueReport> checkData(Integer type, String fromDate, String toDate, String idsSrc, String idsDest, String mediaType,String[] data, Integer index) {
                try {
            var nodes = new ArrayList<>();
            var links = new ArrayList<>();
            List<ValueReport> result = new ArrayList<>();
            String key="";
            if(type==1){
                key+="metacenday";
            } else if(type==2){
                key+="metacenmonth";
            } else if(type==3){
                key+="vsatyear";
            }  else if(type==4){
                key+="vsatweek";
            }

            try (Session session = sessionFor(key)) {
                String query = " MATCH (m:Object) - [r:"+mediaType+"] -> (p:Object) where r.startTime >= '"+fromDate+"' and r.startTime < '"+toDate+"' and m.id='"+idsSrc+"' and p.id='"+idsDest+"'  RETURN  r  limit 1";
                query = query;
                String finalQuery = query;
                var records = session.readTransaction(tx -> tx.run(finalQuery).list());
                records.forEach(record -> {
                    Long id = record.get("r").asRelationship().id();
//				var movie = Map.of("label", "start", "title", record.get("end").asString());

                    var targetIndex = nodes.size();
//				nodes.add(movie);
                    Long count;
                    Long fileSize;
                    try {
                        count = Long.valueOf(record.get("r").get("count").asString());
                        fileSize = Long.valueOf(record.get("r").get("fileSize").asString());
                    }catch (Exception ex) {
                        count = record.get("r").get("count").asLong();
                        fileSize =record.get("r").get("fileSize").asLong();
                    }
                    String time = "";
                    String dateTime ="";
                    DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    ValueReport valueReport = new ValueReport();
                    valueReport.setCount(count);
                    valueReport.setIdsSrc(idsSrc);
                    valueReport.setIdsDest(idsDest);
                    valueReport.setDateTime(fromDate);
                    valueReport.setFileSize(fileSize);
                    valueReport.setId(id);
                    valueReport.setMediaType(mediaType);
//                    valueReport.setData(data);
//                    valueReport.setIndex(index);
                    result.add(valueReport);

                });
//                session.run("MATCH ()-[r:Video]->() WHERE id(r) = 186212 delete r");
            }
            if(result!=null && !result.isEmpty()) {
                return CompletableFuture.completedFuture(result.get(0));
            }else {
                return CompletableFuture.completedFuture(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    public List<ValueReport> checkData(List<String> keys, String database) {
        try {
            List<ValueReport> result= new ArrayList<>();

            try (Session session = sessionFor(database)) {
                String key = keys.toString();
                String query = " MATCH (m:Object) - [r] -> (p:Object) where r.key in "+key+"  RETURN  r ";
                query = query;
                String finalQuery = query;
                var records = session.readTransaction(tx -> tx.run(finalQuery).list());
                records.forEach(record -> {
                    Long id = record.get("r").asRelationship().id();
                    Long count;
                    Long fileSize;
                    try {
                        count = Long.valueOf(record.get("r").get("count").asString());
                        fileSize = Long.valueOf(record.get("r").get("fileSize").asString());
                    }catch (Exception ex) {
                        count = record.get("r").get("count").asLong();
                        fileSize =record.get("r").get("fileSize").asLong();
                    }
                    String keyRelations = record.get("r").get("key").asString();
                    String time = "";
                    String dateTime ="";
                    DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    ValueReport valueReport = new ValueReport();
                    valueReport.setCount(count);
                    valueReport.setFileSize(fileSize);
                    valueReport.setId(id);
                    valueReport.setKey(keyRelations);
                    result.add(valueReport);

                });
//                session.run("MATCH ()-[r:Video]->() WHERE id(r) = 186212 delete r");
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
           return null;
        }
    }


    @Async("updateMulti")
    public CompletableFuture<ValueReport> checkDataAndDelete(Integer type, String fromDate, String toDate, String idsSrc, String idsDest, String mediaType) {
        try {
            var nodes = new ArrayList<>();
            var links = new ArrayList<>();
            List<ValueReport> result = new ArrayList<>();
            String key="";
            if(type==1){
                key+="metacenday";
            } else if(type==2){
                key+="metacenmonth";
            } else if(type==3){
                key+="vsatyear";
            }  else if(type==4){
                key+="vsatweek";
            }

            try (Session session = sessionFor(key)) {
                String query = " MATCH (m:Object) - [r:"+mediaType+"] -> (p:Object) where r.startTime >= '"+fromDate+"' and r.startTime < '"+toDate+"' and m.id='"+idsSrc+"' and p.id='"+idsDest+"'  RETURN  r  limit 1";
                query = query;
                String finalQuery = query;
                var records = session.readTransaction(tx -> tx.run(finalQuery).list());
                if(records!=null&&!records.isEmpty()){
                    Record record= records.get(0);
                    Long id = record.get("r").asRelationship().id();
//				var movie = Map.of("label", "start", "title", record.get("end").asString());

                    var targetIndex = nodes.size();
//				nodes.add(movie);
                    Long count;
                    Long fileSize;
                    try {
                        count = Long.valueOf(record.get("r").get("count").asString());
                        fileSize = Long.valueOf(record.get("r").get("fileSize").asString());
                    }catch (Exception ex) {
                        count = record.get("r").get("count").asLong();
                        fileSize =record.get("r").get("fileSize").asLong();
                    }
                    String time = "";
                    String dateTime ="";
                    DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    ValueReport valueReport = new ValueReport();
                    valueReport.setCount(count);
                    valueReport.setIdsSrc(idsSrc);
                    valueReport.setIdsDest(idsDest);
                    valueReport.setDateTime(fromDate);
                    valueReport.setFileSize(fileSize);
                    valueReport.setId(id);
                    valueReport.setMediaType(mediaType);
                    result.add(valueReport);
                    session.writeTransaction(tx -> tx.run("MATCH ()-[r:Video]->() WHERE id(r) ="+valueReport.getId() + " delete r"));

                }



//                session.run("MATCH ()-[r:Video]->() WHERE id(r) = 186212 delete r");
            }
            if(result!=null && !result.isEmpty()) {

                return CompletableFuture.completedFuture(result.get(0));
            }else {
                return CompletableFuture.completedFuture(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async("updateMulti")
    public CompletableFuture<ValueReport> updateRelation(ValueReport valueReport, String[] data, String database) {
        try {

            try (Session session = sessionFor(database)) {
                String query = " MATCH (m:Object) - [r:"+valueReport.getMediaType()+"] -> (p:Object) where id(r)="+valueReport.getId();
                query += " set r +={";
                Long count = Integer.valueOf(data[4]) +valueReport.getCount();
                query += "count:" + count;
                Long value = Integer.valueOf(data[5]) +valueReport.getFileSize();
                query += ",fileSize" + ":" +value ;
                query += "} ";
                String finalQuery = query;
                session.writeTransaction(tx -> tx.run(finalQuery));
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async("updateMulti")
    public CompletableFuture<ValueReport> delete(List<Long> ids, String database) {
        try {

            try (Session session = sessionFor(database)) {
                String id = ids.toString();
                String query = ":auto Match (a:Object) -[r]->(b:Object) where id(r) in"+id+ " CALL { WITH r \n" +
                        "DELETE r \n" +
                        "} IN TRANSACTIONS OF 1000 ROWS ";
//                String query = " MATCH (m:Object) - [r:"+valueReport.getMediaType()+"] -> (p:Object) where id(r)="+valueReport.getId();
//                query += " set r +={";
//                Long count = Integer.valueOf(data[4]) +valueReport.getCount();
//                query += "count:" + count;
//                Long value = Integer.valueOf(data[5]) +valueReport.getFileSize();
//                query += ",fileSize" + ":" +value ;
//                query += "} ";
                session.writeTransaction(tx -> tx.run(query));
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async("ReProcessing")
    public CompletableFuture<String> saveDataAis(List<ValueReport> valueReports, String timeStart, String timeEnd , Row row, String key){
//        ValueReport valueReport = valueReports.get(0);
//        String query = " MATCH (m:Object) - [r:AIS] -> (p:Object) where r.startTime >= '"+timeStart+"' and r.startTime < '"+timeEnd+"' and r.src='"+row.getAs("idsSrc")+"' and r.dest='"+row.getAs("idsDest")+"'";
//        query += " set r +={";
//        Long count = (Long) row.getAs("count") +valueReport.getCount();
//        query += "count:" + count;
//        query += "} ";
//        query = " USE fabric."+key + query;
//        this.neo4jClient
//                .query(query)
//                .in(database())
//                .run()
//                .counters()
//                .propertiesSet();
        return CompletableFuture.completedFuture("oke");
    }

    @Async("ReProcessing")
    public CompletableFuture<String> process(List<Record> records, Map<String, Object> listNode, Map<String, Relationships> listRelationShipsAis
            , Map<String, Relationships> listRelationShipsMedia, Set<Object> setNode , Set<Relationships> relationshipsResponse , Integer deep, Map<String, Long> countNode, String ids,  Set<String> nodeIds, Set<String> nodeCheck,List<Record> recordList ){
        Integer finalSize =100;
        records.forEach(record -> {
            String key1 = record.get("r1").get("src").asString() + record.get("r1").get("dest").asString();
            String type = record.get("r1").asRelationship().type();
            if (type.equals("MEDIA")) {
                if (listRelationShipsMedia.get(key1) != null && listRelationShipsMedia.get(key1).getCount() > 0) {
                    if(deep<2){
                        if(record.get("a").get("ids").asString().equals("121.51.130.113-92110?")){
                            System.out.println("okghjghje");
                        }
                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                        nodeCheck.add(record.get("a").get("ids").asString());
                        Long count = countNode.get(record.get("a").get("ids").asString());
                        if(count!=null&& count> finalSize){
                            if(record.get("a").get("ids").asString().equals(ids)) {

                                nodeIds.add(record.get("a1").get("ids").asString());
                                setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                relationshipsResponse.add(listRelationShipsMedia.get(key1));
                            }else {
                                recordList.add(record);
                            }
//                            nodeIndex++;

                        }else {
                            nodeIds.add(record.get("a1").get("ids").asString());
                            setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                            relationshipsResponse.add(listRelationShipsMedia.get(key1));
                        }
                    } else if (deep<3){
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString();
                        type = record.get("r2").asRelationship().type();
                        if (type.equals("MEDIA")) {
                            if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                nodeCheck.add(record.get("a1").get("ids").asString());
                                setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                Long count = countNode.get(record.get("a1").get("ids").asString());
                                if(count!=null&& count>finalSize){
                                    if(record.get("a").get("ids").asString().equals(ids)) {
                                        nodeIds.add(record.get("a2").get("ids").asString());
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                    }else {
                                        recordList.add(record);
                                    }

                                }else {
                                    nodeIds.add(record.get("a2").get("ids").asString());
                                    setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                }
                                relationshipsResponse.add(listRelationShipsMedia.get(key1));

                            }
                        }
                        if (type.equals("AIS")) {
                            if (listRelationShipsAis.get(key2) != null && listRelationShipsAis.get(key2).getCount() > 0) {
                                nodeCheck.add(record.get("a1").get("ids").asString());
                                setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                Long count = countNode.get(record.get("a1").get("ids").asString());
                                if(count!=null&& count>finalSize){
                                    if(record.get("a").get("ids").asString().equals(ids)) {
                                        nodeIds.add(record.get("a2").get("ids").asString());
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsAis.get(key2));
                                    }else {
                                        recordList.add(record);
                                    }

                                }else {
                                    nodeIds.add(record.get("a2").get("ids").asString());
                                    setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                    relationshipsResponse.add(listRelationShipsAis.get(key2));
                                }
                                relationshipsResponse.add(listRelationShipsMedia.get(key1));


                            }
                        }
                    }else if (deep<4){
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString();
                        type = record.get("r2").asRelationship().type();
                        if (type.equals("MEDIA")) {
                            if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                String key3 = record.get("r3").get("src").asString() + record.get("r3").get("dest").asString();
                                type = record.get("r3").asRelationship().type();
                                if (type.equals("MEDIA")) {
                                    if (listRelationShipsMedia.get(key3) != null && listRelationShipsMedia.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key3));

                                    }
                                }
                                if (type.equals("AIS")) {
                                    if (listRelationShipsAis.get(key3) != null && listRelationShipsAis.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                        relationshipsResponse.add(listRelationShipsAis.get(key3));

                                    }
                                }

                            }
                        }else if (type.equals("AIS")) {
                            if (listRelationShipsAis.get(key2) != null && listRelationShipsAis.get(key2).getCount() > 0) {
                                String key3 = record.get("r3").get("src").asString() + record.get("r3").get("dest").asString();
                                type = record.get("r3").asRelationship().type();
                                if (type.equals("MEDIA")) {
                                    if (listRelationShipsMedia.get(key3) != null && listRelationShipsMedia.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                        relationshipsResponse.add(listRelationShipsAis.get(key2));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key3));

                                    }
                                }
                                if (type.equals("AIS")) {
                                    if (listRelationShipsAis.get(key3) != null && listRelationShipsAis.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key1));
                                        relationshipsResponse.add(listRelationShipsAis.get(key2));
                                        relationshipsResponse.add(listRelationShipsAis.get(key3));

                                    }
                                }

                            }
                        }
                    }

                }
            } else if (type.equals("AIS")) {
                if (listRelationShipsAis.get(key1) != null && listRelationShipsAis.get(key1).getCount() > 0) {
                    if(deep<2){
                        if(record.get("a").get("ids").asString().equals("121.51.130.113-92110?")){
                            System.out.println("okghjghje");
                        }
                        nodeCheck.add(record.get("a").get("ids").asString());
                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                        Long count = countNode.get(record.get("a").get("ids").asString());
                        if(count!=null&& count>finalSize){
                            if(record.get("a").get("ids").asString().equals(ids)) {
                                nodeIds.add(record.get("a1").get("ids").asString());
                                setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                relationshipsResponse.add(listRelationShipsAis.get(key1));
                            }else {
                                recordList.add(record);
                            }
//                            nodeIndex++;

                        }else {
                            nodeIds.add(record.get("a1").get("ids").asString());
                            setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                            relationshipsResponse.add(listRelationShipsAis.get(key1));
                        }

                    } else if (deep<3){
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString();
                        type = record.get("r2").asRelationship().type();
                        if (type.equals("MEDIA")) {
                            if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                nodeCheck.add(record.get("a1").get("ids").asString());
                                setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                setNode.add(listNode.get(record.get("a1").get("ids").asString()));

                                Long count = countNode.get(record.get("a1").get("ids").asString());
                                if(count!=null&& count>finalSize){
                                    if(record.get("a").get("ids").asString().equals(ids)) {
                                        nodeIds.add(record.get("a2").get("ids").asString());
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                    }else {
                                        recordList.add(record);
                                    }

                                }else {
                                    nodeIds.add(record.get("a2").get("ids").asString());
                                    setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                    relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                }
                                relationshipsResponse.add(listRelationShipsAis.get(key1));

                            }
                        }
                        if (type.equals("AIS")) {
                            if (listRelationShipsAis.get(key2) != null && listRelationShipsAis.get(key2).getCount() > 0) {
                                nodeCheck.add(record.get("a1").get("ids").asString());
                                setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                setNode.add(listNode.get(record.get("a1").get("ids").asString()));

                                Long count = countNode.get(record.get("a1").get("ids").asString());
                                if(count!=null&& count>finalSize){
                                    if(record.get("a").get("ids").asString().equals(ids)) {
                                        nodeIds.add(record.get("a2").get("ids").asString());
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsAis.get(key2));
                                    }else {
                                        recordList.add(record);
                                    }

                                }else {
                                    nodeIds.add(record.get("a2").get("ids").asString());
                                    setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                    relationshipsResponse.add(listRelationShipsAis.get(key2));
                                }
                                relationshipsResponse.add(listRelationShipsAis.get(key1));


                            }
                        }
                    }else if (deep<4){
                        String key2 = record.get("r2").get("src").asString() + record.get("r2").get("dest").asString();
                        type = record.get("r2").asRelationship().type();
                        if (type.equals("MEDIA")) {
                            if (listRelationShipsMedia.get(key2) != null && listRelationShipsMedia.get(key2).getCount() > 0) {
                                String key3 = record.get("r3").get("src").asString() + record.get("r3").get("dest").asString();
                                type = record.get("r3").asRelationship().type();
                                if (type.equals("MEDIA")) {
                                    if (listRelationShipsMedia.get(key3) != null && listRelationShipsMedia.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsAis.get(key1));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key3));

                                    }
                                }
                                if (type.equals("AIS")) {
                                    if (listRelationShipsAis.get(key3) != null && listRelationShipsAis.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsAis.get(key1));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key2));
                                        relationshipsResponse.add(listRelationShipsAis.get(key3));

                                    }
                                }

                            }
                        }else if (type.equals("AIS")) {
                            if (listRelationShipsAis.get(key2) != null && listRelationShipsAis.get(key2).getCount() > 0) {
                                String key3 = record.get("r3").get("src").asString() + record.get("r3").get("dest").asString();
                                type = record.get("r3").asRelationship().type();
                                if (type.equals("MEDIA")) {
                                    if (listRelationShipsMedia.get(key3) != null && listRelationShipsMedia.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsAis.get(key1));
                                        relationshipsResponse.add(listRelationShipsAis.get(key2));
                                        relationshipsResponse.add(listRelationShipsMedia.get(key3));

                                    }
                                }
                                if (type.equals("AIS")) {
                                    if (listRelationShipsAis.get(key3) != null && listRelationShipsAis.get(key3).getCount() > 0) {
                                        setNode.add(listNode.get(record.get("a").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a1").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a2").get("ids").asString()));
                                        setNode.add(listNode.get(record.get("a3").get("ids").asString()));
                                        relationshipsResponse.add(listRelationShipsAis.get(key1));
                                        relationshipsResponse.add(listRelationShipsAis.get(key2));
                                        relationshipsResponse.add(listRelationShipsAis.get(key3));

                                    }
                                }

                            }
                        }
                    }

                }
            }

        });
        return CompletableFuture.completedFuture("oke");
    }


    @Async("saveNode")
    public CompletableFuture<String> saveNeo4j(String query){
        this.neo4jClient
                .query(query)
                .in(database())
                .run()
                .counters()
                .propertiesSet();
        return CompletableFuture.completedFuture("oke");
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
}
