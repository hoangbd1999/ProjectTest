//package movies.spring.data.neo4j.api;
package elcom.com.neo4j.bussiness;

import elcom.com.neo4j.controller.BaseController;
import elcom.com.neo4j.dto.*;
import elcom.com.neo4j.message.MessageContent;
import elcom.com.neo4j.message.ResponseMessage;
import elcom.com.neo4j.model.*;
import elcom.com.neo4j.node.ObjectToNode;
import elcom.com.neo4j.redis.RedisRepository;
import elcom.com.neo4j.repository.ConfigObjectRepository;
import elcom.com.neo4j.service.NodeTopologyService;
import elcom.com.neo4j.service.TopologyService;
import elcom.com.neo4j.service.impl.ObjectServiceImpl;
import elcom.com.neo4j.service.impl.ReProcessingService;
import elcom.com.neo4j.service.impl.TopoVsatService;
import elcom.com.neo4j.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Michael J. Simons
 */
@Controller
public class Neo4jController extends BaseController {

    @Autowired
    private ObjectServiceImpl objectService;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private ConfigObjectRepository objectRepository;

    @Autowired
    private ReProcessingService reProcessingService;

    @Autowired
    private TopoVsatService topoVsatService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${fabric.graph1}")
    private String urlNeo4j;

    @Value("${basic.neo4j}")
    private String basicNeo4j;

    @Autowired
    private TopologyService topologyService;
    @Autowired
    private NodeTopologyService nodeTopologyService;


    public ResponseMessage addNode(Map<String, Object> bodyParam, Map<String, String> headerMap){
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));
        String ip = (String) bodyParam.get("ip");
        Integer dataSource = (Integer) bodyParam.get("dataSource");
        String idObject = (String) bodyParam.get("idObject");
        Integer type = (Integer) bodyParam.get("type");
        Integer countDay = (Integer) bodyParam.get("countDay");
        KeytoObject keytoObject = redisRepository.findIdObject(ip+"_"+dataSource);
        String ips ="";
        List<AisInfo> aisInfoList = new ArrayList<>();
        List<Headquarters> headquartersList = new ArrayList<>();
        List<MmsiIp> mmsiIpList = new ArrayList<>();
        List<ObjectUndefined> objectUndefineds = new ArrayList<>();
        List<ObjectUndefinedIp> objectUndefinedIps = new ArrayList<>();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(now);

        cal.add(Calendar.DAY_OF_MONTH,countDay*-1);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = dff.format(cal.getTime());
        cal.setTime(now);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
//            cal.add(Calendar.MINUTE,-15);
        String endTime =  dff.format(cal.getTime());
        if(keytoObject!=null){
            redisRepository.removeKeyToObjectRedis(ip+"_"+dataSource);
            ObjectInfo objectInfo = redisRepository.findObjectInfoId(keytoObject.getObjectInfo());
            if(objectInfo!=null){
                String objectInfoId = objectInfo.getId();
                objectInfoId = "a" + objectInfoId.replace(".", "_");
                objectInfoId = objectInfoId.replace("-", "_");
                objectInfoId = objectInfoId.replace("?", "_");
                List<String> arrayList= new ArrayList<>();
                arrayList.add(objectInfoId);
            }

            String listIp = objectInfo.getIps();

            while (listIp.indexOf(",")>=0){
                ips+="'"+listIp.substring(0,listIp.indexOf("-")) +"',";
                listIp =listIp.substring(listIp.indexOf(",")+1) ;
            }
            ips +="'"+ listIp.substring(0,listIp.indexOf("-")) +"'";
            Integer mmsi =-999;
//            String query = "MATCH (j:Object)-[r:MEDIA]->(i:Object) where (r.dest = '"+objectInfo.getId()+"' or r.src = '"+objectInfo.getId()+"') and r.endTime >='"+ startTime +"' and r.endTime <= '"+endTime+"' delete r";
            if(objectInfo.getId().indexOf("?")>0){
               String queryDelete = "MATCH (j:Object) where j.ids='"+objectInfo.getId()+"'" +" DETACH DELETE j";
               objectService.updateNode("use metacenv1.metacenday "+queryDelete );
               objectService.updateNode("use metacenv1.metacenmonth "+queryDelete );
               objectService.updateNode("use metacenv1.metacenhour "+queryDelete );

            } else {
                String queryDelete= "MATCH (j:Object) where j.ids='"+objectInfo.getId()+"'" +" DETACH DELETE j";
                objectService.updateNode("use metacenv1.metacenday "+queryDelete);
                objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
                objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
//                String queryUpdate= "MATCH (j:Object) where j.ids='"+objectInfo.getId()+"'" +" Set j.ids ='" +objectInfo.getId()+"??'";
                if(objectInfo.getId().indexOf("_")>0){
                    mmsi =  Integer.valueOf(objectInfo.getId().substring(0,objectInfo.getId().indexOf("_")));
                    Integer typeTmp = Integer.valueOf(objectInfo.getId().substring(objectInfo.getId().indexOf("_")+1));
                    mmsiIpList = objectRepository.getMmsiIp(mmsi);
                    if(typeTmp==0) {
                        aisInfoList = objectRepository.getAisInfo(mmsi);
                    } else {
                        headquartersList = objectRepository.getHeadquarters(mmsi);
                    }
                }else {
                    objectUndefineds = objectRepository.getObjectUndefined(objectInfo.getId());
                    objectUndefinedIps = objectRepository.getObjectUndefinedIp(objectInfo.getId());
                }
            }
            if(type==0){
                ObjectInfo objectInfoUpadte = redisRepository.findObjectInfoId(idObject+"_"+0);
                if(objectInfoUpadte!=null) {
                    String queryDelete= "MATCH (j:Object) where j.ids='"+objectInfoUpadte.getId()+"'" +" DETACH DELETE j";
                    objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenday "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
//                    String queryDelete = "MATCH (j:Object)-[r:MEDIA]->(i:Object) where (r.dest = '"+objectInfoUpadte.getId()+"' or r.src = '"+objectInfoUpadte.getId()+"') and r.endTime >='"+ startTime +"' and r.endTime <= '"+endTime+"' delete r";
                    String objectInfoUpdateId = objectInfoUpadte.getId();
                    objectInfoUpdateId = "a" + objectInfoUpdateId.replace(".", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("-", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("?", "_");
                    listIp = objectInfoUpadte.getIps();
                    ips += ",";
                    while (listIp.indexOf(",")>=0){
                        ips+="'"+listIp.substring(0,listIp.indexOf("-")) +"',";
                        listIp =listIp.substring(listIp.indexOf(",")+1) ;
                    }
                    ips +="'"+ listIp.substring(0,listIp.indexOf("-")) +"',";
                }
                int check = Integer.valueOf(idObject);
                if(mmsi!=check) {
                    List<MmsiIp> mmsiIpList1 = objectRepository.getMmsiIp(Integer.valueOf(idObject));
                    if (mmsiIpList1 != null) {
                        mmsiIpList.addAll(mmsiIpList1);
                        for (MmsiIp mmsiIp : mmsiIpList1) {
                            ips += "'" + mmsiIp.getIpAddress() + "',";
                        }
                    }
                    List<AisInfo> aisInfoList1 = objectRepository.getAisInfo(Integer.valueOf(idObject));
                    if(aisInfoList1!=null){
                        aisInfoList.addAll(aisInfoList1);
                    }
                }

            } else if(type==1){
                ObjectInfo objectInfoUpadte = redisRepository.findObjectInfoId(keytoObject.getObjectInfo()+"_"+1);
                if(objectInfoUpadte!=null) {
                    String queryDelete= "MATCH (j:Object) where j.ids='"+objectInfoUpadte.getId()+"'" +" DETACH DELETE j";
                    objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenday "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
//                    String queryDelete = "MATCH (j:Object)-[r:MEDIA]->(i:Object) where (r.dest = '"+objectInfoUpadte.getId()+"' or r.src = '"+objectInfoUpadte.getId()+"') and r.endTime >='"+ startTime +"' and r.endTime <= '"+endTime+"' delete r";
                    String objectInfoUpdateId = objectInfoUpadte.getId();
                    objectInfoUpdateId = "a" + objectInfoUpdateId.replace(".", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("-", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("?", "_");
                    listIp = objectInfoUpadte.getIps();
                    ips += ",";
                    while (listIp.indexOf(",")>=0){
                        ips+="'"+listIp.substring(0,listIp.indexOf("-")) +"',";
                        listIp =listIp.substring(listIp.indexOf(",")+1) ;
                    }
                    ips +="'"+ listIp.substring(0,listIp.indexOf("-")) +"',";
                }
                int check = Integer.valueOf(idObject);
                if(mmsi!=check) {
                    List<MmsiIp> mmsiIpList1 = objectRepository.getMmsiIp(Integer.valueOf(idObject));
                    if (mmsiIpList1 != null) {
                        mmsiIpList.addAll(mmsiIpList1);
                        for (MmsiIp mmsiIp : mmsiIpList1) {
                            ips += "'" + mmsiIp.getIpAddress() + "',";
                        }
                    }
                    List<Headquarters> headquartersList1 = objectRepository.getHeadquarters(Integer.valueOf(idObject));
                    if (headquartersList1 != null) {
                        headquartersList.addAll(headquartersList1);
                    }
                }
            } else {
                ObjectInfo objectInfoUpadte = redisRepository.findObjectInfoId(keytoObject.getObjectInfo());
                if(objectInfoUpadte!=null) {
                    String queryDelete= "MATCH (j:Object) where j.ids='"+objectInfoUpadte.getId()+"'" +" DETACH DELETE j";
                    objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenday "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
//                    String queryDelete = "MATCH (j:Object)-[r:MEDIA]->(i:Object) where (r.dest = '"+objectInfoUpadte.getId()+"' or r.src = '"+objectInfoUpadte.getId()+"') and r.endTime >='"+ startTime +"' and r.endTime <= '"+endTime+"' delete r";
                    String objectInfoUpdateId = objectInfoUpadte.getId();
                    objectInfoUpdateId = "a" + objectInfoUpdateId.replace(".", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("-", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("?", "_");
                    listIp = objectInfoUpadte.getIps();
                    ips += ",";
                    while (listIp.indexOf(",")>=0){
                        ips+="'"+listIp.substring(0,listIp.indexOf("-")) +"',";
                        listIp =listIp.substring(listIp.indexOf(",")+1) ;
                    }
                    ips +="'"+ listIp.substring(0,listIp.indexOf("-")) +"',";
                }
                int check = Integer.valueOf(idObject);
                if(mmsi!=check) {
                    List<ObjectUndefined> objectUndefined1 = objectRepository.getObjectUndefined(idObject);
                    if (objectUndefined1 != null) {
                        objectUndefineds.addAll(objectUndefined1);
                    }
                    List<ObjectUndefinedIp> objectUndefinedIps1 = objectRepository.getObjectUndefinedIp(idObject);
                    if (objectUndefinedIps1 != null) {
                        objectUndefinedIps.addAll(objectUndefinedIps1);
                        for (ObjectUndefinedIp mmsiIp : objectUndefinedIps1) {
                            ips += "'" + mmsiIp.getIpAddress() + "',";
                        }
                    }
                }
            }
        } else {
            String queryDelete= "MATCH (j:Object) where j.ids='"+ip+"-"+dataSource+"?"+"'" +" DETACH DELETE j";
            objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
            objectService.updateNode("use metacenv1.metacenday "+queryDelete);
            objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
            String listIp="";
            if(type==0){
                ObjectInfo objectInfoUpdate = redisRepository.findObjectInfoId(idObject+"_"+0);
                if(objectInfoUpdate!=null) {
                    String queryDeleteNew = "MATCH (j:Object) where j.ids='"+objectInfoUpdate.getId()+"'" +" DETACH DELETE j";
                    objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenday "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
//                    String queryDelete = "MATCH (j:Object)-[r:MEDIA]->(i:Object) where (r.dest = '"+objectInfoUpadte.getId()+"' or r.src = '"+objectInfoUpadte.getId()+"') and r.endTime >='"+ startTime +"' and r.endTime <= '"+endTime+"' delete r";
                    String objectInfoUpdateId = objectInfoUpdate.getId();
                    objectInfoUpdateId = "a" + objectInfoUpdateId.replace(".", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("-", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("?", "_");
                    listIp = objectInfoUpdate.getIps();
                    while (listIp.indexOf(",")>=0){
                        ips+="'"+listIp.substring(0,listIp.indexOf("-")) +"',";
                        listIp =listIp.substring(listIp.indexOf(",")+1) ;
                    }
                    ips +="'"+ listIp.substring(0,listIp.indexOf("-")) +"',";
                }

                List<MmsiIp> mmsiIpList1 = objectRepository.getMmsiIp(Integer.valueOf(idObject));
                if(mmsiIpList1!=null){
                    mmsiIpList.addAll(mmsiIpList1);
                    for (MmsiIp mmsiIp : mmsiIpList1){
                        ips+="'"+mmsiIp.getIpAddress()+"',";
                    }
                }
                List<AisInfo> aisInfoList1 = objectRepository.getAisInfo(Integer.valueOf(idObject));
                if(aisInfoList1!=null){
                    aisInfoList.addAll(aisInfoList1);
                }
            } else if(type==1){
                ObjectInfo objectInfoUpadte = redisRepository.findObjectInfoId(keytoObject.getObjectInfo()+"_"+1);
                if(objectInfoUpadte!=null) {
                    String queryDeleteNew= "MATCH (j:Object) where j.ids='"+objectInfoUpadte.getId()+"'" +" DETACH DELETE j";
                    objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenday "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
//                    String queryDelete = "MATCH (j:Object)-[r:MEDIA]->(i:Object) where (r.dest = '"+objectInfoUpadte.getId()+"' or r.src = '"+objectInfoUpadte.getId()+"') and r.endTime >='"+ startTime +"' and r.endTime <= '"+endTime+"' delete r";
                    String objectInfoUpdateId = objectInfoUpadte.getId();
                    objectInfoUpdateId = "a" + objectInfoUpdateId.replace(".", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("-", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("?", "_");
                    listIp = objectInfoUpadte.getIps();
                    while (listIp.indexOf(",")>=0){
                        ips+="'"+listIp.substring(0,listIp.indexOf("-")) +"',";
                        listIp =listIp.substring(listIp.indexOf(",")+1) ;
                    }
                    ips +="'"+ listIp.substring(0,listIp.indexOf("-")) +"',";
                }
                List<MmsiIp> mmsiIpList1 = objectRepository.getMmsiIp(Integer.valueOf(idObject));
                if(mmsiIpList1!=null){
                    mmsiIpList.addAll(mmsiIpList1);
                    for (MmsiIp mmsiIp : mmsiIpList1){
                        ips+="'"+mmsiIp.getIpAddress()+"',";
                    }
                }
                List<Headquarters> headquartersList1 = objectRepository.getHeadquarters(Integer.valueOf(idObject));
                if(headquartersList1!=null){
                    headquartersList.addAll(headquartersList1);
                }
            } else {
                ObjectInfo objectInfoUpadte = redisRepository.findObjectInfoId(keytoObject.getObjectInfo());
                if(objectInfoUpadte!=null) {
                    String queryDeleteNew= "MATCH (j:Object) where j.ids='"+objectInfoUpadte.getId()+"'" +" DETACH DELETE j";
                    objectService.updateNode("use metacenv1.metacenmonth "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenday "+queryDelete);
                    objectService.updateNode("use metacenv1.metacenhour "+queryDelete);
//                    String queryUpdate =  "MATCH (j:Object) where j.ids='"+objectInfoUpadte.getId()+"'" +" Set j.ids ='" +objectInfoUpadte.getId()+"??'";
//                    String queryDelete = "MATCH (j:Object)-[r:MEDIA]->(i:Object) where (r.dest = '"+objectInfoUpadte.getId()+"' or r.src = '"+objectInfoUpadte.getId()+"') and r.endTime >='"+ startTime +"' and r.endTime <= '"+endTime+"' delete r";
                    String objectInfoUpdateId = objectInfoUpadte.getId();
                    objectInfoUpdateId = "a" + objectInfoUpdateId.replace(".", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("-", "_");
                    objectInfoUpdateId = objectInfoUpdateId.replace("?", "_");
                    System.out.println("xoa node moi "+ objectInfoUpdateId);
                    listIp = objectInfoUpadte.getIps();
                    while (listIp.indexOf(",")>=0){
                        ips+="'"+listIp.substring(0,listIp.indexOf("-")) +"',";
                        listIp =listIp.substring(listIp.indexOf(",")+1) ;
                    }
                    ips +="'"+ listIp.substring(0,listIp.indexOf("-")) +"',";
                }
                List<ObjectUndefined> objectUndefined1 = objectRepository.getObjectUndefined(idObject);
                if(objectUndefined1!=null){
                    objectUndefineds.addAll(objectUndefined1);
                }
                List<ObjectUndefinedIp> objectUndefinedIps1 = objectRepository.getObjectUndefinedIp(idObject);
                if(objectUndefinedIps1!=null){
                    objectUndefinedIps.addAll(objectUndefinedIps1);
                    for (ObjectUndefinedIp mmsiIp : objectUndefinedIps1){
                        ips+="'"+mmsiIp.getIpAddress()+"',";
                    }
                }
            }

        }
        loadRedis(mmsiIpList,objectUndefinedIps,aisInfoList,headquartersList,objectUndefineds);
        ips=ips.substring(0,ips.length()-1);
        reProcessingService.reProcessing(ips,startTime,endTime);

        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),"success"));
    }

    public ResponseMessage getGraph(Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        String startTime = (String) bodyParam.get("startTime");
        String endTime = (String) bodyParam.get("endTime");
        Integer type = (Integer) bodyParam.get("type");
        Integer typeRelation = (Integer) bodyParam.get("typeRelation");
        String ip = (String) bodyParam.get("ip");
        List<Integer> typeData = (List<Integer>) bodyParam.get("typeData");
        List<Integer> dataSource = (List<Integer>) bodyParam.get("dataSource");
        Boolean  exactly= (Boolean) bodyParam.get("exactly");


        return sendNeo4j(startTime,endTime,ip,type,typeRelation,dataSource,typeData,exactly);
    }
    public ResponseMessage addTopology(Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = (String) bodyParam.get("startTime");
        String endTime = (String) bodyParam.get("endTime");
        String type = (String) bodyParam.get("type");
        String resource = (String) bodyParam.get("resource");
        String name = (String) bodyParam.get("name");
        String ips = (String) bodyParam.get("ips");
        Topology topology = new Topology();
        topology.setId(UUID.randomUUID().toString());
        topology.setName(name);
        topology.setStartTime(dff.parse(startTime));
        topology.setEndTime(dff.parse(endTime));
        topology.setResource(resource);
        topology.setType(type);
        topology.setIps(ips);
        topologyService.saveTopology(topology);




        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),topology));
    }
    public ResponseMessage updateTopology(Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String id = (String) bodyParam.get("id");
        String startTime = (String) bodyParam.get("startTime");
        String endTime = (String) bodyParam.get("endTime");
        String type = (String) bodyParam.get("type");
        String resource = (String) bodyParam.get("resource");
        String name = (String) bodyParam.get("name");
        String ips = (String) bodyParam.get("ips");
        Topology topology = new Topology();
        topology.setId(id);
        topology.setName(name);
        topology.setStartTime(dff.parse(startTime));
        topology.setEndTime(dff.parse(endTime));
        topology.setResource(resource);
        topology.setType(type);
        topology.setIps(ips);
        if(topologyService.findById(id).isPresent()) {
            topologyService.saveTopology(topology);
        }else {
            return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(),"Không tìm thấy topology",null));
        }

        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),topology));
    }
    public ResponseMessage getTopology( Map<String, String> headerMap,String param) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        Map<String, String> params = StringUtil.getUrlParamValues(param);
        Integer size = Integer.valueOf(params.get("size"));
        Integer page = Integer.valueOf(params.get("page"));
        Pageable paging = PageRequest.of(page, size, Sort.by("name").descending());

        Page<Topology> topologyPage = topologyService.findAll(paging);
        TopologyPageDto pageDto = new TopologyPageDto();
        pageDto.setData(topologyPage.getContent());
        pageDto.setSize(topologyPage.getTotalElements());

        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),pageDto));
    }

    public ResponseMessage deleteTopo( Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        List<String> ids= (List<String>) bodyParam.get("ids");
        for (String id: ids
             ) {
            if(!topologyService.findById(id).isPresent()){
                return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(),"Không tìm thấy topology " +id,null));
            }
        }

        topologyService.delete(ids);

        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),
                "Xóa thành công"));
    }

    public ResponseMessage getGraphDeep(Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        String startTime = (String) bodyParam.get("startTime");
        String endTime = (String) bodyParam.get("endTime");
        Integer type = (Integer) bodyParam.get("type");
        Integer typeRelation = (Integer) bodyParam.get("typeRelation");
        String ip = (String) bodyParam.get("ip");
        List<Integer> typeData = (List<Integer>) bodyParam.get("typeData");
        Integer deep = (Integer) bodyParam.get("deep");
        List<Integer> dataSource = (List<Integer>) bodyParam.get("dataSource");
        Boolean  exactly= (Boolean) bodyParam.get("exactly");
        String ids = (String) bodyParam.get("ids");
        Integer page = (Integer) bodyParam.get("page");


        return topoVsat(startTime,endTime,ip,type,typeRelation,deep,typeData,dataSource,exactly,ids,page);
    }

    private ResponseMessage sendNeo4j(String startTime,String endTime,String ip, Integer type, Integer typeRelation ,List<Integer> dataSource,List<Integer> typeData,Boolean exactly){
        String urlRequest ="http://"+urlNeo4j+ ":7474/db/data/transaction/commit";
        String query;
        if(typeRelation==0){
            query= "match p= (a:Object) -[r:MEDIA] ->(b:Object)";
        } else if(typeRelation==1){
            query= "match p= (a:Object) -[r:AIS] ->(b:Object)";
        } else {
            query= "match p= (a:Object) -[r] ->(b:Object)";
        }
        if(type==0){
            query +="  where r.startTime>='" +startTime+"' and r.startTime <='"+endTime+"' ";
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
                }else {
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
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
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
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
                            query += "  a.ips =~ '.*"  + "-" + dataSource.get(i) + ".*' ";
                        else
                            query += " or a.ips =~ '.*" + "-" + dataSource.get(i) + ".*' ";
                    }
                    query += " )";
                }
            }
        } else if(type==1){
            query +="  where r.startTime>='" +startTime+"' and r.startTime <='"+endTime+"' ";
            if(!StringUtil.isNullOrEmpty(ip) && dataSource!=null){
                if(exactly) {
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +="  b.ips contains '"+ip+ "-"+dataSource.get(i)+"' ";
                        else
                            query += " or b.ips contains '" + ip + "-" + dataSource.get(i) + "' ";
                    }
                    query += " )";

                }else {
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
                    }
                    query+= " and(";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=  " b.ips =~ '"+ip+ "-" + dataSource.get(i) + ".*' ";
                        else
                            query +=  " or b.ips =~ '"+ip+ "-" + dataSource.get(i) + ".*' ";
                    }
                    query += " )";

                }

            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    query +=" and b.ips contains '"+ip+ "' ";
                } else if(dataSource!=null){
                    query+= " and (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  b.ips contains '"  + "-" + dataSource.get(i) + "' ";
                        else
                            query += " or b.ips contains '" + "-" + dataSource.get(i) + "' ";
                    }
                    query += " )";
                }
            }
        } else {
            query +="  where r.startTime>='" +startTime+"' and r.startTime <'"+endTime+"' ";
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
//                    ip=ip.replace(".","\\.");
                    query+= " and  a.ips contains '" + ip + "' and(";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=  " a.ips contains '-" + dataSource.get(i) + "' ";
                        else
                            query += "or a.ips contains '-" + dataSource.get(i) + "' ";
                    }
                    query += " )";
                }

            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    query +=" and (a.ips contains '"+ip+"') ";
                } else if(dataSource!=null) {
                    query += " and (";
                    for (int i = 0; i < dataSource.size(); i++) {
                        if (i == 0)
                            query += " a.ips contains '-" + dataSource.get(i) + "' ";
                        else
                            query += " or a.ips contains '-" + dataSource.get(i) + "' ";
                    }
                    query += " )";

                }
            }
        }
        if(typeData!=null&&!typeData.isEmpty()){
            switch (typeData.get(0)){
                case 0:
                    query +=  " and ( ( type(r)='AIS' and r.count >0)";
                    break;
                case 1:
                    query +=  " and ( r.VoiceCount >0";
                    break;
                case 2:
                    query +=  "and (  r.VideoCount >0";
                    break;
                case 3:
                    query +=  "and ( r.WebCount >0";
                    break;
                case 4:
                    query +=  "and ( r.EmailCount >0";
                    break;
                case 5:
                    query +=  "and ( r.TransferFileCount >0";
                    break;
                case 6:
                    query +=  "and ( r.khac >0";
                    break;
            }
        }
        if(typeData!=null&&!typeData.isEmpty()) {
            for (Integer tmp : typeData
            ) {
                switch (tmp) {
                    case 0:
                        query += " or( type(r)='AIS' and r.count >0)";
                        break;
                    case 1:
                        query += " or r.VoiceCount >0";
                        break;
                    case 2:
                        query += " or r.VideoCount >0";
                        break;
                    case 3:
                        query += " or r.WebCount >0";
                        break;
                    case 4:
                        query += " or r.EmailCount >0";
                        break;
                    case 5:
                        query += " or r.TransferFileCount >0";
                        break;
                    case 6:
                        query += " or r.khac >0";
                        break;
                }

            }
        }
        if(typeData!=null&&!typeData.isEmpty()){
           query += ") ";
        }

        query += " return p limit 500";
        //Payload request dbm.root.url//v1.0/dbm/management/mons/recognitions
        Map<String, java.lang.Object> bodyParam = new HashMap<>();
        Statement data = new Statement();
        data.setStatement(query);
        List<String> resultDataContents = new ArrayList<>();
        resultDataContents.add("graph");
        data.setResultDataContents(resultDataContents);
        List<Statement> statements = new ArrayList<>();
        statements.add(data);
        bodyParam.put("statements",statements);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.setBearerAuth("bmVvNGo6ZWxjb21AMTIz");
        headers.setBasicAuth(basicNeo4j);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, java.lang.Object>> requestEntity = new HttpEntity<>(bodyParam,headers);
        //Call rest api with url, method, payload, response entity
        HttpEntity<java.lang.Object> response = restTemplate.exchange(urlRequest, HttpMethod.POST, requestEntity, java.lang.Object.class);
        Map<String, ObjectToNode> mapBody = (Map<String, ObjectToNode>) response.getBody();
        List<ObjectToNode> errors = (List<ObjectToNode>) mapBody.get("errors");
        if(errors!=null && !errors.isEmpty()){
            return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(),"Có lỗi xảy ra",null));
        }
        Map<String,List<ObjectToNode>> results = new HashMap<>();
        results.put("results", (List<ObjectToNode>) mapBody.get("results"));
        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),results));
    }

    private ResponseMessage sendNeo4jDeep(String startTime,String endTime,String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData,List<Integer> dataSource,Boolean
            exactly){
        if(ip!=null)
            ip = ip.trim();
        String urlRequest ="http://"+urlNeo4j+ ":7474/db/data/transaction/commit";
        String query="";
        if(typeRelation==0){
            if(type==2){
                query= "match p= (a:Object) -[r:MEDIA*1.."+deep+"] -(b:Object)";
            }else
                query= "match p= (a:Object) -[r:MEDIA*1.."+deep+"] ->(b:Object)";
        } else if(typeRelation==1){
            if(type==2){
                query= "match p= (a:Object) -[r:AIS*1.."+deep+"] -(b:Object)";
            }else
                query= "match p= (a:Object) -[r:AIS*1.."+deep+"] ->(b:Object) ";
        } else {
            if(type!=2) {
                query = "match p= (a:Object) -[r*1.."+deep+"] ->(b:Object)";
            } else {
                query = "match p= (a:Object) -[r*1.."+deep+"] -(b:Object)";
            }
        }

        if(type==0){
            if(!StringUtil.isNullOrEmpty(ip) && dataSource!=null){
                if(exactly) {
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  a.ips contains '" + ip + "-" + dataSource.get(i) + "' ";
                        else
                            query += " or a.ips contains '" + ip + "-" + dataSource.get(i) + "' ";
                    }
                    query += " )";
                }else {
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  a.ips contains '" + ip + "*-" + dataSource.get(i) + "' ";
                        else
                            query += " or a.ips contains '" + ip + "*-" + dataSource.get(i) + "' ";
                    }
                    query += " )";

                }
            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    query +=" where a.ips contains '"+ip+ "' ";
                } else if(dataSource!=null){
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  a.ips contains '"  + "-" + dataSource.get(i) + "' ";
                        else
                            query += " or a.ips contains '" + "-" + dataSource.get(i) + "' ";
                    }
                    query += " )";
                }
            }
        } else if(type==1){
            if(!StringUtil.isNullOrEmpty(ip) && dataSource!=null){
                if(exactly) {
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +="  b.ips contains '"+ip+ "-"+dataSource.get(i)+"' ";
                        else
                            query += " or b.ips contains '" + ip + "-" + dataSource.get(i) + "' ";
                    }
                    query += " )";

                }else {
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=" b.ips contains '"+ip+ "*-"+dataSource.get(i)+"' ";
                        else
                            query +=" or b.ips contains '"+ip+ "*-"+dataSource.get(i)+"' ";
                    }
                    query += " )";

                }

            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    query +=" where b.ips contains '"+ip+ "' ";
                } else if(dataSource!=null){
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query += "  b.ips contains '"  + "-" + dataSource.get(i) + "' ";
                        else
                            query += " or b.ips contains '" + "-" + dataSource.get(i) + "' ";
                    }
                    query += " )";
                }
            }
        } else {
            if(!StringUtil.isNullOrEmpty(ip) && dataSource!=null){
                if(exactly) {
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=" a.ips contains '"+ip+ "-"+dataSource.get(i)+"'";
                        else
                            query +=" or a.ips contains '"+ip+ "-"+dataSource.get(i)+"'";
                    }
                    query += " )";

                }else {
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=" a.ips contains '"+ip+ "*-"+dataSource.get(i)+"'";
                        else
                            query +=" or a.ips contains '"+ip+ "*-"+dataSource.get(i)+"'";
                    }
                    query += " )";
                }

            } else {
                if(!StringUtil.isNullOrEmpty(ip)){
                    query +=" where (a.ips contains '"+ip+"') ";
                } else if(dataSource!=null){
                    query+= " where (";
                    for (int i=0;i<dataSource.size();i++) {
                        if(i==0)
                            query +=" (a.ips contains '-"+dataSource.get(i)+"'') ";
                        else
                            query +=" or (a.ips contains '-"+dataSource.get(i)+"') ";
                    }
                    query += " )";

                }
            }
        }
//        if(type==0){
//            query +="  where a.ips contains ='" +ip+"' and a<>b";
//        } else if(type==1){
//            query +="  where b.ids ='" +ids+"' and a<>b";
//        } else {
//            query +="  where a.ids ='" +ids+"' ";
//        }
        if(ip==null&&dataSource==null){
            query+= " where  all (r0 in r where r0.endTime>='" +startTime+"' and r0.endTime <='"+endTime+"' ";
        } else
            query+= "  and  all (r0 in r where r0.endTime>='" +startTime+"' and r0.endTime <='"+endTime+"' ";
        if(typeData!=null && !typeData.isEmpty()){
            for (Integer tmp: typeData
            ) {
                switch (tmp){
                    case 0:
                        query +=  " or( type(r0)='AIS' and r0.count >0)";
                        break;
                    case 1:
                        query +=  " or r0.VoiceCount >0";
                        break;
                    case 2:
                        query +=  " or r0.VideoCount >0";
                        break;
                    case 3:
                        query +=  " or r0.WebCount >0";
                        break;
                    case 4:
                        query +=  " or r0.EmailCount >0";
                        break;
                    case 5:
                        query +=  " or r0.TransferFileCount >0";
                        break;
                    case 6:
                        query +=  " or r0.khac >0";
                        break;
                }

            }

        }
        query +=")";




        query += " WITH p SKIP 0 LIMIT 5  return p ";
//        return null;
        //Payload request dbm.root.url//v1.0/dbm/management/mons/recognitions
        Map<String, java.lang.Object> bodyParam = new HashMap<>();
        Statement data = new Statement();
        data.setStatement(query);
        List<String> resultDataContents = new ArrayList<>();
        resultDataContents.add("graph");
        data.setResultDataContents(resultDataContents);
        List<Statement> statements = new ArrayList<>();
        statements.add(data);
        bodyParam.put("statements",statements);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.setBearerAuth("bmVvNGo6ZWxjb21AMTIz");
        headers.setBasicAuth(basicNeo4j);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, java.lang.Object>> requestEntity = new HttpEntity<>(bodyParam,headers);
        //Call rest api with url, method, payload, response entity

        HttpEntity<java.lang.Object> response = restTemplate.exchange(urlRequest, HttpMethod.POST, requestEntity, java.lang.Object.class);
        Map<String, ObjectToNode> mapBody = (Map<String, ObjectToNode>) response.getBody();
        List<ObjectToNode> errors = (List<ObjectToNode>) mapBody.get("errors");
        if(errors!=null && !errors.isEmpty()){
            return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(),"Có lỗi xảy ra",null));
        }
        Map<String,List<ObjectToNode>> results = new HashMap<>();
        results.put("results", (List<ObjectToNode>) mapBody.get("results"));
        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),results));
//        return null;
    }

    private ResponseMessage sendNeo4jDeepUpdate(String startTime,String endTime,String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData,List<Integer> dataSource,Boolean
            exactly){
        String urlRequest ="http://"+urlNeo4j+ ":7474/db/data/transaction/commit";
        String query="";
        if(typeRelation==0){
            query = "match p= (a:Object)";
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
        query+= "  where 1=1 ";
        for (int i=1;i<=deep;i++){
            if(type!=2) {
                query += " and a<>a" + i;
                if(i>=2){
                    query += " and a"+(i-1)+"<>a" + i;
                }
            }

            query+= " and  r"+i+".startTime>='" +startTime+"' and r"+i+".startTime <='"+endTime+"' ";
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
                }else {
                    ip=ip.trim();
                    ip=ip.replace(".x","x");
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
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
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
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
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
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
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
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
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
                    if(!ip.startsWith(".*")){
                        ip =".*"+ip;
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
                    ip=ip.replace(".","\\.");
                    ip=ip.replace("x",".*");
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

        if(typeData!=null && !typeData.isEmpty()){
            for (int i=1;i<=deep;i++){
                queryDeepRelation(query,typeData,"r"+i);
            }
        }

        query += "  return p limit 500";
        //Payload request dbm.root.url//v1.0/dbm/management/mons/recognitions
        Map<String, java.lang.Object> bodyParam = new HashMap<>();
        Statement data = new Statement();
        data.setStatement(query);
        List<String> resultDataContents = new ArrayList<>();
        resultDataContents.add("graph");
        data.setResultDataContents(resultDataContents);
        List<Statement> statements = new ArrayList<>();
        statements.add(data);
        bodyParam.put("statements",statements);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.setBearerAuth("bmVvNGo6ZWxjb21AMTIz");
        headers.setBasicAuth(basicNeo4j);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, java.lang.Object>> requestEntity = new HttpEntity<>(bodyParam,headers);
        //Call rest api with url, method, payload, response entity
        HttpEntity<java.lang.Object> response = restTemplate.exchange(urlRequest, HttpMethod.POST, requestEntity, java.lang.Object.class);
        Map<String, ObjectToNode> mapBody = (Map<String, ObjectToNode>) response.getBody();
        List<ObjectToNode> errors = (List<ObjectToNode>) mapBody.get("errors");
        if(errors!=null && !errors.isEmpty()){
            return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(),"Có lỗi xảy ra",null));
        }
        Map<String,List<ObjectToNode>> results = new HashMap<>();
        results.put("results", (List<ObjectToNode>) mapBody.get("results"));
        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),response.getBody()));
    }

    private ResponseMessage topoVsat(String startTime,String endTime,String ip, Integer type, Integer typeRelation , Integer deep, List<Integer> typeData,List<Integer> dataSource,Boolean
            exactly,String ids, Integer page) throws ParseException {
        if(page==null){
            page=0;
        }
        ResponseTopo result = topoVsatService.getTopoTest1(startTime,endTime,ip,type,typeRelation,deep,typeData,dataSource,exactly,ids,page);
        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),result));
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
            }

        }
        query +=")";
        return query;
    }

    private void loadRedis(List<MmsiIp> mmsiIpList,List<ObjectUndefinedIp> objectUndefinedIps,List<AisInfo> aisInfoList,
                           List<Headquarters> headquartersList, List<ObjectUndefined> objectUndefineds){
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

    private static String stripWildcards(String title) {
        String result = title;
        if (result.startsWith("*")) {
            result = result.substring(1);
        }
        if (result.endsWith("*")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }


    public ResponseMessage addNodeInfo(Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String note = (String) bodyParam.get("note");
        String ids = (String) bodyParam.get("nodeId");
        NodeInfo topology = new NodeInfo();
        topology.setId(UUID.randomUUID().toString());
        topology.setNote(note);
        topology.setCreatedBy(userInfo.getUuid());
        topology.setCreatedDate(new Date());
        topology.setNodeId(ids);
        nodeTopologyService.saveTopology(topology);




        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),topology));
    }
    public ResponseMessage updateTopologyNode(Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String note = (String) bodyParam.get("note");
        String id = (String) bodyParam.get("id");
        String ids = (String) bodyParam.get("nodeId");
        NodeInfo topology = new NodeInfo();
        topology.setId(id);
        topology.setNote(note);
        topology.setCreatedBy(userInfo.getUuid());
        topology.setCreatedDate(new Date());
        topology.setNodeId(ids);
        if(nodeTopologyService.findById(ids)!=null) {
            nodeTopologyService.saveTopology(topology);
        }else {
            return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(),"Không tìm thấy topology",null));
        }

        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),topology));
    }
    public ResponseMessage getTopologyNode( Map<String, String> headerMap,String param) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        Map<String, String> params = StringUtil.getUrlParamValues(param);
        String nodeId = params.get("nodeId");
        NodeInfo nodeInfo = nodeTopologyService.findById(nodeId);

        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),nodeInfo));
    }

    public ResponseMessage deleteTopoNode( Map<String, java.lang.Object> bodyParam, Map<String, String> headerMap) throws IOException, ParseException {
        AuthorizationResponseDTO userInfo = null;
        userInfo = authenToken(headerMap);
        if (userInfo == null)
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Bạn chưa đăng nhập"));

        List<String> ids= (List<String>) bodyParam.get("ids");
        for (String id: ids
        ) {
            if(!nodeTopologyService.findByIdCheck(id).isPresent()){
                return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(),"Không tìm thấy topology " +id,null));
            }
        }

        nodeTopologyService.delete(ids);

        return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(),
                "Xóa thành công"));
    }
}
