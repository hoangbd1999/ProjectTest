package com.elcom.metacen.raw.data.repository;

import com.elcom.metacen.raw.data.model.dto.PositionResponseDTO;
import com.elcom.metacen.raw.data.model.dto.VsatAisFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatAisDTO;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.utils.StringUtil;
import org.hibernate.Session;
import org.hibernate.type.*;
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

import java.util.List;

@Repository
public class VsatAisDataRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatAisDataRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public VsatAisDataRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }
    
    public MessageContent filterVsatAisRawData(VsatAisFilterDTO input) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();

            String condition = "";
            if ( !StringUtil.isNullOrEmpty(input.getTerm() ))
                condition += " AND ( ( toString(mmsi) like :term ) OR "
                            + " ilike( sourceIp, :term ) OR "
                            + " ilike( destIp, :term ) OR "
                            + " ilike( replaceAll(name, '  ', ' '), :term ) ) ";
            
            if (!StringUtil.isNullOrEmpty(input.getSourceIps())) {
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
            if (!StringUtil.isNullOrEmpty(input.getDestIps())) {
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
            
            if ( input.getMmsi() != null )
                condition += " AND (toString(mmsi) like :mmsi) ";
            
            if ( input.getDataSourceId() != null && !input.getDataSourceId().isEmpty() )
                condition += " AND dataSourceId IN :dataSources ";
            
            if ( input.getTypeId() != null && !input.getTypeId().isEmpty() )
                condition += " AND typeId IN :typeIds ";
            
            if ( input.getCountryId() != null && !input.getCountryId().isEmpty() )
                condition += " AND countryId IN :countryIds ";
            
            if ( input.getDataVendors() != null && !input.getDataVendors().isEmpty() )
                condition += " AND dataVendor IN :dataVendors ";

            if ( input.getProcessStatus() != null && !input.getProcessStatus().isEmpty() )
                condition += " AND processStatus IN :processStatus ";
            
            condition = " ( eventTime BETWEEN :fromTime AND :toTime ) " + condition;
            
            String sql = " SELECT t.mmsi AS mmsi, t.draught AS draught, dimA AS dimA, dimB AS dimB, dimC AS dimC, dimD AS dimD, t.name AS name, t.callSign AS callSign, t.imo AS imo " +
                         " , t.rot AS rot, t.sog AS sog, t.cog AS cog, t.longitude AS longitude, t.latitude AS latitude, t.eventTime AS eventTime, t.sourceIp AS sourceIp, t.destIp AS destIp " +
                         " , t.typeId AS typeId, t.countryId AS countryId, t.dataSourceName AS dataSourceName, t.dataVendor AS dataVendor " +
                         " FROM ( SELECT * FROM ( " +
                         "    SELECT uuidKey, mmsi, eventTime, ingestTime, row_number() OVER ( PARTITION BY mmsi ORDER BY eventTime DESC, ingestTime DESC ) AS rank " +
                         "    FROM vsat_ais " +
                         "    WHERE " + condition +
                         "    SETTINGS allow_experimental_window_functions = 1 " +
                         " ) WHERE rank = 1 ) r " +
                         " INNER JOIN ( " +
                         "   SELECT uuidKey, mmsi, draught, dimA, dimB, dimC, dimD, name " +
                         " , callSign, imo, rot, sog, cog, longitude, latitude, eventTime, ingestTime, sourceIp, destIp " +
                         " , typeId, countryId, dataSourceName, dataVendor " +
                         "   FROM vsat_ais " +
                         "   WHERE " + condition +
                         //" ) t ON ( r.mmsi = t.mmsi AND r.eventTime = t.eventTime AND r.ingestTime = t.ingestTime ) LIMIT :limit OFFSET 0";
                         " ) t ON r.uuidKey = t.uuidKey LIMIT :limit OFFSET 0";

            NativeQuery query = session.createNativeQuery(sql)
                                        .setParameter("fromTime", input.getFromTime().trim())
                                        .setParameter("toTime", input.getToTime().trim())
                                        .setParameter("limit", input.getLimit());

            if (!StringUtil.isNullOrEmpty(input.getSourceIps())) {
                String sourceIps = input.getSourceIps().trim();
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
            if (!StringUtil.isNullOrEmpty(input.getDestIps())) {
                String destIps = input.getDestIps().trim();
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

            if (!StringUtil.isNullOrEmpty(input.getTerm())) {
                String term = "%" + input.getTerm().trim() + "%";
                query.setParameter("term", term);
            }
            
            if ( input.getMmsi() != null )
                query.setParameter("mmsi", "%" + input.getMmsi() + "%");
            
            if ( input.getDataSourceId() != null && !input.getDataSourceId().isEmpty() )
                query.setParameterList("dataSources", input.getDataSourceId());
            
            if ( input.getTypeId() != null && !input.getTypeId().isEmpty() )
                query.setParameterList("typeIds", input.getTypeId());
            
            if ( input.getCountryId() != null && !input.getCountryId().isEmpty() )
                query.setParameterList("countryIds", input.getCountryId());

            if ( input.getDataVendors() != null && !input.getDataVendors().isEmpty() )
                query.setParameterList("dataVendors", input.getDataVendors());
            
            if ( input.getProcessStatus() != null && !input.getProcessStatus().isEmpty() )
                query.setParameterList("processStatus", input.getProcessStatus());
            
            query.addScalar("mmsi", BigIntegerType.INSTANCE)
//                .addScalar("uuidKey", StringType.INSTANCE)
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
//                .addScalar("eta", StringType.INSTANCE)
//                .addScalar("destination", StringType.INSTANCE)
//                .addScalar("mmsiMaster", BigIntegerType.INSTANCE)
                .addScalar("eventTime", TimestampType.INSTANCE)
//                .addScalar("ingestTime", TimestampType.INSTANCE)
//                .addScalar("sourcePort", LongType.INSTANCE)
//                .addScalar("destPort", LongType.INSTANCE)
                .addScalar("sourceIp", StringType.INSTANCE)
                .addScalar("destIp", StringType.INSTANCE)
//                .addScalar("direction", IntegerType.INSTANCE)
                .addScalar("dataSourceName", StringType.INSTANCE)
                .addScalar("dataVendor", StringType.INSTANCE)
//                .addScalar("processStatus", IntegerType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(VsatAisDTO.class));
            
            return new MessageContent(query.getResultList());
            
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }
    
    /*public MessageContent filterVsatAisOld(VsatAisFilterDTO input) {
        Session session = null;
        String schemaMeta = this.getSchemaMeta();
        try {
            session = this.clickHouseSessionFactory.openSession();
            String fromTime = input.getFromTime().trim();
            String toTime = input.getToTime().trim();
            
            Pageable pageable = PageRequest.of(0, ApplicationConfig.MAX_RECORDS_AIS_RETURN_NOT_UNIQUE);
            long offset = (long) pageable.getOffset();

            String condition = "";
            if (!StringUtil.isNullOrEmpty(input.getSourceIps())) {
                String sourceIps = input.getSourceIps().trim();
                String[] sourceIpLst = sourceIps.split(",");
                if (sourceIpLst.length > 1) {
                    int k = 0;
                    condition += " AND ( ";
                    for (String sourceIp : sourceIpLst) {
                        k++;
                        String newSourceIp = sourceIp.trim().toUpperCase();
                        if (newSourceIp.contains("X")) {
                            condition += " ilike(sourceIp, :sourceIps_" + k + ")";
                        } else {
                            condition += " sourceIp = :sourceIps_" + k;
                        }

                        if (k < sourceIpLst.length) {
                            condition += " OR ";
                        }
                    }
                    condition += " ) ";
                } else {
                    String newSourceIp = sourceIps.toUpperCase();
                    if (newSourceIp.contains("X")) {
                        condition += " AND ilike(sourceIp, :sourceIps) ";
                    } else {
                        condition += " AND sourceIp = :sourceIps ";
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(input.getDestIps())) {
                String destIps = input.getDestIps().trim();
                String[] destIpLst = destIps.split(",");
                if (destIpLst.length > 1) {
                    int k = 0;
                    condition += " AND ( ";
                    for (String destIp : destIpLst) {
                        k++;
                        String newDestIp = destIp.trim().toUpperCase();
                        if (newDestIp.contains("X")) {
                            condition += " ilike(destIp, :destIps_" + k + ")";
                        } else {
                            condition += " destIp = :destIps_" + k;
                        }

                        if (k < destIpLst.length) {
                            condition += " OR ";
                        }
                    }
                    condition += " ) ";
                } else {
                    String newDestIp = destIps.toUpperCase();
                    if (newDestIp.contains("X")) {
                        condition += " AND ilike(destIp, :destIps) ";
                    } else {
                        condition += " AND destIp = :destIps ";
                    }
                }
            }
            if (input.getMmsi() != null) {
                condition += " AND t.mmsi = :mmsi";
            }

            if (input.getProcessStatus() != null && !input.getProcessStatus().isEmpty()) {
                condition += " AND processStatus IN :processStatus";
            }

            if (input.getDataVendors() != null && !input.getDataVendors().isEmpty()) {
                condition += " AND dataVendor IN :dataVendors";
            }

            if (input.getDataSourceId() != null && !input.getDataSourceId().isEmpty()) {
                condition += " AND dataSource IN :dataSources ";
            }

            if (input.getTypeId() != null && !input.getTypeId().isEmpty()) {
                condition += " AND typeId IN :typeIds ";
            }

            if (input.getCountryId() != null && !input.getCountryId().isEmpty()) {
                condition += " AND countryId IN :countryIds ";
            }

            String term = input.getTerm().trim();
            if (term != null && !term.isEmpty()) {
                condition += " AND (toString(mmsi) = '" + term + "') OR ";
                condition += "ilike(sourceIp, '" + term + "') OR ";
                condition += "ilike(destIp, '" + term + "') ";
            }

            condition = " ( eventTime BETWEEN :fromTime AND :toTime ) " + condition;

            String sql = " SELECT t.uuidKey, t.mmsi, t.imo, t.name, t.callSign, "
                       + " t.draught, t.rot, t.sog, t.cog, t.longitude, t.latitude, t.dimA, t.dimB, t.dimC, t.dimD, "
                       + " t.eta, t.destination, t.mmsiMaster, t.sourcePort, t.sourceIp, t.destPort, t.destIp, "
                       + " t.direction, t.eventTime, t.ingestTime, t.typeId, t.countryId, t.processStatus, t.dataSourceId, t.dataSourceName, t.timeKey, t.dataVendor "
                       + " FROM vsat_ais t "
                       + " WHERE " + condition
                       + " ORDER BY t.eventTime DESC, t.ingestTime DESC "
                       + " LIMIT :offset, :limit ";

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime)
                    .setParameter("offset", offset)
                    .setParameter("limit", pageable.getPageSize());

            if (!StringUtil.isNullOrEmpty(input.getSourceIps())) {
                String sourceIps = input.getSourceIps().trim();
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
            if (!StringUtil.isNullOrEmpty(input.getDestIps())) {
                String destIps = input.getDestIps().trim();
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
            if (input.getMmsi() != null) {
                query.setParameter("mmsi", input.getMmsi());
            }

            if (input.getDataSourceId() != null && !input.getDataSourceId().isEmpty()) {
                query.setParameterList("dataSources", input.getDataSourceId());
            }

            if (input.getDataVendors() != null && !input.getDataVendors().isEmpty()) {
                query.setParameterList("dataVendors", input.getDataVendors());
            }

            if (input.getTypeId() != null && !input.getTypeId().isEmpty()) {
                query.setParameterList("typeIds", input.getTypeId());
            }

            if (input.getCountryId() != null && !input.getCountryId().isEmpty()) {
                query.setParameterList("countryIds", input.getCountryId());
            }

            if (input.getProcessStatus() != null && !input.getProcessStatus().isEmpty()) {
                query.setParameterList("processStatus", input.getProcessStatus());
            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("mmsi", BigIntegerType.INSTANCE)
                    .addScalar("draught", FloatType.INSTANCE)
                    .addScalar("imo", StringType.INSTANCE)
                    .addScalar("countryId", IntegerType.INSTANCE)
                    .addScalar("name", StringType.INSTANCE)
                    .addScalar("callSign", StringType.INSTANCE)
                    .addScalar("eta", StringType.INSTANCE)
                    .addScalar("destination", StringType.INSTANCE)
                    .addScalar("sourcePort", LongType.INSTANCE)
                    .addScalar("rot", FloatType.INSTANCE)
                    .addScalar("sog", FloatType.INSTANCE)
                    .addScalar("cog", FloatType.INSTANCE)
                    .addScalar("longitude", BigDecimalType.INSTANCE)
                    .addScalar("latitude", BigDecimalType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("mmsiMaster", BigIntegerType.INSTANCE)
                    .addScalar("sourceIp", StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("destPort", LongType.INSTANCE)
                    .addScalar("direction", IntegerType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("typeId", IntegerType.INSTANCE)
                    .addScalar("dimA", IntegerType.INSTANCE)
                    .addScalar("dimB", IntegerType.INSTANCE)
                    .addScalar("dimC", IntegerType.INSTANCE)
                    .addScalar("dimD", IntegerType.INSTANCE)
                    .addScalar("dataSourceId", LongType.INSTANCE)
                    .addScalar("dataSourceName", StringType.INSTANCE)
                    .addScalar("timeKey", BigIntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatAisDTO.class));
            return new MessageContent(query.getResultList());
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }*/

    public List<PositionResponseDTO> getVsatPositionsOfShip(Integer mmsi, String startTime, String endTime, Integer limit) {

        Session session = clickHouseSessionFactory.openSession();
        String sql = " SELECT va.uuidKey AS uuidKey, va.mmsi AS mmsi, va.draught AS draught, dimA AS dimA, dimB AS dimB, dimC AS dimC, dimD AS dimD, va.name AS name, va.callSign AS callSign, va.imo AS imo " +
                " , va.rot AS rot, va.sog AS sog, va.cog AS cog, va.longitude AS longitude, va.latitude AS latitude, va.eventTime AS eventTime, va.ingestTime AS ingestTime, va.sourceIp AS sourceIp, va.destIp AS destIp " +
                " , va.typeId AS typeId, va.countryId AS countryId, va.dataSourceName AS dataSourceName, va.dataVendor AS dataVendor, 'VSAT' AS sourceType " +
                " FROM vsat_ais va " +
                " WHERE va.mmsi = :mmsi AND " +
                " va.eventTime >= :startTime AND va.eventTime < :endTime " +
                " order by va.eventTime " +
                " limit :limit";

        NativeQuery nativeQuery = session.createNativeQuery(sql);
        nativeQuery.setParameter("mmsi", mmsi)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setParameter("limit", limit);

        nativeQuery.addScalar("uuidKey", StringType.INSTANCE)
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
        return ((List<PositionResponseDTO>) nativeQuery.getResultList());
    }
}
