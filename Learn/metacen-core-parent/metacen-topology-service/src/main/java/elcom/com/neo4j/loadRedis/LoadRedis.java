package elcom.com.neo4j.loadRedis;

import elcom.com.neo4j.dto.KeytoObject;
import elcom.com.neo4j.dto.ObjectInfo;
import elcom.com.neo4j.model.*;
import elcom.com.neo4j.redis.RedisRepository;
import elcom.com.neo4j.repository.ConfigObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoadRedis {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRedis.class);

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private ConfigObjectRepository objectInfoRepository;


//    @Bean
    public void loading(){
        List<AisInfo> aisInfoList = objectInfoRepository.getAisInfo();
        List<Headquarters> headquartersList = objectInfoRepository.getHeadquarters();
        List<MmsiIp> mmsiIpList = objectInfoRepository.getMmsiIp();
        List<ObjectUndefined> objectUndefineds = objectInfoRepository.getObjectUndefined();
        List<ObjectUndefinedIp> objectUndefinedIps = objectInfoRepository.getObjectUndefinedIp();
        Map<String,List<MmsiIp>> mmsiToIp = new HashMap<>();
        Map<String,List<ObjectUndefinedIp>> idToIp = new HashMap<>();
        for (MmsiIp mmsiIp: mmsiIpList
             ) {
            String key = ""+mmsiIp.getMmsi()+"_"+mmsiIp.getType();
            if(mmsiToIp.get(key)!=null) {
                List<MmsiIp> mmsiIpList1 = mmsiToIp.get(key);
                mmsiIpList1.add(mmsiIp);
                mmsiToIp.replace(key, mmsiIpList1);
            }else {
                List<MmsiIp> mmsiIpList1 = new ArrayList<>();
                mmsiIpList1.add(mmsiIp);
                mmsiToIp.put(key, mmsiIpList1);
            }

        }
        for (ObjectUndefinedIp objectUndefinedIp: objectUndefinedIps
        ) {
            String key = ""+objectUndefinedIp.getUfoId();
            if(idToIp.get(key)!=null) {
                List<ObjectUndefinedIp> ipList = idToIp.get(key);
                ipList.add(objectUndefinedIp);
                idToIp.replace(key, ipList);
            }else {
                List<ObjectUndefinedIp> ipList = new ArrayList<>();
                ipList.add(objectUndefinedIp);
                idToIp.put(key, ipList);
            }
        }
        for ( AisInfo aisInfo: aisInfoList
             ) {
            String listIp = "";
            String objectId = aisInfo.getMmsi()+"_"+0;
            if(mmsiToIp.get(objectId)!=null){
                List<MmsiIp> mmsiIpList1 = mmsiToIp.get(objectId);
                for (MmsiIp  mmsiIp: mmsiIpList1
                ) {
                    KeytoObject keytoObject = new KeytoObject();
                    keytoObject.setKey(mmsiIp.getIpAddress()+"_"+mmsiIp.getDataSource());
                    keytoObject.setObjectInfo(objectId);
                    redisRepository.saveHashKeyObject(keytoObject);
                    listIp+= mmsiIp.getIpAddress()+"-"+mmsiIp.getDataSource()+",";
                }
                listIp = listIp.substring(0,listIp.length()-1);
                ObjectInfo objectInfo = new ObjectInfo();
                objectInfo.setId(objectId);
                if(aisInfo.getName()!=null && !aisInfo.getName().equals("")) {
                    objectInfo.setName(aisInfo.getName());
                } else {
                    objectInfo.setName(aisInfo.getSourceIp());
                }
                objectInfo.setIps(listIp);
                redisRepository.saveObjectInfo(objectInfo);


            }
        }

        for ( Headquarters aisInfo: headquartersList
        ) {
            String listIp = "";
            String objectId = aisInfo.getId()+"_"+1;
            if(mmsiToIp.get(objectId)!=null){
                List<MmsiIp> mmsiIpList1 = mmsiToIp.get(objectId);
                for (MmsiIp  mmsiIp: mmsiIpList1
                ) {
                    KeytoObject keytoObject = new KeytoObject();
                    keytoObject.setKey(mmsiIp.getIpAddress()+"_"+mmsiIp.getDataSource());
                    keytoObject.setObjectInfo(objectId);
                    redisRepository.saveHashKeyObject(keytoObject);
                    listIp+= mmsiIp.getIpAddress()+"-"+mmsiIp.getDataSource()+",";
                }
                listIp = listIp.substring(0,listIp.length()-1);

                ObjectInfo objectInfo = new ObjectInfo();
                objectInfo.setId(objectId);
                if(aisInfo.getName()!=null && !aisInfo.getName().equals("")) {
                    objectInfo.setName(aisInfo.getName());
                } else {
                    objectInfo.setName(aisInfo.getLatitude()+"-"+aisInfo.getLongitude());
                    objectInfo.setLatitude(aisInfo.getLatitude());
                    objectInfo.setLongitude(aisInfo.getLongitude());
                }
                objectInfo.setIps(listIp);
                redisRepository.saveObjectInfo(objectInfo);

            }
        }

        for ( ObjectUndefined aisInfo: objectUndefineds
        ) {
            String listIp = "";
            String objectId = aisInfo.getUuid();
            if (idToIp.get(objectId) != null) {
                List<ObjectUndefinedIp> objectUndefinedIpList = idToIp.get(objectId);
                for (ObjectUndefinedIp mmsiIp : objectUndefinedIpList
                ) {
                    KeytoObject keytoObject = new KeytoObject();
                    keytoObject.setKey(mmsiIp.getIpAddress() + "_" + mmsiIp.getDataSource());
                    keytoObject.setObjectInfo(objectId);
                    redisRepository.saveHashKeyObject(keytoObject);
                    listIp += mmsiIp.getIpAddress() + "-" + mmsiIp.getDataSource() + ",";
                }
                listIp = listIp.substring(0, listIp.length() - 1);
                ObjectInfo objectInfo = new ObjectInfo();
                objectInfo.setId(objectId);
                if (aisInfo.getName() != null && !aisInfo.getName().equals("")) {
                    objectInfo.setName(aisInfo.getName());
                } else {
                    objectInfo.setName(aisInfo.getSourceIp());
                }
                objectInfo.setIps(listIp);
                redisRepository.saveObjectInfo(objectInfo);

            }
        }
        System.out.println("ads");



    }
}
