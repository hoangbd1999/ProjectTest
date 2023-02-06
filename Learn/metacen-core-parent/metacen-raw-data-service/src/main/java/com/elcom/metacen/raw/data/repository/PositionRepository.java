package com.elcom.metacen.raw.data.repository;

import com.elcom.metacen.raw.data.model.dto.PositionOverallRequest;
import com.elcom.metacen.raw.data.model.dto.PositionResponseDTO;
import com.elcom.metacen.utils.StringUtil;
import java.text.DecimalFormat;
import java.util.List;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.BigIntegerType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;

@Repository
public class PositionRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public PositionRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }
    
    public List<PositionResponseDTO> findPositionOverallFromVsatSystem(PositionOverallRequest input) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();

            String condition = "";
            if( !StringUtil.isNullOrEmpty(input.getTerm()) )
                condition += " AND ( CAST(mmsi AS VARCHAR) = :termAbsoluteCondition OR ilike(replaceAll(name, '  ', ' '), :termRelativeCondition) " +
                             " OR ilike(sourceIp, :termRelativeCondition) OR ilike(destIp, :termRelativeCondition) ) ";
            
            if ( !StringUtil.isNullOrEmpty(input.getSourceIps()) ) {
                String sourceIps = input.getSourceIps().trim();
                String[] sourceIpLst = sourceIps.split(",");
                if (sourceIpLst.length > 1) {
                    int k = 0;
                    condition += " AND ( ";
                    for (String sourceIp : sourceIpLst) {
                        k++;
                        String newSourceIp = sourceIp.trim().toUpperCase();
                        if (newSourceIp.contains("X"))
                            condition += " ilike(sourceIp, :sourceIps_" + k + ")";
                        else
                            condition += " sourceIp = :sourceIps_" + k;

                        if (k < sourceIpLst.length)
                            condition += " OR ";
                    }
                    condition += " ) ";
                } else {
                    String newSourceIp = sourceIps.toUpperCase();
                    if (newSourceIp.contains("X"))
                        condition += " AND ilike(sourceIp, :sourceIps) ";
                    else
                        condition += " AND sourceIp = :sourceIps ";
                }
            }
            if ( !StringUtil.isNullOrEmpty(input.getDestIps()) ) {
                String destIps = input.getDestIps().trim();
                String[] destIpLst = destIps.split(",");
                if (destIpLst.length > 1) {
                    int k = 0;
                    condition += " AND ( ";
                    for (String destIp : destIpLst) {
                        k++;
                        String newDestIp = destIp.trim().toUpperCase();
                        if (newDestIp.contains("X"))
                            condition += " ilike(destIp, :destIps_" + k + ")";
                        else
                            condition += " destIp = :destIps_" + k;

                        if (k < destIpLst.length)
                            condition += " OR ";
                    }
                    condition += " ) ";
                } else {
                    String newDestIp = destIps.toUpperCase();
                    if (newDestIp.contains("X"))
                        condition += " AND ilike(destIp, :destIps) ";
                    else
                        condition += " AND destIp = :destIps ";
                }
            }
            if ( input.getDataSourceIds() != null && !input.getDataSourceIds().isEmpty() )
                condition += " AND dataSourceId IN :dataSources ";
            
            if ( !StringUtil.isNullOrEmpty(input.getMmsiVsat()) )
                condition += " AND CAST(mmsi AS VARCHAR) = :mmsi ";
            
            if ( !StringUtil.isNullOrEmpty(input.getImoVsat()) )
                condition += " AND imo = :imo ";
            
            if ( input.getCountryIds() != null && !input.getCountryIds().isEmpty() )
                condition += " AND countryId IN :countryIds ";
            
            if ( input.getDataVendorsVsat()!= null && !input.getDataVendorsVsat().isEmpty() )
                condition += " AND dataVendor IN :dataVendors ";

            String tileNumberCondition = "";
            List<String> tileCoordinates = input.getTileCoordinates();
            if( tileCoordinates != null && !tileCoordinates.isEmpty() ) {
                DecimalFormat df = new DecimalFormat("#.000000");
                for( String tileCoordinate : tileCoordinates ) {
                    try {
                        if( tileCoordinate == null || !tileCoordinate.contains("###") )
                            continue;

                        String[] arrTmp = tileCoordinate.trim().split("###");

                        if( arrTmp[0] == null || !arrTmp[0].contains(",") || arrTmp[1] == null || !arrTmp[1].contains(",") )
                            continue;

                        String[] originCoordinate = arrTmp[0].trim().split(",");
                        String[] cornerCoordinate = arrTmp[1].trim().split(",");

                        float longitudeOrigin = Float.parseFloat(originCoordinate[0].trim());
                        float latitudeOrigin = Float.parseFloat(originCoordinate[1].trim());
                        float longitudeCorner = Float.parseFloat(cornerCoordinate[0].trim());
                        float latitudeCorner = Float.parseFloat(cornerCoordinate[1].trim());

                        tileNumberCondition += " OR ( latitude BETWEEN " + StringUtil.normalizeCoordinatesValue(df.format(latitudeOrigin)) + " AND " + StringUtil.normalizeCoordinatesValue(df.format(latitudeCorner)) +
                                                " AND longitude BETWEEN " + StringUtil.normalizeCoordinatesValue(df.format(longitudeOrigin)) + " AND " + StringUtil.normalizeCoordinatesValue(df.format(longitudeCorner)) + " ) ";
                    } catch (Exception e) {
                        LOGGER.error("ex_0: ", e);
                    }
                }
            }
            if( !"".equals(tileNumberCondition) ) {
                condition += " AND ( " + tileNumberCondition.substring(3) + " ) ";
                LOGGER.info("condition with tileNumbers -> [ {} ]", tileNumberCondition.substring(3));
            }
            
            String joinGroupCondition = "";
            String whereGroupCondition = "";
            if ( input.getGroupIds() != null && !input.getGroupIds().isEmpty() ) {
                joinGroupCondition  = " INNER JOIN dim_observed_object_group_mapping doogm FINAL ON doogm.objId = CAST(t1.mmsi AS VARCHAR) ";
                whereGroupCondition = " AND doogm.isDeleted = 0 AND doogm.groupId IN :groupIds ";
            }
            
            condition = " ( eventTime BETWEEN :fromTime AND :toTime ) " + condition;
            
            String sql = " SELECT t.uuidKey AS uuidKey, t.mmsi AS mmsi, t.draught AS draught, dimA AS dimA, dimB AS dimB, dimC AS dimC, dimD AS dimD, t.name AS name, t.callSign AS callSign, t.imo AS imo " +
                         " , t.rot AS rot, t.sog AS sog, t.cog AS cog, t.longitude AS longitude, t.latitude AS latitude, t.eventTime AS eventTime, t.ingestTime AS ingestTime, t.sourceIp AS sourceIp, t.destIp AS destIp " +
                         " , t.typeId AS typeId, t.countryId AS countryId, t.dataSourceName AS dataSourceName, t.dataVendor AS dataVendor, 'VSAT' AS sourceType " +
                         " FROM ( SELECT * FROM ( " +
                         "    SELECT uuidKey, row_number() OVER ( PARTITION BY mmsi ORDER BY eventTime DESC, ingestTime DESC ) AS rank " +
                         "    FROM vsat_ais " +
                         // joinGroupCondition +
                         "    WHERE eventTime BETWEEN :fromTime AND :toTime " +
                         // whereGroupCondition +
                         "    SETTINGS allow_experimental_window_functions = 1 " +
                         " ) WHERE rank = 1 ) r " +
                         " INNER JOIN ( " +
                         "   SELECT uuidKey, mmsi, draught, dimA, dimB, dimC, dimD, name, callSign, imo, rot, sog, cog, longitude, latitude, eventTime, ingestTime, sourceIp, destIp, typeId, countryId, dataSourceName, dataVendor " +
                         "   FROM vsat_ais t1 " +
                         joinGroupCondition +
                         "   WHERE " + condition +
                         whereGroupCondition +
                         " ) t ON r.uuidKey = t.uuidKey " +
                         // " ORDER BY t.ingestTime DESC " +
                         " LIMIT :limit OFFSET 0 ";
            
            NativeQuery query = session.createNativeQuery(sql)
                                       .setParameter("fromTime", input.getFromTime())
                                       .setParameter("toTime", input.getToTime())
                                       .setParameter("limit", input.getLimit());

            if ( !StringUtil.isNullOrEmpty(input.getSourceIps()) ) {
                String sourceIps = input.getSourceIps();
                String[] sourceIpLst = sourceIps.split(",");
                if (sourceIpLst.length > 1) {
                    int k = 0;
                    for (String sourceIp : sourceIpLst) {
                        k++;
                        String newSourceIp = sourceIp.trim().toUpperCase();
                        if (newSourceIp.contains("X")) {
                            newSourceIp = newSourceIp.replace("X", "%");
                        }
                        query.setParameter("sourceIps_" + k, newSourceIp);
                    }
                } else {
                    String newSourceIp = sourceIps.toUpperCase();
                    if (newSourceIp.contains("X")) {
                        newSourceIp = newSourceIp.replace("X", "%");
                    }
                    query.setParameter("sourceIps", newSourceIp);
                }
            }
            if ( !StringUtil.isNullOrEmpty(input.getDestIps()) ) {
                String destIps = input.getDestIps();
                String[] destIpLst = destIps.split(",");
                if (destIpLst.length > 1) {
                    int k = 0;
                    for (String destIp : destIpLst) {
                        k++;
                        String newDestIp = destIp.trim().toUpperCase();
                        if (newDestIp.contains("X")) {
                            newDestIp = newDestIp.replace("X", "%");
                        }
                        query.setParameter("destIps_" + k, newDestIp);
                    }
                } else {
                    String newDestIp = destIps.toUpperCase();
                    if (newDestIp.contains("X")) {
                        newDestIp = newDestIp.replace("X", "%");
                    }
                    query.setParameter("destIps", newDestIp);
                }
            }
            if ( input.getDataSourceIds() != null && !input.getDataSourceIds().isEmpty() )
                query.setParameterList("dataSources", input.getDataSourceIds());

            if ( !StringUtil.isNullOrEmpty(input.getTerm()) ) {
                query.setParameter("termAbsoluteCondition", input.getTerm());
                query.setParameter("termRelativeCondition", "%" + ( input.getTerm().equals("%") ? "\\%" : input.getTerm() ) + "%");
            }
            
            if ( !StringUtil.isNullOrEmpty(input.getMmsiVsat()) )
                query.setParameter("mmsi", input.getMmsiVsat());
            
            if ( !StringUtil.isNullOrEmpty(input.getImoVsat()) )
                query.setParameter("imo", input.getImoVsat());
                
            if ( input.getCountryIds() != null && !input.getCountryIds().isEmpty() )
                query.setParameterList("countryIds", input.getCountryIds());

            if ( input.getDataVendorsVsat()!= null && !input.getDataVendorsVsat().isEmpty() )
                query.setParameterList("dataVendors", input.getDataVendorsVsat());
            
            if ( input.getGroupIds() != null && !input.getGroupIds().isEmpty() ) 
                query.setParameterList("groupIds", input.getGroupIds());
            
            query.addScalar("uuidKey", StringType.INSTANCE)
                .addScalar("mmsi", BigIntegerType.INSTANCE)
                .addScalar("name", StringType.INSTANCE)
                .addScalar("callSign", StringType.INSTANCE)
                .addScalar("imo", StringType.INSTANCE)
                .addScalar("countryId", IntegerType.INSTANCE)
                .addScalar("typeId", IntegerType.INSTANCE)
                .addScalar("dimA", IntegerType.INSTANCE)
                .addScalar("dimB", IntegerType.INSTANCE)
                .addScalar("dimC", IntegerType.INSTANCE)
                .addScalar("dimD", IntegerType.INSTANCE)
                .addScalar("draught", FloatType.INSTANCE)
                .addScalar("rot", FloatType.INSTANCE)
                .addScalar("sog", FloatType.INSTANCE)
                .addScalar("cog", FloatType.INSTANCE)
                .addScalar("longitude", BigDecimalType.INSTANCE)
                .addScalar("latitude", BigDecimalType.INSTANCE)
                .addScalar("eventTime", TimestampType.INSTANCE)
                .addScalar("ingestTime", TimestampType.INSTANCE)
                .addScalar("sourceIp", StringType.INSTANCE)
                .addScalar("destIp", StringType.INSTANCE)
                .addScalar("dataSourceName", StringType.INSTANCE)
                .addScalar("dataVendor", StringType.INSTANCE)
                .addScalar("sourceType", StringType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(PositionResponseDTO.class));
            
            return query.getResultList();
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }
    
    public List<PositionResponseDTO> findPositionOverallFromAisSystem(PositionOverallRequest input) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();

            String condition = "";
            if( !StringUtil.isNullOrEmpty(input.getTerm()) )
                    condition += " AND ( CAST(mmsi AS VARCHAR) = :termAbsoluteCondition OR ilike(replaceAll(name, '  ', ' '), :termRelativeCondition) ) ";
            
            if ( !StringUtil.isNullOrEmpty(input.getMmsiAis()) )
                condition += " AND CAST(mmsi AS VARCHAR) = :mmsi ";
            
            if ( !StringUtil.isNullOrEmpty(input.getImoAis()) )
                condition += " AND imo = :imo ";
            
            if ( input.getCountryIds() != null && !input.getCountryIds().isEmpty() )
                condition += " AND countryId IN :countryIds ";
            
            if ( input.getDataVendorsAis()!= null && !input.getDataVendorsAis().isEmpty() )
                condition += " AND dataVendor IN :dataVendors ";

            String tileNumberCondition = "";
            List<String> tileCoordinates = input.getTileCoordinates();
            if( tileCoordinates != null && !tileCoordinates.isEmpty() ) {
                DecimalFormat df = new DecimalFormat("#.000000");
                for( String tileCoordinate : tileCoordinates ) {
                    
                    if( tileCoordinate == null || !tileCoordinate.contains("###") )
                        continue;
                    
                    String[] arrTmp = tileCoordinate.trim().split("###");
                    
                    if( arrTmp[0] == null || !arrTmp[0].contains(",") || arrTmp[1] == null || !arrTmp[1].contains(",") )
                        continue;
                    
                    String[] originCoordinate = arrTmp[0].trim().split(",");
                    String[] cornerCoordinate = arrTmp[1].trim().split(",");
                    
                    float longitudeOrigin = Float.parseFloat(originCoordinate[0].trim());
                    float latitudeOrigin = Float.parseFloat(originCoordinate[1].trim());
                    float longitudeCorner = Float.parseFloat(cornerCoordinate[0].trim());
                    float latitudeCorner = Float.parseFloat(cornerCoordinate[1].trim());
                    
                    tileNumberCondition += " OR ( latitude BETWEEN " + StringUtil.normalizeCoordinatesValue(df.format(latitudeOrigin)) + " AND " + StringUtil.normalizeCoordinatesValue(df.format(latitudeCorner)) +
                                            " AND longitude BETWEEN " + StringUtil.normalizeCoordinatesValue(df.format(longitudeOrigin)) + " AND " + StringUtil.normalizeCoordinatesValue(df.format(longitudeCorner)) + " ) ";
                }
            }
            if( !"".equals(tileNumberCondition) ) {
                condition += " AND ( " + tileNumberCondition.substring(3) + " ) ";
                LOGGER.info("condition with tileNumbers -> [ {} ]", tileNumberCondition.substring(3));
            }
            
            String joinGroupCondition = "";
            String whereGroupCondition = "";
            if ( input.getGroupIds() != null && !input.getGroupIds().isEmpty() ) {
                joinGroupCondition  = " INNER JOIN dim_observed_object_group_mapping doogm FINAL ON doogm.objId = CAST(t1.mmsi AS VARCHAR) ";
                whereGroupCondition = " AND doogm.isDeleted = 0 AND doogm.groupId IN :groupIds ";
            }
            
            condition = " ( eventTime BETWEEN :fromTime AND :toTime ) " + condition;
            
            String sql = " SELECT t.uuidKey AS uuidKey, t.mmsi AS mmsi, t.name AS name, t.imo AS imo " +
                         " , t.sog AS sog, t.cog AS cog, t.longitude AS longitude, t.latitude AS latitude, t.eventTime AS eventTime, t.ingestTime AS ingestTime " +
                         " , t.countryId AS countryId, t.dataVendor AS dataVendor, 'AIS' AS sourceType " +
                         " FROM ( SELECT * FROM ( " +
                         "    SELECT uuidKey, row_number() OVER ( PARTITION BY mmsi ORDER BY eventTime DESC, ingestTime DESC ) AS rank " +
                         "    FROM ais_data " +
                         // joinGroupCondition +
                         "    WHERE eventTime BETWEEN :fromTime AND :toTime " +
                         // whereGroupCondition +
                         "    SETTINGS allow_experimental_window_functions = 1 " +
                         " ) WHERE rank = 1 ) r " +
                         " INNER JOIN ( " +
                         "   SELECT uuidKey, mmsi, name, imo, sog, cog, longitude, latitude, eventTime, ingestTime, countryId, dataVendor " +
                         "   FROM ais_data t1 " +
                         joinGroupCondition +
                         "   WHERE " + condition +
                         whereGroupCondition +
                         " ) t ON r.uuidKey = t.uuidKey " +
                         // " ORDER BY t.ingestTime DESC " +
                         " LIMIT :limit OFFSET 0 ";
            
            NativeQuery query = session.createNativeQuery(sql)
                                       .setParameter("fromTime", input.getFromTime())
                                       .setParameter("toTime", input.getToTime())
                                       .setParameter("limit", input.getLimit());

            if ( !StringUtil.isNullOrEmpty(input.getTerm()) ) {
                query.setParameter("termAbsoluteCondition", input.getTerm());
                query.setParameter("termRelativeCondition", "%" + ( input.getTerm().equals("%") ? "\\%" : input.getTerm() ) + "%");
            }
            
            if ( !StringUtil.isNullOrEmpty(input.getMmsiAis()) )
                query.setParameter("mmsi", input.getMmsiAis());
            
            if ( !StringUtil.isNullOrEmpty(input.getImoAis()) )
                query.setParameter("imo", input.getImoAis());
            
            if ( input.getCountryIds() != null && !input.getCountryIds().isEmpty() )
                query.setParameterList("countryIds", input.getCountryIds());

            if ( input.getDataVendorsAis()!= null && !input.getDataVendorsAis().isEmpty() )
                query.setParameterList("dataVendors", input.getDataVendorsAis());
            
            if ( input.getGroupIds() != null && !input.getGroupIds().isEmpty() ) 
                query.setParameterList("groupIds", input.getGroupIds());
            
            query.addScalar("uuidKey", StringType.INSTANCE)
                .addScalar("mmsi", BigIntegerType.INSTANCE)
                .addScalar("name", StringType.INSTANCE)
                .addScalar("imo", StringType.INSTANCE)
                .addScalar("countryId", IntegerType.INSTANCE)
                .addScalar("sog", FloatType.INSTANCE)
                .addScalar("cog", FloatType.INSTANCE)
                .addScalar("longitude", BigDecimalType.INSTANCE)
                .addScalar("latitude", BigDecimalType.INSTANCE)
                .addScalar("eventTime", TimestampType.INSTANCE)
                .addScalar("ingestTime", TimestampType.INSTANCE)
                .addScalar("dataVendor", StringType.INSTANCE)
                .addScalar("sourceType", StringType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(PositionResponseDTO.class));
            
            return query.getResultList();
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }
}
