package elcom.com.neo4j.service.impl;

import com.elcom.metacen.utils.DateUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elcom.com.neo4j.clickhouse.model.VsatMedia;
import elcom.com.neo4j.clickhouse.service.MetaCenMediaService;
import elcom.com.neo4j.dto.MappingVsatFilterDTO;
import elcom.com.neo4j.dto.MappingVsatResponseDTO;
import elcom.com.neo4j.dto.ObjectMetacenDTO;
import elcom.com.neo4j.dto.RelationshipLstDTO;
import elcom.com.neo4j.repositoryPostgre.CustomerRepository;
import elcom.com.neo4j.service.LinkObjectService;
import elcom.com.neo4j.service.MappingVsatMetacenService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class LinkObjectServiceImpl implements LinkObjectService {
    private final ObjectRepository movieRepository;

    private final Neo4jClient neo4jClient;

    private final Driver driver;

    private final DatabaseSelectionProvider databaseSelectionProvider;


    @Autowired
    private MetaCenMediaService metaCenMediaService;

    @Autowired
    private MappingVsatMetacenService mappingVsatMetacenService;

    LinkObjectServiceImpl(ObjectRepository movieRepository,
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

    @Override
    public void addLinkObject(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        List<ObjectMetacenDTO> listData = null;
        if (bodyParam.get("listObject") != null) {
            listData = mapper.convertValue(
                    bodyParam.get("listObject"),
                    new TypeReference<List<ObjectMetacenDTO>>() {
                    });
        }
        String vsatMediaUuidKey = (String) bodyParam.getOrDefault("vsatMediaDataAnalyzedUuidKey", "");
        String delete = " use metacenv1.metacenlink match (a)-[r:MENTION]->(b) where r.mediaUuid = '"+vsatMediaUuidKey+"' delete r ";
        this.neo4jClient
                .query(delete)
                .in(database())
                .run()
                .counters()
                .propertiesSet();
        List<VsatMedia> media =metaCenMediaService.mediaUuid(vsatMediaUuidKey);
        if(media!=null&&!media.isEmpty()){

            processLinkObject(media.get(0),listData,vsatMediaUuidKey);
        }



    }

    @Override
    public void updateNode(Map<String, Object> body) {
        ObjectMapper mapper = new ObjectMapper();
        String objectId = (String) body.get("objectId");
        String objectUuid = (String) body.get("objectUuid");
        String objectType = (String) body.get("objectType");
        String objectName = (String) body.get("objectName");
        String mmsi = (String) body.get("mmsi");

        String update = " use metacenv1.metacenlink match (a:"+objectType+") where a.objectId = '"+objectId+"' set a=" +queryMerge(objectId,objectName,objectUuid);
        this.neo4jClient
                .query(update)
                .in(database())
                .run()
                .counters()
                .propertiesSet();
    }

    @Override
    public void createLinkContainsObject(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        List<RelationshipLstDTO> listData = null;
        if (bodyParam.get("relationshipLst") != null) {
            listData = mapper.convertValue(
                    bodyParam.get("relationshipLst"),
                    new TypeReference<List<RelationshipLstDTO>>() {
                    });
        }
        ObjectMetacenDTO object = mapper.convertValue(bodyParam.get("object"),ObjectMetacenDTO.class);
        String delete = " use metacenv1.metacenlink match (a)-[r:BELONG]-(b) where a.objectUuid = '"+object.getObjectUuid()+"' delete r ";
        this.neo4jClient
                .query(delete)
                .in(database())
                .run()
                .counters()
                .propertiesSet();
        if(listData!=null&&!listData.isEmpty()){
            processLinkObjectContains(object,listData);
        }

    }

    @Override
    public void deleteNode(Map<String, Object> body) {
        ObjectMapper mapper = new ObjectMapper();
        String uuid = (String) body.get("objectUuid");
        String delete = " use metacenv1.metacenlink match (a)-[r:BELONG]-(b) where a.objectUuid = '"+uuid+"' delete r ";
        this.neo4jClient
                .query(delete)
                .in(database())
                .run()
                .counters()
                .propertiesSet();
    }

    private void processLinkObject( VsatMedia media, List<ObjectMetacenDTO> listData , String mediaUuid){
        List<Integer> dataSource = new ArrayList<>();
        dataSource.add(media.getDataSource());
        MappingVsatFilterDTO mappingVsatFilterDTO = new MappingVsatFilterDTO();
        mappingVsatFilterDTO.setPage(0);
        mappingVsatFilterDTO.setSize(2);
        mappingVsatFilterDTO.setVsatDataSourceIds(dataSource);
        mappingVsatFilterDTO.setVsatIpAddress(media.getSourceIp());

        Page<MappingVsatResponseDTO>  start =mappingVsatMetacenService.findListMappingVsat(mappingVsatFilterDTO);
        MappingVsatResponseDTO source = null;
        if(start!=null&&start.getContent()!=null&&!start.getContent().isEmpty()){
            source = start.getContent().get(0);
        }
        mappingVsatFilterDTO.setVsatIpAddress(media.getDestIp());
        Page<MappingVsatResponseDTO>  end =mappingVsatMetacenService.findListMappingVsat(mappingVsatFilterDTO);
        MappingVsatResponseDTO dest = null;
        if(end!=null&&end.getContent()!=null&&!end.getContent().isEmpty()){
            dest = end.getContent().get(0);
        }
        if(dest!=null&& source!=null&&listData!=null&&!listData.isEmpty()){
            StringBuilder query = new StringBuilder();
            String mergeSrc = queryMerge("src",source);
            String mergeDest = queryMerge("dest",dest);
            query.append(mergeSrc).append(mergeDest);
            StringBuilder create = new StringBuilder();
            String objectName = "";
            String objectUuid = "";
            String objectId = "";
            String objectType ="";
            String mmsi = String.valueOf(media.getSourceId())+"#"+String.valueOf(media.getDestId());
            for (ObjectMetacenDTO data :listData
                 ) {
                if(data.getObjectName().isEmpty()) {
                    objectName += " " + "#";
                }else {
                    objectName += data.getObjectName() + "#";
                }
                objectId+= data.getObjectId()+"#";
                objectUuid+= data.getObjectUuid()+"#";
                objectType+= data.getObjectType()+"#";
                if(data.getObjectMmsi()!=null&&!data.getObjectMmsi().isEmpty()){
                    mmsi+=data.getObjectMmsi()+"#";
                }
            }
            objectName=objectName.substring(0,objectName.length()-1);
            objectUuid=objectUuid.substring(0,objectUuid.length()-1);
            objectId=objectId.substring(0,objectId.length()-1);
            objectType=objectType.substring(0,objectType.length()-1);
            if(mmsi.endsWith("#")){
                mmsi=mmsi.substring(0,mmsi.length()-1);
            }
            create.append(queryRelation("src",source.getObjectUuid(),"dest",dest.getObjectUuid(),
                    objectUuid,objectId,objectName,"mention",media,mediaUuid,mmsi,objectType));
            String relation= create.toString();
            String cypherNeo4j ="use  metacenv1.metacenlink "+ query.toString() +" CREATE "+ relation;
            createRelationNeo4j(cypherNeo4j);

        }
    }

    private void processLinkObjectContains(ObjectMetacenDTO object, List<RelationshipLstDTO> listData ){
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if(listData!=null&&!listData.isEmpty()){
            StringBuilder query = new StringBuilder();
            String object1 = queryMerge("src",object);
            StringBuilder  create = new StringBuilder();
            query.append(object1);
            for (RelationshipLstDTO data :listData
            ) {
                String object2 = "dest"+UUID.randomUUID().toString();
                object2=object2.replaceAll("-","");
                ObjectMetacenDTO destObject = new ObjectMetacenDTO();
                destObject.setObjectUuid(data.getDestObjectId());
                destObject.setObjectId(data.getId());
                destObject.setObjectType(data.getDestObjectType());
                destObject.setObjectName(data.getName());
                String mergeDest = queryMerge(object2,destObject);
                query.append(mergeDest);
                String relationName = UUID.randomUUID().toString();
                relationName=relationName.replaceAll("-","");
                relationName="r"+relationName;
                String startTime ="";
                String endTime ="";
                if(data.getFromTime()!=null&&!data.getFromTime().isEmpty()){
                    try {
                        startTime = dff.format(df1.parse(data.getFromTime()));
                    }catch (Exception ex){
                        startTime="";
                    }
                }
                if(data.getToTime()!=null&&!data.getToTime().isEmpty()){
                    try {
                        endTime = dff.format(df1.parse(data.getToTime()));
                    }catch (Exception ex){
                        endTime="";
                    }
                }
                if(data.getRelationshipType()==0){
                    create.append(queryRelationBELONG(object2,"src",relationName, startTime,endTime,destObject.getObjectUuid(),object.getObjectUuid(),data.getNote()));
                }else {
                    create.append(queryRelationBELONG("src",object2,relationName,startTime,endTime,object.getObjectUuid(),destObject.getObjectUuid(),data.getNote()));
                }
            }
            String relation= create.toString();
            relation = relation.substring(0,relation.length()-1);
            String cypherNeo4j ="use  metacenv1.metacenlink "+ query.toString() +" CREATE "+ relation;
            createRelationNeo4j(cypherNeo4j);
        }
    }

    public void createRelationNeo4j(String query) {
        this.neo4jClient
                .query(query)
                .in(database())
                .run()
                .counters()
                .propertiesSet();
    }

    private String queryRelationBELONG(String srcName,String destName, String relationName, String startTime, String endTime, String mmsiStart, String mmsiEnd,String note){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  "("+srcName+") - ["+relationName+":BELONG{startTime:'"+startTime+"',endTime:'"+endTime+"',"+
                " uuidStart:'"+mmsiStart+"',uuidEnd:'"+mmsiEnd+"',note:'"+note+"'}] ->("+destName+"),";
    }

    private String  queryRelation(String srcName,String mmsiStart,String destName,String mmsiEnd,String objectUuid,String objectId,String objectName, String relationName,VsatMedia media,String mediaUuid, String mmsi,String objectType){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ip = media.getSourceIp()+"-"+media.getDestIp();
        return  "("+srcName+") - ["+relationName+":MENTION{eventTime:'"+df.format(media.getProcessTime())+"',mediaUuid:'"+mediaUuid+"',"+
                " uuidStart:'"+mmsiStart+"',uuidEnd:'"+mmsiEnd+"',objectUuid:'"+objectUuid+"',objectId:'"+objectId+"',objectName:'"+objectName+"',objectType:'"+objectType+"',mediaType:'"+media.getMediaTypeName()+"',mmsi:'"+mmsi+"',dataSource:"+media.getDataSource()+",ip:'"+ip+"'}] ->("+destName+")";
    }

    private String queryMerge(String idName, ObjectMetacenDTO data){
       return  " MERGE("+idName+":"+data.getObjectType()+"{objectName:'"+data.getObjectName()+"',objectUuid:'"+data.getObjectUuid()+"',objectId:'"+
                data.getObjectId()+"'}) ";
    }

    private String queryMerge(String idName, MappingVsatResponseDTO data){
        return  " MERGE("+idName+":"+data.getObjectType()+"{objectName:'"+data.getObjectName()+"',objectUuid:'"+data.getObjectUuid()+"',objectId:'"+
                data.getObjectId()+"'}) ";
    }

    private String queryMerge(String objectId,String objectName, String objectUuid){
        return  "{objectName:'"+objectName+"',objectUuid:'"+objectUuid+"',objectId:'"+
                objectId+"'} ";
    }

    @Override
    public void createIndex(String query) {
        query = " use metacenv1.metacenlink "+query;
                this.neo4jClient
                .query(query)
                .in(database())
                .run()
                .counters()
                .propertiesSet();
    }
}
