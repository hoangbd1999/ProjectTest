package com.elcom.metacen.content.load;

import com.elcom.metacen.content.dto.FolderDTO;
import com.elcom.metacen.content.redis.RedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LoadRedis {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRedis.class);

    @Autowired
    private RedisRepository redisRepository;

//    @Bean
    public void loading(){
        List<FolderDTO> list = new ArrayList<>();
        list.add( new FolderDTO("48QYJ",21.692146831,106.93317948,21.678072962,107.89898443, 20.775687491,107.88139149,20.78912145,106.92143665 ));
        list.add( new FolderDTO("48QZJ",21.678072962,107.89898443 ,21.658395998,108.86384926 , 20.75690501,108.84043169 ,20.775687491,107.88139149 ));
        list.add( new FolderDTO("49QBD",	21.678072962,108.10101557  ,21.692146831 ,109.06682052  , 20.78912145 ,109.07856335  ,20.775687491 ,108.11860851));
        list.add( new FolderDTO("49QCD",21.692146831,109.06682052  ,21.700598725 ,110.03325318  , 20.797188998 ,110.03912886  ,20.78912145 ,109.07856335 ));
        list.add( new FolderDTO("48QXH",20.797188998 ,105.96087114   ,20.78912145 ,106.92143665   , 19.885995125 ,106.91030469   ,19.893682727 ,105.95530117 ));
        list.add( new FolderDTO("48QYH",20.78912145 ,106.92143665  ,20.775687491 ,107.88139149  , 19.873193764 ,107.86471347  ,19.885995125 ,106.91030469 ));
        list.add( new FolderDTO("48QZH",20.775687491 ,107.88139149  ,20.75690501 ,108.84043169 , 19.855295569 ,108.81823143 ,19.873193764,107.86471347 ));
        list.add( new FolderDTO("49QBC",20.775687491,108.11860851 ,20.78912145 ,109.07856335 , 19.885995125 ,109.08969531 ,19.873193764 ,108.13528653));
        list.add( new FolderDTO("49QCC",20.78912145 ,109.07856335 ,20.797188998 ,110.03912886  , 19.893682727 ,110.04469883  ,19.885995125 ,109.08969531 ));
        list.add( new FolderDTO("48QWG",19.896246394 ,105,19.893682727 ,105.95530117,18.990083412 ,105.95002892,18.992521748,105 ));
        list.add( new FolderDTO("48QXG",19.893682727,105.95530117  ,19.885995125 ,106.91030469  , 18.982771613 ,106.89976766 ,18.990083412 ,105.95002892));
        list.add( new FolderDTO("48QYG",	19.885995125,106.91030469   ,19.873193764,107.86471347   , 18.970595965,107.84892652   ,18.982771613,106.89976766));
        list.add( new FolderDTO("48QZG",19.873193764,107.86471347    ,19.855295569,108.81823143   , 18.953572458,108.79721682  ,18.970595965,107.84892652));
        list.add( new FolderDTO("49QBB",19.873193764,108.13528653     ,19.885995125,109.08969531   , 18.982771613,109.10023234   ,18.982771613 ,109.10023234) );
        list.add( new FolderDTO("48QWF",18.992521748,105  ,18.990083412,105.95002892  , 18.086394638,105.94504695   ,18.088708943,105 ));
        list.add( new FolderDTO("48QXF",18.990083412,105.95002892  ,18.982771613,106.89976766   , 18.079454749 ,106.88981067  ,18.086394638 ,105.94504695) );
        list.add( new FolderDTO("48QYF",18.982771613,106.89976766   ,18.970595965,107.84892652  , 18.067898343,107.83400839 ,18.079454749 ,106.88981067 ));
        list.add( new FolderDTO("48QZF",18.970595965,107.84892652  ,18.953572458,108.79721682 , 18.051740499,108.77735831 ,18.067898343 ,107.83400839));
        list.add( new FolderDTO("49QBA",18.970595965,108.15107348 ,18.982771613, 109.10023234 , 18.079454749,109.11018933  ,18.067898343,108.16599161) );
        list.add( new FolderDTO("49QCA",18.982771613,109.10023234  ,18.990083412,110.04997108  , 18.086394638,110.05495305   ,18.079454749,109.11018933 ));

        list.add( new FolderDTO("49QDA",18.990083412,110.04997108  ,18.992521748,111,18.088708943,111,18.086394638,110.05495305));
        list.add(new FolderDTO("48QXE","18.086394638 105.94504695 18.079454749 106.88981067 17.176048447 106.88041982 17.182620081 105.94034829"));
        list.add(new FolderDTO("48QYE","18.079454749 106.88981067 18.067898343 107.83400839 17.165105207 107.8199383 17.176048447 106.88041982"));
        list.add(new FolderDTO("48QZE","18.067898343 107.83400839 18.051740499 108.77735831 17.149804554 108.75862833 17.165105207 107.8199383"));
        list.add(new FolderDTO("49QBV","18.067898343 108.16599161 18.079454749 109.11018933 17.176048447 109.11958018 17.165105207 108.1800617"));
        list.add(new FolderDTO("49QCV","18.079454749 109.11018933 18.086394638 110.05495305 17.182620081 110.05965171 17.176048447 109.11958018"));
        list.add(new FolderDTO("49QDV","18.086394638 110.05495305 18.088708943 111 17.184811575 111 17.182620081 110.05965171"));
        list.add(new FolderDTO("49QEV","18.088708943 111 18.086394638 111.94504695 17.182620081 111.94034829 17.184811575 111"));
        list.add(new FolderDTO("48QYD","17.176048447 106.88041982 17.165105207 107.8199383 16.262220927 107.80669689 16.272556694 106.87158218"));

        list.add(new FolderDTO("48QZD","17.165105207 107.8199383 17.149804554 108.75862833 16.247769526 108.74100115 16.262220927 107.80669689"));
        list.add(new FolderDTO("49QBU","17.165105207 108.1800617 17.176048447 109.11958018 16.272556694 109.12841782 16.262220927 108.19330311"));
        list.add(new FolderDTO("49QCU","17.176048447 109.11958018 17.182620081 110.05965171 16.278763497 110.06407355 16.272556694 109.12841782"));
        list.add(new FolderDTO("49QDU","17.182620081 110.05965171 17.184811575 111 16.280833323 111 16.278763497 110.06407355"));

        list.add(new FolderDTO("49QEU","17.184811575 111 17.182620081 111.94034829 16.278763497 111.93592645 16.280833323 111"));
        list.add(new FolderDTO("49PBT","16.262220927 108.19330311 16.272556694 109.12841782 15.368983548 109.1367143 15.359249931 108.20573385"));
        list.add(new FolderDTO("49PCT","16.272556694 109.12841782 16.278763497 110.06407355 15.374828723 110.06822459 15.368983548 109.1367143"));
        list.add(new FolderDTO("49PDT","16.278763497 110.06407355 16.280833323 111 15.37677795 111 15.374828723 110.06822459"));


        list.add(new FolderDTO("49PET","16.280833323 111 16.278763497 111.93592645 15.374828723 111.93177541 15.37677795 111"));
        list.add(new FolderDTO("49PFT","16.278763497 111.93592645 16.272556694 112.87158218 15.368983548 112.8632857 15.374828723 111.93177541"));
        list.add(new FolderDTO("49PBS","15.359249931 108.20573385 15.368983548 109.1367143 14.465333137 109.1444808 14.456196702 108.21737065"));
        list.add(new FolderDTO("49PCS","15.368983548 109.1367143 15.374828723 110.06822459 14.470819672 110.07211043 14.465333137 109.1444808"));

        list.add(new FolderDTO("49PDS","15.374828723 110.06822459 15.37677795 111 14.472649297 111 14.470819672 110.07211043"));
        list.add(new FolderDTO("49PES","15.37677795 111 15.374828723 111.93177541 14.470819672 111.92788957 14.472649297 111"));
        list.add(new FolderDTO("49PFS","15.374828723 111.93177541 15.368983548 112.8632857 14.465333137 112.8555192 14.470819672 111.92788957"));
        list.add(new FolderDTO("49PCR","14.465333137 109.1444808 14.470819672 110.07211043 13.566740328 110.07573628 13.561609653 109.1517277"));

        list.add(new FolderDTO("49PDR","14.470819672 110.07211043 14.472649297 111 13.568451278 111 13.566740328 110.07573628"));
        list.add(new FolderDTO("49PER","14.472649297 111 14.470819672 111.92788957 13.566740328 111.92426372 13.568451278 111"));
        list.add(new FolderDTO("49PFR","14.470819672 111.92788957 14.465333137 112.8555192 13.561609653 112.8482723 13.566740328 111.92426372"));
        list.add(new FolderDTO("49PCQ","13.561609653 109.1517277 13.566740328 110.07573628 12.662594744 110.07910692 12.657817352 109.15846456"));

        list.add(new FolderDTO("49PDQ","13.566740328 110.07573628 13.568451278 111 12.664187881 111 12.662594744 110.07910692"));
        list.add(new FolderDTO("49PEQ","13.568451278 111 13.566740328 111.92426372 12.662594744 111.92089308 12.664187881 111"));
        list.add(new FolderDTO("49PFQ","13.566740328 111.92426372 13.561609653 112.8482723 12.657817352 112.84153544 12.662594744 111.92089308"));
        list.add(new FolderDTO("49PCP","12.657817352 109.15846456 12.662594744 110.07910692 11.758387039 110.08222677 11.753960548 109.16470022"));

        list.add(new FolderDTO("49PDP","12.662594744 110.07910692 12.664187881 111 11.759863157 111 11.758387039 110.08222677"));
        list.add(new FolderDTO("49PEP","12.664187881 111 12.662594744 111.92089308 11.758387039 111.91777323 11.759863157 111"));
        list.add(new FolderDTO("49PFP","12.662594744 111.92089308 12.657817352 112.84153544 11.753960548 112.83529978 11.758387039 111.91777323"));
        for (FolderDTO folder: list
             ) {
            redisRepository.saveFolder(folder);
        }
        LOGGER.info("oke");





    }

//    @Bean
//    public void loading(){
//        List<AisInfo> aisInfoList = objectInfoRepository.getAisInfo();
//        List<Headquarters> headquartersList = objectInfoRepository.getHeadquarters();
//        List<MmsiIp> mmsiIpList = objectInfoRepository.getMmsiIp();
//        List<ObjectUndefined> objectUndefineds = objectInfoRepository.getObjectUndefined();
//        List<ObjectUndefinedIp> objectUndefinedIps = objectInfoRepository.getObjectUndefinedIp();
//        Map<String,List<MmsiIp>> mmsiToIp = new HashMap<>();
//        Map<String,List<ObjectUndefinedIp>> idToIp = new HashMap<>();
//        for (MmsiIp mmsiIp: mmsiIpList
//             ) {
//            String key = ""+mmsiIp.getMmsi()+"_"+mmsiIp.getType();
//            if(mmsiToIp.get(key)!=null) {
//                List<MmsiIp> mmsiIpList1 = mmsiToIp.get(key);
//                mmsiIpList1.add(mmsiIp);
//                mmsiToIp.replace(key, mmsiIpList1);
//            }else {
//                List<MmsiIp> mmsiIpList1 = new ArrayList<>();
//                mmsiIpList1.add(mmsiIp);
//                mmsiToIp.put(key, mmsiIpList1);
//            }
//
//        }
//        for (ObjectUndefinedIp objectUndefinedIp: objectUndefinedIps
//        ) {
//            String key = ""+objectUndefinedIp.getUfoId();
//            if(idToIp.get(key)!=null) {
//                List<ObjectUndefinedIp> ipList = idToIp.get(key);
//                ipList.add(objectUndefinedIp);
//                idToIp.replace(key, ipList);
//            }else {
//                List<ObjectUndefinedIp> ipList = new ArrayList<>();
//                ipList.add(objectUndefinedIp);
//                idToIp.put(key, ipList);
//            }
//        }
//        for ( AisInfo aisInfo: aisInfoList
//             ) {
//            String listIp = "";
//            String objectId = aisInfo.getMmsi()+"_"+0;
//            if(mmsiToIp.get(objectId)!=null){
//                List<MmsiIp> mmsiIpList1 = mmsiToIp.get(objectId);
//                for (MmsiIp  mmsiIp: mmsiIpList1
//                ) {
//                    KeytoObject keytoObject = new KeytoObject();
//                    keytoObject.setKey(mmsiIp.getIpAddress()+"_"+mmsiIp.getDataSource());
//                    keytoObject.setObjectInfo(objectId);
//                    redisRepository.saveHashKeyObject(keytoObject);
//                    listIp+= mmsiIp.getIpAddress()+"-"+mmsiIp.getDataSource()+",";
//                }
//                listIp = listIp.substring(0,listIp.length()-1);
//                ObjectInfo objectInfo = new ObjectInfo();
//                objectInfo.setId(objectId);
//                if(aisInfo.getName()!=null && !aisInfo.getName().equals("")) {
//                    objectInfo.setName(aisInfo.getName());
//                } else {
//                    objectInfo.setName(aisInfo.getSourceIp());
//                }
//                objectInfo.setIps(listIp);
//                redisRepository.saveObjectInfo(objectInfo);
//
//
//            }
//        }
//
//        for ( Headquarters aisInfo: headquartersList
//        ) {
//            String listIp = "";
//            String objectId = aisInfo.getId()+"_"+1;
//            if(mmsiToIp.get(objectId)!=null){
//                List<MmsiIp> mmsiIpList1 = mmsiToIp.get(objectId);
//                for (MmsiIp  mmsiIp: mmsiIpList1
//                ) {
//                    KeytoObject keytoObject = new KeytoObject();
//                    keytoObject.setKey(mmsiIp.getIpAddress()+"_"+mmsiIp.getDataSource());
//                    keytoObject.setObjectInfo(objectId);
//                    redisRepository.saveHashKeyObject(keytoObject);
//                    listIp+= mmsiIp.getIpAddress()+"-"+mmsiIp.getDataSource()+",";
//                }
//                listIp = listIp.substring(0,listIp.length()-1);
//
//                ObjectInfo objectInfo = new ObjectInfo();
//                objectInfo.setId(objectId);
//                if(aisInfo.getName()!=null && !aisInfo.getName().equals("")) {
//                    objectInfo.setName(aisInfo.getName());
//                } else {
//                    objectInfo.setName(aisInfo.getLatitude()+"-"+aisInfo.getLongitude());
//                    objectInfo.setLatitude(aisInfo.getLatitude());
//                    objectInfo.setLongitude(aisInfo.getLongitude());
//                }
//                objectInfo.setIps(listIp);
//                redisRepository.saveObjectInfo(objectInfo);
//
//            }
//        }
//
//        for ( ObjectUndefined aisInfo: objectUndefineds
//        ) {
//            String listIp = "";
//            String objectId = aisInfo.getUuid();
//            if (idToIp.get(objectId) != null) {
//                List<ObjectUndefinedIp> objectUndefinedIpList = idToIp.get(objectId);
//                for (ObjectUndefinedIp mmsiIp : objectUndefinedIpList
//                ) {
//                    KeytoObject keytoObject = new KeytoObject();
//                    keytoObject.setKey(mmsiIp.getIpAddress() + "_" + mmsiIp.getDataSource());
//                    keytoObject.setObjectInfo(objectId);
//                    redisRepository.saveHashKeyObject(keytoObject);
//                    listIp += mmsiIp.getIpAddress() + "-" + mmsiIp.getDataSource() + ",";
//                }
//                listIp = listIp.substring(0, listIp.length() - 1);
//                ObjectInfo objectInfo = new ObjectInfo();
//                objectInfo.setId(objectId);
//                if (aisInfo.getName() != null && !aisInfo.getName().equals("")) {
//                    objectInfo.setName(aisInfo.getName());
//                } else {
//                    objectInfo.setName(aisInfo.getSourceIp());
//                }
//                objectInfo.setIps(listIp);
//                redisRepository.saveObjectInfo(objectInfo);
//
//            }
//        }
//        System.out.println("ads");
//
//
//
//    }
}
