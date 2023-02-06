package elcom.com.neo4j.loadRedis;

import elcom.com.neo4j.controller.BaseController;
import elcom.com.neo4j.dto.KeytoObject;
import elcom.com.neo4j.dto.MappingVsatRequestDTO;
import elcom.com.neo4j.dto.MarineVesselDTO;
import elcom.com.neo4j.dto.ObjectInfo;
import elcom.com.neo4j.model.*;
import elcom.com.neo4j.redis.RedisRepository;
import elcom.com.neo4j.repositorymogo.ConfigObjectRepository;
import elcom.com.neo4j.service.LinkObjectService;
import elcom.com.neo4j.service.MappingVsatMetacenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Controller
public class LoadRedis extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRedis.class);

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private ConfigObjectRepository objectInfoRepository;

    @Autowired
    private MappingVsatMetacenService mappingVsatMetacenService;

    @Autowired
    private LinkObjectService linkObjectService;


//    @Bean
    public void loading(){
        List<AisInfo> aisInfoList = objectInfoRepository.getAisInfo();
        List<MmsiIp> mmsiIpList = objectInfoRepository.getMmsiIp();
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
        for ( AisInfo aisInfo: aisInfoList
             ) {
            String listIp = "";
            String objectId = aisInfo.getMmsi()+"_"+0;
            if(mmsiToIp.get(objectId)!=null) {
                List<MmsiIp> mmsiIpList1 = mmsiToIp.get(objectId);
                MappingVsatMetacen mappingVsatMetacen = null;
                Map<String, Object> body = convert(aisInfo);
                MarineVesselDTO marineVesselDTO = callSaveContact(body);
                if (marineVesselDTO != null){
                    for (MmsiIp mmsiIp : mmsiIpList1
                    ) {
                        MappingVsatRequestDTO mappingVsatRequestDTO = new MappingVsatRequestDTO();
                        mappingVsatRequestDTO.setObjectId(marineVesselDTO.getId());
                        mappingVsatRequestDTO.setObjectName(marineVesselDTO.getName());
                        mappingVsatRequestDTO.setObjectType("VEHICLE");
                        mappingVsatRequestDTO.setObjectUuid(marineVesselDTO.getUuid());
                        mappingVsatRequestDTO.setVsatIpAddress(mmsiIp.getIpAddress());
                        mappingVsatRequestDTO.setVsatDataSourceId(mmsiIp.getDataSource().intValue());
                        mappingVsatRequestDTO.setVsatDataSourceName("APT9_KuLO_H_Evo_1778_ID90");
                        mappingVsatMetacenService.save(mappingVsatRequestDTO, "Hệ thống");

                    }
                }



            }
        }

        System.out.println("oke");



    }

    @Bean
    public void createIndex(){
        try {
            File myObj = new File("config/createIndex.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String query = myReader.nextLine();
                if(!query.isEmpty()) {
                    linkObjectService.createIndex(query);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    private Map<String,Object> convert(AisInfo aisInfo){
        Map<String,Object> result = new HashMap<>();
        result.put("mmsi",aisInfo.getMmsi());
        result.put("name",aisInfo.getName());
        result.put("imo",aisInfo.getName());
        return result;
    }

}
