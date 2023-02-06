package elcom.com.neo4j.clickhouse.repository;

import com.ctc.wstx.shaded.msv_core.datatype.xsd.DateTimeType;
import elcom.com.neo4j.clickhouse.model.VsatMedia;
import elcom.com.neo4j.dto.VsatMediaPagingDTO;
import elcom.com.neo4j.utils.DateTimeFormat;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Repository
public class VsatMediaCHRepository extends BaseRepositoryCH {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaCHRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public VsatMediaCHRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }


    public List<VsatMedia> searchPlateGroupBy(String startDate, String endDate) {
        VsatMediaPagingDTO recognitionPlatePagingDTO = new VsatMediaPagingDTO();
        List<VsatMedia> lstMediaRaw = new ArrayList<>();
        Session session = null;
        long count = 0;
        try {
            session = this.clickHouseSessionFactory.openSession();
            String sql = "SELECT uuidKey,eventTime, "
                    + "procTime , mediaTypeId, "
                    + "mediaTypeName,sourceIp,srcId,"
                    + "srcObjId , srcIsUfo, "
                    + "srcIsHq,srcCountryId,srcTypeId,"
                    + "srcName , srcExtra, "
                    + "sourcePort,sourcePhone,destIp,"
                    + "destId , destObjId, "
                    + "destIsUfo,destIsHq,destCountryId,"
                    + "destTypeId , destName, "
                    + "destExtra,destPort,destPhone,"
                    + "filePath,fileSize,fileName,"
                    + "fileType , dataSource, "
                    + "dataSourceName,direction,relId,"
                    + "partName,ingestTime"
                    + " FROM vsat_media WHERE 1=1 ";
            sql += "AND ingestTime >='" + startDate +"' AND ingestTime <='" + endDate + "'";

//            if (startDate != null && endDate != null) {
//                sql += " AND create_date >= :start_time AND create_date < :end_time";
//            }


            NativeQuery query = session.createNativeQuery(sql);
//            if (startDate != null && endDate != null) {
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                query.setParameter("start_time",df.format(startDate));
//                query.setParameter("end_time",df.format(endDate));
//            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .addScalar("procTime", TimestampType.INSTANCE)
                    .addScalar("mediaTypeId", IntegerType.INSTANCE)
                    .addScalar("mediaTypeName", StringType.INSTANCE)
                    .addScalar("sourceIp",StringType.INSTANCE)
                    .addScalar("srcId",IntegerType.INSTANCE)
                    .addScalar("srcObjId", StringType.INSTANCE)
                    .addScalar("srcIsUfo", IntegerType.INSTANCE)
                    .addScalar("srcIsHq", IntegerType.INSTANCE)
                    .addScalar("srcCountryId", IntegerType.INSTANCE)
                    .addScalar("srcTypeId",IntegerType.INSTANCE)
                    .addScalar("srcName",StringType.INSTANCE)
                    .addScalar("srcExtra", StringType.INSTANCE)
                    .addScalar("sourcePort", IntegerType.INSTANCE)
                    .addScalar("sourcePhone", StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("destId",IntegerType.INSTANCE)
                    .addScalar("destObjId",StringType.INSTANCE)
                    .addScalar("destIsUfo", IntegerType.INSTANCE)
                    .addScalar("destIsHq", IntegerType.INSTANCE)
                    .addScalar("destCountryId", IntegerType.INSTANCE)
                    .addScalar("destTypeId", IntegerType.INSTANCE)
                    .addScalar("destName",StringType.INSTANCE)
                    .addScalar("destExtra",StringType.INSTANCE)
                    .addScalar("destPort", IntegerType.INSTANCE)
                    .addScalar("destPhone", StringType.INSTANCE)
                    .addScalar("filePath", StringType.INSTANCE)
                    .addScalar("fileSize", LongType.INSTANCE)
                    .addScalar("fileName",StringType.INSTANCE)
                    .addScalar("fileType",StringType.INSTANCE)
                    .addScalar("dataSource", IntegerType.INSTANCE)
                    .addScalar("dataSourceName", StringType.INSTANCE)
                    .addScalar("direction", IntegerType.INSTANCE)
                    .addScalar("relId", StringType.INSTANCE)
                    .addScalar("partName", LongType.INSTANCE)
                    .addScalar("ingestTime",TimestampType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMedia.class));

            lstMediaRaw = query.getResultList();
            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
                return lstMediaRaw;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }  finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }

    public List<VsatMedia> searchPlateGroupBy2(String startDate, String endDate) {
        VsatMediaPagingDTO recognitionPlatePagingDTO = new VsatMediaPagingDTO();
        List<VsatMedia> lstMediaRaw = new ArrayList<>();
        Session session = null;
        long count = 0;
        try {
            session = this.clickHouseSessionFactory.openSession();
            DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(dff.parse(startDate));
            cal.add(Calendar.MONTH, -1);
            String eventTime = dff.format(cal.getTime());
            String sql = "SELECT  "
                    + "mediaTypeName,sourceId,sourceIp,"
                    + "sourceName,destIp,destName,destId,"
                    + "fileSize,"
                    + " dataSourceId as dataSource,"
                    + " toStartOfHour(eventTime) as eventTime"
                    + " FROM vsat_media WHERE 1=1 ";

            sql += " AND eventTime >='"+eventTime+"' AND eventTime <='" + endDate + "' " ;
            sql += " AND ingestTime >='" + startDate +"' AND ingestTime <'" + endDate + "' " ;

//            if (startDate != null && endDate != null) {
//                sql += " AND create_date >= :start_time AND create_date < :end_time";
//            }


            NativeQuery query = session.createNativeQuery(sql);
//            if (startDate != null && endDate != null) {
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                query.setParameter("start_time",df.format(startDate));
//                query.setParameter("end_time",df.format(endDate));
//            }

            query.addScalar("mediaTypeName", StringType.INSTANCE)
                    .addScalar("sourceIp",StringType.INSTANCE)
                    .addScalar("sourceId",BigIntegerType.INSTANCE)
                    .addScalar("sourceName",StringType.INSTANCE)
                    .addScalar("destId", BigIntegerType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("destName", StringType.INSTANCE)
                    .addScalar("fileSize", LongType.INSTANCE)
                    .addScalar("dataSource", IntegerType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMedia.class));

            lstMediaRaw = query.getResultList();
            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
                return lstMediaRaw;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }  finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }

    public List<VsatMedia> searchPlateGroupBy2(String startDate, String endDate,String ips) {
        VsatMediaPagingDTO recognitionPlatePagingDTO = new VsatMediaPagingDTO();
        List<VsatMedia> lstMediaRaw = new ArrayList<>();
        Session session = null;
        long count = 0;
        try {
            session = this.clickHouseSessionFactory.openSession();
            String sql = "SELECT  "
                    + "mediaTypeName,sourceIp,"
                    + "destIp,"
                    + "fileSize,"
                    + " dataSource"
                    + " FROM vsat_media WHERE 1=1 ";
            sql += "AND ingestTime >='" + startDate +"' AND ingestTime <'" + endDate + "' " ;
            sql += " and (sourceIp in ("+ips+") or destIp in ( "+ips+"))";

//            if (startDate != null && endDate != null) {
//                sql += " AND create_date >= :start_time AND create_date < :end_time";
//            }


            NativeQuery query = session.createNativeQuery(sql);
//            if (startDate != null && endDate != null) {
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                query.setParameter("start_time",df.format(startDate));
//                query.setParameter("end_time",df.format(endDate));
//            }

            query.addScalar("mediaTypeName", StringType.INSTANCE)
                    .addScalar("sourceIp",StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("fileSize", LongType.INSTANCE)
                    .addScalar("dataSource", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMedia.class));

            lstMediaRaw = query.getResultList();
            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
                return lstMediaRaw;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }  finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }

    public List<elcom.com.neo4j.clickhouse.model.Ais> searchAis(String startDate, String endDate) {
        VsatMediaPagingDTO recognitionPlatePagingDTO = new VsatMediaPagingDTO();
        List<elcom.com.neo4j.clickhouse.model.Ais> lstMediaRaw = new ArrayList<>();
        DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Session session = null;
        long count = 0;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dff.parse(startDate));
            cal.add(Calendar.MONTH, -1);
            String eventTime = dff.format(cal.getTime());
            session = this.clickHouseSessionFactory.openSession();
            String sql = "SELECT  "
                    + "sourceIp,v.mmsi as sourceId, v.name as sourceName,"
                    + "destIp,"
                    + " dataSourceId as dataSource ,toStartOfHour(eventTime) as eventTime ,count (*) as count"
                    + " FROM vsat_ais v WHERE sourceIp<>'' and sourceIp is not null and destIp<>'' and destIp is not null ";

            sql += " AND eventTime >='"+eventTime+"' AND eventTime <='" + endDate + "' " ;
            sql += "AND ingestTime >='" + startDate +"' AND ingestTime <'" + endDate + "' " ;
            sql += "GROUP by eventTime,sourceId,sourceIp,sourceName,destIp, dataSource";
            NativeQuery query = session.createNativeQuery(sql);
            query.addScalar("sourceId",BigIntegerType.INSTANCE)
                    .addScalar("sourceIp",StringType.INSTANCE)
                    .addScalar("sourceName", StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("dataSource", IntegerType.INSTANCE)
                    .addScalar("count",IntegerType.INSTANCE)
                    .addScalar("eventTime",TimestampType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(elcom.com.neo4j.clickhouse.model.Ais.class));

            lstMediaRaw = query.getResultList();
            LOGGER.info("Query ais {}" ,sql);
//            LOGGER.info("result : {}",lstMediaRaw.toString());
            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
                return lstMediaRaw;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }  finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }

    public List<elcom.com.neo4j.clickhouse.model.Ais> searchAis(String startDate, String endDate, String ips) {
        VsatMediaPagingDTO recognitionPlatePagingDTO = new VsatMediaPagingDTO();
        List<elcom.com.neo4j.clickhouse.model.Ais> lstMediaRaw = new ArrayList<>();
        Session session = null;
        long count = 0;
        try {
            session = this.clickHouseSessionFactory.openSession();
            String sql = "SELECT  "
                    + "sourceIp,"
                    + "destIp,"
                    + " dataSource ,count (*) as count"
                    + " FROM vsat_ais WHERE sourceIp<>'' and sourceIp is not null and destIp<>'' and destIp is not null ";
            sql += "AND ingestTime >='" + startDate +"' AND ingestTime <'" + endDate + "' " ;
            sql += " and (sourceIp in ("+ips+") or destIp in ( "+ips+"))";
            sql += "GROUP by sourceIp,destIp, dataSource";
            NativeQuery query = session.createNativeQuery(sql);
            query.addScalar("sourceIp",StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("dataSource", IntegerType.INSTANCE)
                    .addScalar("count",IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(elcom.com.neo4j.clickhouse.model.Ais.class));

            lstMediaRaw = query.getResultList();
            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
                return lstMediaRaw;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }  finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }

    public List<Object[]> getNodeImportant(String sql) {
        Session session = null;
        long count = 0;
        List<Object[]> lstMediaRaw = new ArrayList<>();
        try {
            session = this.clickHouseSessionFactory.openSession();
            NativeQuery query = session.createNativeQuery(sql);
            lstMediaRaw = query.getResultList();
            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
                return lstMediaRaw;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }  finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }

//    public List<Ais> searchAis(String objId, String startDate, String endDate) {
//        VsatMediaPagingDTO recognitionPlatePagingDTO = new VsatMediaPagingDTO();
//        List<Ais> lstMediaRaw = new ArrayList<>();
//        Session session = null;
//        long count = 0;
//        try {
//            session = this.clickHouseSessionFactory.openSession();
//            String sql = "SELECT * "
//                    + " FROM vsat_ais WHERE 1=1 ";
//            sql += " AND objId ='"+objId+"' ";
//            sql += " AND ingestTime >=" + startDate +" AND ingestTime <=" + endDate + "order by ingestTime desc limit 1 ";
//
////            if (startDate != null && endDate != null) {
////                sql += " AND create_date >= :start_time AND create_date < :end_time";
////            }
//
//
//            NativeQuery query = session.createNativeQuery(sql);
////            if (startDate != null && endDate != null) {
////                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////                query.setParameter("start_time",df.format(startDate));
////                query.setParameter("end_time",df.format(endDate));
////            }
//
//            query.addScalar("objId", StringType.INSTANCE)
//                    .addScalar("imo", StringType.INSTANCE)
//                    .addScalar("countryId", IntegerType.INSTANCE)
//                    .addScalar("typeId", IntegerType.INSTANCE)
//                    .addScalar("dimA", StringType.INSTANCE)
//                    .addScalar("dimB",StringType.INSTANCE)
//                    .addScalar("dimC",IntegerType.INSTANCE)
//                    .addScalar("dimD", StringType.INSTANCE)
//                    .addScalar("draugth", FloatType.INSTANCE)
//                    .addScalar("rot", FloatType.INSTANCE)
//                    .addScalar("sog", FloatType.INSTANCE)
//                    .addScalar("cog", FloatType.INSTANCE)
//                    .addScalar("longitude",FloatType.INSTANCE)
//                    .addScalar("latitude",FloatType.INSTANCE)
//                    .addScalar("navStatus", IntegerType.INSTANCE)
//                    .addScalar("trueHanding", IntegerType.INSTANCE)
//                    .addScalar("callSign", StringType.INSTANCE)
//                    .addScalar("eta", StringType.INSTANCE)
//                    .addScalar("destination",StringType.INSTANCE)
//                    .addScalar("procTime",TimestampType.INSTANCE)
//                    .addScalar("isMaster", IntegerType.INSTANCE)
//                    .addScalar("mmsiMaster", IntegerType.INSTANCE)
//                    .addScalar("sourceId", IntegerType.INSTANCE)
//                    .addScalar("sourcePort", IntegerType.INSTANCE)
//                    .addScalar("sourceIp",StringType.INSTANCE)
//                    .addScalar("destId",IntegerType.INSTANCE)
//                    .addScalar("destPort", IntegerType.INSTANCE)
//                    .addScalar("destIp", StringType.INSTANCE)
//                    .addScalar("direction", IntegerType.INSTANCE)
//                    .addScalar("isUfo", IntegerType.INSTANCE)
//                    .addScalar("dataSource",IntegerType.INSTANCE)
//                    .addScalar("dataSourceName",StringType.INSTANCE)
//                    .addScalar("areaIds", StringType.INSTANCE)
//                    .addScalar("groupIds", StringType.INSTANCE)
//                    .addScalar("count", IntegerType.INSTANCE)
//                    .addScalar("hasMedia", StringType.INSTANCE)
//                    .addScalar("partName", LongType.INSTANCE)
//                    .addScalar("timeKey",LongType.INSTANCE)
//                    .addScalar("ingestTime",TimestampType.INSTANCE)
//                    .addScalar("eventTime",TimestampType.INSTANCE)
//                    .setResultTransformer(Transformers.aliasToBean(Ais.class));
//
//            lstMediaRaw = query.getResultList();
//            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
//                return lstMediaRaw;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }  finally {
//            if (session != null && session.isOpen()) {
//                session.disconnect();
//                session.close();
//            }
//        }
//        return null;
//    }

//    public long saveCorrect( com.elcom.itscore.data.model.RecognitionPlate recognitionPlate) {
//        RecognitionPlatePagingDTO recognitionPlatePagingDTO = new RecognitionPlatePagingDTO();
//        List<RecognitionPlate> lstMediaRaw = new ArrayList<>();
//        Session session = null;
//        long count = 0;
//        try {
//            session = this.clickHouseSessionFactory.openSession();
//            String sql = " INSERT INTO recognition_plate ( id,start_time,source_id,source_name, "
//                    + "site, lane,bbox,image_url,longitude,latitude,plate,event,event_code,"
//                    + "event_name, object_type,object_name,speed_of_vehicle,create_by,create_date,modified_by,modified_action, "
//                    + "modified_date, is_newest,is_delete,reason,event_id_string,brand,color,identity_value,parent_id)"
//                    + " VALUES ( ?,?,?,?,"
//                    + "?, ?,?,?,"+ recognitionPlate.getLongitude() +","+ recognitionPlate.getLatitude()
//                    + ",?," + recognitionPlate.getEvent() + ",?,"
//                    + "?, ?,?," + recognitionPlate.getSpeedOfVehicle() +",?,?,?,?,"
//                    + "?,"+recognitionPlate.getIsNewest() + "," + recognitionPlate.getIsDelete().code() + ",?,?,?,?,?,?)";
//
//            sql += recognitionPlate.getId();
//            sql += ",'" + recognitionPlate + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recognitionPlate.getStartTime()) + "'" ;
//            sql += ",'" + recognitionPlate.getSourceId() + "'" ;
//            sql += ",'" + recognitionPlate.getSourceName() + "'" ;
//            if(recognitionPlate.getSite()!=null) {
//                sql += ",'" +  (new Gson()).toJson(recognitionPlate.getSite()) + "'" ;
//            }else {
//                sql += "," +  null ;
//            }
//            if(recognitionPlate.getLane()!=null) {
//                sql += ",'" +  (new Gson()).toJson(recognitionPlate.getLane()) + "'" ;
//            }else {
//                sql += "," +  null ;
//            }
//            if(recognitionPlate.getBbox()!=null) {
//                sql += ",'" +  (new Gson()).toJson(recognitionPlate.getBbox()) + "'" ;
//            }else {
//                sql += "," +  null ;
//            }
//            sql += ",'" + recognitionPlate.getImageUrl() + "'" ;
//            sql += "," + recognitionPlate.getLongitude() +","+ recognitionPlate.getLatitude();
//            sql += ",'" + recognitionPlate.getPlate() + "'" ;
//            sql += "," + recognitionPlate.getEvent() ;
//            sql += ",'" + recognitionPlate.getEventCode() + "'" ;
//            sql += ",'" + recognitionPlate.getEventName() + "'" ;
//            sql += ",'" + recognitionPlate.getObjectType() + "'" ;
//            sql += ",'" + recognitionPlate.getObjectName() + "'" ;
//            sql += "," + recognitionPlate.getSpeedOfVehicle() ;
//            sql += ",'" + recognitionPlate.getCreateBy() + "'" ;
//            sql += ",'" + recognitionPlate + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recognitionPlate.getCreateDate()) + "'" ;
//            sql += ",'" + recognitionPlate.getModifiedBy() + "'" ;
//            sql += ",'" + recognitionPlate.getModifiedAction() + "'" ;
//            sql += ",'" + recognitionPlate + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recognitionPlate.getCreateDate()) + "'" ;
//            sql += "," + recognitionPlate.getIsNewest() +","+ recognitionPlate.getIsDelete().code();
//            sql += ",'" + recognitionPlate.getReason() + "'" ;
//            sql += ",'" + recognitionPlate.getEventIdString() + "'" ;
//            sql += ",'" + recognitionPlate.getBrand() + "'" ;
//            sql += ",'" + recognitionPlate.getColor() + "'" ;
//            sql += ",'" + recognitionPlate.getIdentityValue() + "'" ;
//            sql += ",'" + recognitionPlate.getParentId() + "' )" ;
//            NativeQuery query = session.createNativeQuery(sql);
//            count = ((Number) query.getSingleResult()).intValue();
//
//        } catch (Exception ex) {
//            LOGGER.error("ex: ", ex);
//        } finally {
//            if (session != null && session.isOpen()) {
//                session.disconnect();
//                session.close();
//            }
//        }
//        return count;
//    }
//
//    public void updateCorrect( com.elcom.itscore.data.model.RecognitionPlate recognitionPlate) {
//        RecognitionPlatePagingDTO recognitionPlatePagingDTO = new RecognitionPlatePagingDTO();
//        List<RecognitionPlate> lstMediaRaw = new ArrayList<>();
//        Session session = null;
//        long count = 1;
//        try {
//            session = this.clickHouseSessionFactory.openSession();
//            String sql = " ALTER TABLE recognition_plate UPDATE " ;
//            sql += " source_id ='" + recognitionPlate.getSourceId() + "'" ;
//            sql += ",source_name = '" + recognitionPlate.getSourceName() + "'";
//            if(recognitionPlate.getSite()!=null) {
//                sql += ", site = '" +  (new Gson()).toJson(recognitionPlate.getSite()) + "'" ;
//            }else {
//                sql += ", site = " +  null ;
//            }
//            if(recognitionPlate.getLane()!=null) {
//                sql += ",lane = '" +  (new Gson()).toJson(recognitionPlate.getLane()) + "'" ;
//            }else {
//                sql += ", lane = " +  null ;
//            }
//            if(recognitionPlate.getBbox()!=null) {
//                sql += ", bbox = '" +  (new Gson()).toJson(recognitionPlate.getBbox()) + "'" ;
//            }else {
//                sql += ", bbox = " +  null ;
//            }
//            sql += ", image_url= '" + recognitionPlate.getImageUrl() + "'" ;
//            sql += ", longitude= " + recognitionPlate.getLongitude() +", latitude ="+ recognitionPlate.getLatitude();
//            sql += ", plate = '" + recognitionPlate.getPlate() + "'" ;
//            sql += ", event = " + recognitionPlate.getEvent() ;
//            sql += ", event_code = '" + recognitionPlate.getEventCode() + "'" ;
//            sql += ", event_name = '" + recognitionPlate.getEventName() + "'" ;
//            sql += ", object_type = '" + recognitionPlate.getObjectType() + "'" ;
//            sql += ", object_name = '" + recognitionPlate.getObjectName() + "'" ;
//            sql += ", speed_of_vehicle = " + recognitionPlate.getSpeedOfVehicle() ;
//            sql += ", create_by = '" + recognitionPlate.getCreateBy() + "'" ;
//            sql += ", create_date = '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recognitionPlate.getCreateDate()) + "'" ;
//            sql += ", modified_by = '" + recognitionPlate.getModifiedBy() + "'" ;
//            sql += ", modified_action = '" + recognitionPlate.getModifiedAction() + "'" ;
//            sql += ", modified_date = '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recognitionPlate.getCreateDate()) + "'" ;
//            sql += ", is_newest = " + recognitionPlate.getIsNewest() +", is_delete = "+ recognitionPlate.getIsDelete().code();
//            sql += ", reason = '" + recognitionPlate.getReason() + "'" ;
//            sql += ", event_id_string = '" + recognitionPlate.getEventIdString() + "'" ;
//            sql += ", brand = '" + recognitionPlate.getBrand() + "'" ;
//            sql += ", color = '" + recognitionPlate.getColor() + "'" ;
//            sql += ", identity_value = '" + recognitionPlate.getIdentityValue() + "'" ;
//            sql += " WHERE id = '" + recognitionPlate.getId()+ "'" + " AND  start_time= '" +
//                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recognitionPlate.getStartTime()) +"'";
//            NativeQuery query = session.createSQLQuery(sql);
//            query.isCallable();
//            query.getQueryReturns();
//            query.executeUpdate();
//
////            query.executeUpdate();
//
////            session.createQuery(sql);
////            query.executeUpdate();
//
//        } catch (Exception ex) {
//            LOGGER.error("ex: ", ex);
//        } finally {
//            if (session != null && session.isOpen()) {
//                session.disconnect();
//                session.close();
//            }
//        }
//    }

//    private List<RecognitionDTO> tranform(List<RecognitionPlate> recognitionPlateList){
//        List<RecognitionDTO> recognitionPlates = new ArrayList<>();
//        for (RecognitionPlate recognition: recognitionPlateList
//             ) {
//            RecognitionDTO recognitionPlate = tranform(recognition);
//            recognitionPlates.add(recognitionPlate);
//        }
//        return recognitionPlates;
//    }
//    private RecognitionDTO tranform(RecognitionPlate recognitionPlate){
//        RecognitionDTO result = new RecognitionDTO();
//        result.setSourceId(recognitionPlate.getSource_id());
//        result.setObjectType(recognitionPlate.getObject_type());
//        result.setBrand(recognitionPlate.getBrand());
//        result.setColor(recognitionPlate.getColor());
//        result.setPlateColor(recognitionPlate.getPlateColor());
//        result.setStartTime(recognitionPlate.getStart_time());
//
//        return result;
//
//
//    }


}
