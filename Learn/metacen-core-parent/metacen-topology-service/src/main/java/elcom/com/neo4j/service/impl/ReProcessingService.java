package elcom.com.neo4j.service.impl;

import elcom.com.neo4j.clickhouse.model.Ais;
import elcom.com.neo4j.clickhouse.service.MetaCenMediaService;
import elcom.com.neo4j.dto.*;
import elcom.com.neo4j.redis.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ReProcessingService {

//    @Autowired
//    private SparkSession sparkSession;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private MetaCenMediaService vsatMediaService;

    @Autowired
    private ObjectServiceImpl objectService;

    @Async("ReProcessing")
    public CompletableFuture<String> reProcessing(String ips, String startTime, String endTime) {
        try {
            DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String start = startTime;
            Calendar cal = Calendar.getInstance();
            cal.setTime(dff.parse(start));
            cal.add(Calendar.DAY_OF_MONTH,1);
            String end = dff.format(cal.getTime());
            while (dff.parse(end).getTime()<=dff.parse(endTime).getTime()){
//                schuduleS02022(start,end,ips);
                start=end;
                cal.setTime(dff.parse(start));
                cal.add(Calendar.DAY_OF_MONTH,1);
                end = dff.format(cal.getTime());

            }
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dff.parse(endTime));
            calEnd.clear(Calendar.MINUTE);
            calEnd.clear(Calendar.SECOND);
            calEnd.clear(Calendar.MILLISECOND);
            end = dff.format(calEnd.getTime());
//            schuduleS02022(start,end,ips);
            return CompletableFuture.completedFuture("oke");
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture("false");
        }
    }

//    public void schuduleS02022(String s02022,String endTime, String ips) throws Exception {
////        redisRepository.saveKeyTime("2022-03-09 09:00:00");
//        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        List<VsatMedia> vsatMedias = vsatMediaService.findReProcessing(s02022,endTime,ips);
//        List<ValueSpark> valueSparks = new ArrayList<>();
//        String now = new Date().toString();
//        Long count = 0L;
//        if (vsatMedias != null && !vsatMedias.isEmpty()) {
//            Set<String> set = new LinkedHashSet<>();
//            for (VsatMedia vsatMedia : vsatMedias
//            ) {
//                set.add(vsatMedia.getSourceIp() + "_" + vsatMedia.getDataSource());
//                set.add(vsatMedia.getSourceIp() + "_" + "-1");
//                set.add(vsatMedia.getDestIp() + "_" + "-1");
//                set.add(vsatMedia.getDestIp() + "_" + vsatMedia.getDataSource());
//            }
//            List<String> listKeySource = new ArrayList<>(set);
//
//            List<KeytoObject> keytoObjectList = redisRepository.findKeytoObject(listKeySource);
//            listKeySource = new ArrayList<>();
//
//            Map<String, String> keyToId = new HashMap<>();
//            Map<String, ObjectInfo> mapObjectInfo = new HashMap<>();
//            for (KeytoObject key : keytoObjectList
//            ) {
//                if (key != null) {
//                    keyToId.put(key.getKey(), key.getObjectInfo());
//                    listKeySource.add(key.getObjectInfo());
//                }
//            }
//            List<ObjectInfo> objectInfoList = redisRepository.findRedisObjectInfo(listKeySource);
//            for (ObjectInfo key : objectInfoList
//            ) {
//                if (key != null) {
//                    mapObjectInfo.put(key.getId(), key);
//                }
//
//            }
//            System.out.println(new Date().toString());
//            for (VsatMedia vsatMedia : vsatMedias
//            ) {
//                ObjectInfo objectInfo = null;
//                ObjectInfo objectInfoDest = null;
//                String keytoObject = keyToId.get(vsatMedia.getSourceIp() + "_" + vsatMedia.getDataSource());
//                String keytoObjectDest = keyToId.get(vsatMedia.getDestIp() + "_" + vsatMedia.getDataSource());
//                if (keytoObject != null) {
//                    objectInfo = mapObjectInfo.get(keytoObject);
//                } else {
//                    keytoObject = keyToId.get(vsatMedia.getSourceIp() + "_-1");
//                    if (keytoObject != null) {
//                        objectInfo = mapObjectInfo.get(keytoObject);
//                    } else {
//                        objectInfo = new ObjectInfo();
//                        objectInfo.setId(vsatMedia.getSourceIp() + "-" + vsatMedia.getDataSource() + "?");
//                        objectInfo.setName(vsatMedia.getSourceIp());
//                        objectInfo.setIps(vsatMedia.getSourceIp() + "-" + vsatMedia.getDataSource());
//                        KeytoObject keytoObject1 = new KeytoObject();
//                        keytoObject1.setKey(vsatMedia.getSourceIp() + "_" + vsatMedia.getDataSource());
//                        keytoObject1.setObjectInfo(vsatMedia.getSourceIp() + "-" + vsatMedia.getDataSource() + "?");
//                        keyToId.put(vsatMedia.getSourceIp() + "_" + vsatMedia.getDataSource(), vsatMedia.getSourceIp() + "-" + vsatMedia.getDataSource() + "?");
//                        mapObjectInfo.put(vsatMedia.getSourceIp() + "-" + vsatMedia.getDataSource() + "?", objectInfo);
////                        redisRepository.saveHashKeyObject(keytoObject1);
////                        redisRepository.saveObjectInfo(objectInfo);
//                    }
//                }
//                if (keytoObjectDest != null) {
//                    objectInfoDest = mapObjectInfo.get(keytoObjectDest);
//                } else {
//                    keytoObjectDest = keyToId.get(vsatMedia.getDestIp() + "_-1");
//                    if (keytoObjectDest != null) {
//                        objectInfoDest = mapObjectInfo.get(keytoObjectDest);
//                    } else {
//                        objectInfoDest = new ObjectInfo();
//                        objectInfoDest.setId(vsatMedia.getDestIp() + "-" + vsatMedia.getDataSource() + "?");
//                        objectInfoDest.setName(vsatMedia.getDestIp());
//                        objectInfoDest.setIps(vsatMedia.getDestIp() + "-" + vsatMedia.getDataSource());
//                        KeytoObject keytoObject1 = new KeytoObject();
//                        keytoObject1.setKey(vsatMedia.getDestIp() + "_" + vsatMedia.getDataSource());
//                        keytoObject1.setObjectInfo(vsatMedia.getDestIp() + "-" + vsatMedia.getDataSource() + "?");
//                        keyToId.put(vsatMedia.getDestIp() + "_" + vsatMedia.getDataSource(), vsatMedia.getDestIp() + "-?");
//                        mapObjectInfo.put(vsatMedia.getDestIp() + "-?", objectInfoDest);
////                        redisRepository.saveHashKeyObject(keytoObject1);
////                        redisRepository.saveObjectInfo(objectInfoDest);
//                    }
//                }
//                if (objectInfo != null && objectInfoDest != null) {
//                    ValueSpark valueSpark = new ValueSpark();
//                    valueSpark.setIps(objectInfo.getIps());
//                    valueSpark.setId(objectInfo.getId());
//                    valueSpark.setLatitude(objectInfo.getLatitude());
//                    valueSpark.setLongitude(objectInfo.getLongitude());
//                    valueSpark.setName(objectInfo.getName());
//                    valueSpark.setTypeName(vsatMedia.getMediaTypeName());
//                    valueSpark.setFileSize(vsatMedia.getFileSize());
//                    valueSpark.setTypeSize(1L);
//
//                    valueSpark.setIp(vsatMedia.getSourceIp());
//
//                    valueSpark.setDestId(objectInfoDest.getId());
//                    valueSpark.setDestName(objectInfoDest.getName());
//                    valueSpark.setDestIps(objectInfoDest.getIps());
//                    valueSpark.setDestLatitude(objectInfoDest.getLatitude());
//                    valueSpark.setDestLongitude(objectInfoDest.getLongitude());
//                    valueSpark.setEventTime(dff.format(vsatMedia.getEventTime()));
//                    valueSparks.add(valueSpark);
//                }
//
//            }
//            count = Long.valueOf(vsatMedias.size());
//
//            Dataset<Row> dataFrame = sparkSession.createDataFrame(valueSparks, ValueSpark.class);
//            Dataset<Row> value = dataFrame.groupBy("eventTime","id", "destId", "typeName", "ips", "latitude", "name",
//                            "longitude", "destIps", "destLatitude", "destLongitude", "destName")
//                    .sum("fileSize", "typeSize").withColumnRenamed("sum(fileSize)", "fileSize")
//                    .withColumnRenamed("sum(typeSize)", "typeSize");
//            List<Row> listKey = value.collectAsList();
//            Map<String, List<Row>> mapObjectTypeMedia = new HashMap<>();
//            Map<String, Map<String, List<Row>>> keyDbObjectType = new HashMap<>();
//            DateFormat df = new SimpleDateFormat("yyyy");
//            String key = df.format(dff.parse(endTime));
//            for (Row row : listKey
//            ) {
//                String id = row.getAs("id") + "" + row.getAs("destId") +row.getAs("eventTime");
//                if (mapObjectTypeMedia.get(id) != null) {
//                    List<Row> rowList = mapObjectTypeMedia.get(id);
//                    rowList.add(row);
//                    if (id.indexOf("?") >= 0) {
//                        key = "s0" + key;
//                    } else {
//                        key = "s0" + key;
//                    }
//                    if (keyDbObjectType.get(key) != null) {
//                        Map<String, List<Row>> nodeNeo4j = keyDbObjectType.get(key);
//                        if (nodeNeo4j.get(id) != null) {
//                            nodeNeo4j.replace(id, rowList);
//                        } else {
//                            nodeNeo4j.put(id, rowList);
//                        }
//                        keyDbObjectType.replace(key, nodeNeo4j);
//                    } else {
//                        Map<String, List<Row>> nodeNeo4j = new HashMap<>();
//                        nodeNeo4j.put(id, rowList);
//                        keyDbObjectType.put(key, nodeNeo4j);
//                    }
//                    mapObjectTypeMedia.replace(id, rowList);
//                } else {
//                    List<Row> rowList = new ArrayList<>();
//                    rowList.add(row);
//                    if (id.indexOf("?") >= 0) {
//                        key = "s0" + key;
//                    } else {
//                        key = "s0" + key;
//                    }
//                    if (keyDbObjectType.get(key) != null) {
//                        Map<String, List<Row>> nodeNeo4j = keyDbObjectType.get(key);
//                        if (nodeNeo4j.get(id) != null) {
//                            nodeNeo4j.replace(id, rowList);
//                        } else {
//                            nodeNeo4j.put(id, rowList);
//                        }
//                        keyDbObjectType.replace(key, nodeNeo4j);
//                    } else {
//                        Map<String, List<Row>> nodeNeo4j = new HashMap<>();
//                        nodeNeo4j.put(id, rowList);
//                        keyDbObjectType.put(key, nodeNeo4j);
//                    }
//                    mapObjectTypeMedia.put(id, rowList);
//                }
//                key = df.format(dff.parse(endTime));
//            }
//
//            String finalS0202 = s02022;
//            keyDbObjectType.forEach((k, v) -> {
//                List<DataNeo4j> dataNeo4js = new ArrayList<>();
//                v.forEach((k1, v1) -> {
//                    DataNeo4j dataNeo4j = new DataNeo4j();
//                    dataNeo4j.setKey(k1);
//                    dataNeo4j.setMedias(v1);
//                    dataNeo4js.add(dataNeo4j);
//                    if (dataNeo4js.size() > 25) {
//                        objectService.saveObjectUpdateReprocessing(dataNeo4js, k, finalS0202, endTime);
//                        dataNeo4js.clear();
//                    }
//                });
//                if (dataNeo4js.size() > 0)
//                    objectService.saveObjectUpdateReprocessing(dataNeo4js, k, finalS0202, endTime);
//                dataNeo4js.clear();
//            });
//
//            System.out.println("okes1");
//        } else {
//            System.out.println("nos1");
//        }
//        addAis(s02022, endTime,ips);
////        processReport(s02022,endTime);
////        String out = "xu ly :" + count + now + "-" + new Date().toString() + " den" + endTime;
////        LOGGER.info(out);
//        s02022 = endTime;
//    }

    private void addAis(String startTime, String endTime,String ips) throws Exception {
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(dff.parse(endTime));;
        List<Ais> vsatMedias = vsatMediaService.findAisReProcess(startTime,endTime,ips);
        List<AisValueSpark> valueSparks = new ArrayList<>();
        Long count = 0L;
        if(vsatMedias!=null&&!vsatMedias.isEmpty()) {
            DateFormat df = new SimpleDateFormat("yyyy");
            Set<String> set = new LinkedHashSet<>();
            for (Ais vsatMedia: vsatMedias
            ) {
                set.add(vsatMedia.getSourceIp()+"_"+vsatMedia.getDataSource());
                set.add(vsatMedia.getSourceIp()+"_"+"-1");
                set.add(vsatMedia.getDestIp()+"_"+"-1");
                set.add(vsatMedia.getDestIp()+"_"+vsatMedia.getDataSource());
            }
            List<String> listKeySource = new ArrayList<>(set);

            List<KeytoObject> keytoObjectList = redisRepository.findKeytoObject(listKeySource);
            listKeySource = new ArrayList<>();

            Map<String,String> keyToId = new HashMap<>();
            Map<String,ObjectInfo> mapObjectInfo = new HashMap<>();
            for (KeytoObject key: keytoObjectList
            ) {
                if(key!=null) {
                    keyToId.put(key.getKey(), key.getObjectInfo());
                    listKeySource.add(key.getObjectInfo());
                }
            }
            List<ObjectInfo> objectInfoList = redisRepository.findRedisObjectInfo(listKeySource);
            for (ObjectInfo key: objectInfoList
            ) {
                if(key!=null) {
                    mapObjectInfo.put(key.getId(),key);
                }

            }
            Map<String,List<AisValueSpark>> mapValueSpark= new HashMap<>();
            for (Ais vsatMedia: vsatMedias
            ) {
                ObjectInfo objectInfo =null;
                ObjectInfo objectInfoDest =null;
                String keytoObject = keyToId.get(vsatMedia.getSourceIp()+"_"+vsatMedia.getDataSource());

                String keytoObjectDest = keyToId.get(vsatMedia.getDestIp()+"_"+vsatMedia.getDataSource());
                if(keytoObject!=null){
                    objectInfo=mapObjectInfo.get(keytoObject);
                } else {
                    keytoObject = keyToId.get(vsatMedia.getSourceIp()+"_"+-1);
                    if(keytoObject!=null){
                        objectInfo=mapObjectInfo.get(keytoObject);
                    } else {
                        objectInfo = new ObjectInfo();
                        objectInfo.setId(vsatMedia.getSourceIp()+"-"+vsatMedia.getDataSource()+"?");
                        objectInfo.setName(vsatMedia.getSourceIp());
                        objectInfo.setIps(vsatMedia.getSourceIp()+"-"+vsatMedia.getDataSource());
                        KeytoObject keytoObject1 = new KeytoObject();
                        keytoObject1.setKey(vsatMedia.getSourceIp()+"_"+vsatMedia.getDataSource());
                        keytoObject1.setObjectInfo(vsatMedia.getSourceIp()+"-"+vsatMedia.getDataSource()+"?");
                        keyToId.put(vsatMedia.getSourceIp()+"_"+vsatMedia.getDataSource(),vsatMedia.getSourceIp()+"-?");
                        mapObjectInfo.put(vsatMedia.getSourceIp()+"-?",objectInfo);
//                        redisRepository.saveHashKeyObject(keytoObject1);
//                        redisRepository.saveObjectInfo(objectInfo);
                    }
                }
                if(keytoObjectDest!=null){
                    objectInfoDest=mapObjectInfo.get(keytoObjectDest);
                } else {
                    keytoObjectDest = keyToId.get(vsatMedia.getDestIp()+"_"+-1);
                    if(keytoObjectDest!=null){
                        objectInfoDest=mapObjectInfo.get(keytoObjectDest);
                    } else {
                        objectInfoDest = new ObjectInfo();
                        objectInfoDest.setId(vsatMedia.getDestIp()+"-"+vsatMedia.getDataSource()+"?");
                        objectInfoDest.setName(vsatMedia.getDestIp());
                        objectInfoDest.setIps(vsatMedia.getDestIp()+"-"+vsatMedia.getDataSource());
                        KeytoObject keytoObject1 = new KeytoObject();
                        keytoObject1.setKey(vsatMedia.getDestIp()+"_"+vsatMedia.getDataSource());
                        keytoObject1.setObjectInfo(vsatMedia.getDestIp()+"-"+vsatMedia.getDataSource()+"?");
                        keyToId.put(vsatMedia.getDestIp()+"_"+vsatMedia.getDataSource(),vsatMedia.getDestIp()+"-?");
                        mapObjectInfo.put(vsatMedia.getDestIp()+"-?",objectInfoDest);
//                        redisRepository.saveHashKeyObject(keytoObject1);
//                        redisRepository.saveObjectInfo(objectInfoDest);
                    }
                }
                if(objectInfo!=null &&objectInfoDest!=null){
                    String key = df.format( cal.getTime());
                    AisValueSpark valueSpark = new AisValueSpark();
                    valueSpark.setSrc(objectInfo);
                    valueSpark.setDest(objectInfoDest);
                    valueSpark.setCount(vsatMedia.getCount());
                    valueSpark.setEventTime(dff.format(vsatMedia.getEventTime()));
                    if(objectInfo.getId().indexOf("?")>=0){
                        key = "s0"+key;
                    } else {
                        key = "s0"+key;
                    }
                    if(mapValueSpark.get(key)!=null){
                        List<AisValueSpark> nodeNeo4j =  mapValueSpark.get(key);
                        nodeNeo4j.add(valueSpark);
                        mapValueSpark.replace(key,nodeNeo4j);
                    } else {
                        List<AisValueSpark> nodeNeo4j = new ArrayList<>();
                        nodeNeo4j.add(valueSpark);
                        mapValueSpark.put(key,nodeNeo4j);
                    }
                }

            }
            count = Long.valueOf(vsatMedias.size());
            mapValueSpark.forEach((k,v)->{
                valueSparks.addAll(v);
                List<AisValueSpark> list = v;
                while (list.size()>25){
                    List<AisValueSpark> listSave = list.subList(0,25);
                    objectService.saveAisUpdateReprocessing(listSave,k,startTime,endTime);
                    list = list.subList(25,list.size());
                }
                objectService.saveAisUpdateReprocessing(list,k,startTime,endTime);
            });

        } else {
        }
    }
}
