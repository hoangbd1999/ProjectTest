package com.elcom.metacen.raw.data.repository;

import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.raw.data.model.dto.AisDataDTO;
import com.elcom.metacen.raw.data.model.dto.AisDataFilterDTO;
import com.elcom.metacen.raw.data.model.dto.PositionResponseDTO;
import com.elcom.metacen.utils.StringUtil;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Repository
public class AisDataRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AisDataRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public AisDataRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }
    
    public MessageContent filterAisRawData(AisDataFilterDTO input) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();

            String condition = "";
            if ( !StringUtil.isNullOrEmpty(input.getTerm()) )
                condition += " AND ( (toString(mmsi) like :term) OR "
                            + " ilike(name, :term) OR "
                            + " ilike(imo, :term) OR "
                            + " (toString(longitude) like :term) OR "
                            + " (toString(latitude) like :term) ) ";
            
            if ( input.getMmsi() != null )
                condition += " AND (toString(mmsi) like :mmsi) ";
            
            if ( !StringUtil.isNullOrEmpty(input.getImo()) )
                condition += " AND ilike(imo, :imo) ";
            
            if ( input.getLongitude() != null )
                condition += " AND (toString(longitude) like :longitude) ";

            if ( input.getLatitude() != null )
                condition += " AND (toString(latitude) like :latitude) ";
            
            if ( input.getCountryId() != null && !input.getCountryId().isEmpty() )
                condition += " AND countryId IN :countryIds ";
            
            if ( input.getDataVendors() != null && !input.getDataVendors().isEmpty() )
                condition += " AND dataVendor IN :dataVendors ";

            if ( input.getProcessStatus() != null && !input.getProcessStatus().isEmpty() )
                condition += " AND processStatus IN :processStatus ";
            
            condition = " ( eventTime BETWEEN :fromTime AND :toTime ) " + condition;
            
            String sql = " SELECT t.mmsi AS mmsi, t.imo AS imo, t.name AS name, t.sog AS sog, t.cog AS cog, t.longitude AS longitude, t.latitude AS latitude " +
                         " , t.eventTime AS eventTime, t.dataVendor AS dataVendor " +
                         " FROM ( SELECT * FROM ( " +
                         "    SELECT uuidKey, mmsi, eventTime, ingestTime, row_number() OVER ( PARTITION BY mmsi ORDER BY eventTime DESC, ingestTime DESC ) AS rank " +
                         "    FROM ais_data " +
                         "    WHERE " + condition +
                         "    SETTINGS allow_experimental_window_functions = 1 " +
                         " ) WHERE rank = 1 ) r " +
                         " INNER JOIN ( " +
                         "   SELECT uuidKey, mmsi, imo, name, sog, cog, longitude, latitude, eventTime, ingestTime, dataVendor " +
                         "   FROM ais_data " +
                         "   WHERE " + condition +
                         // " ) t ON ( r.uuidKey = t.uuidKey AND r.mmsi = t.mmsi AND r.eventTime = t.eventTime AND r.ingestTime = t.ingestTime ) LIMIT :limit OFFSET 0";
                         " ) t ON r.uuidKey = t.uuidKey LIMIT :limit OFFSET 0";

            NativeQuery query = session.createNativeQuery(sql)
                                        .setParameter("fromTime", input.getFromTime().trim())
                                        .setParameter("toTime", input.getToTime().trim())
                                        .setParameter("limit", input.getLimit());

            if (!StringUtil.isNullOrEmpty(input.getTerm())) {
                String term = "%" + input.getTerm().trim() + "%";
                query.setParameter("term", term);
            }
            if ( input.getMmsi() != null )
                query.setParameter("mmsi", "%" + input.getMmsi() + "%");
            
            if ( !StringUtil.isNullOrEmpty(input.getImo()) )
                query.setParameter("imo", "%" + input.getImo().trim() + "%");
            
            if ( input.getLongitude() != null )
                query.setParameter("longitude", "%" + input.getLongitude() + "%");

            if ( input.getLatitude() != null )
                query.setParameter("latitude", "%" + input.getLatitude() + "%");
            
            if ( input.getCountryId() != null && !input.getCountryId().isEmpty() )
                query.setParameterList("countryIds", input.getCountryId());
            
            if ( input.getDataVendors() != null && !input.getDataVendors().isEmpty() )
                query.setParameterList("dataVendors", input.getDataVendors());
            
            if ( input.getProcessStatus() != null && !input.getProcessStatus().isEmpty() )
                query.setParameterList("processStatus", input.getProcessStatus());

            query.addScalar("mmsi", BigIntegerType.INSTANCE)
//                .addScalar("uuidKey", StringType.INSTANCE)
                .addScalar("name", StringType.INSTANCE)
                .addScalar("imo", StringType.INSTANCE)
//                .addScalar("countryId", IntegerType.INSTANCE)
                .addScalar("sog", FloatType.INSTANCE)
                .addScalar("cog", FloatType.INSTANCE)
                .addScalar("longitude", BigDecimalType.INSTANCE)
                .addScalar("latitude", BigDecimalType.INSTANCE)
//                .addScalar("processStatus", IntegerType.INSTANCE)
                .addScalar("dataVendor", StringType.INSTANCE)
                .addScalar("eventTime", TimestampType.INSTANCE)
//                .addScalar("ingestTime", TimestampType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(AisDataDTO.class));
            return new MessageContent(query.getResultList());
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }

    public Page<AisDataDTO> filterAisMapping(Integer page, Integer size, String term) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();
            Pageable pageable = PageRequest.of(page, size);

            String condition = "";
            if (!StringUtil.isNullOrEmpty(term)) {
                condition += " AND ( "
                        + " (toString(mmsi) like :term) OR "
                        + " ilike(name, :term) OR "
                        + " ilike(imo, :term) "
                        + " ) ";
            }

            String sqlTotal = " SELECT COUNT(uuidKey) " +
                              " FROM ( SELECT uuidKey FROM ais_data WHERE uuidKey IS NOT NULL " + condition + " LIMIT :limit OFFSET :offset ) ";

            String sql = " SELECT t.uuidKey AS uuidKey, t.mmsi AS mmsi, t.imo AS imo, t.name AS name, t.sog AS sog, t.cog AS cog, t.longitude AS longitude, t.latitude AS latitude, t.eventTime AS eventTime, t.ingestTime AS ingestTime " +
                    " , t.countryId AS countryId, t.processStatus AS processStatus, t.dataVendor AS dataVendor " +
                    " FROM ( SELECT * FROM ( " +
                    "    SELECT uuidKey, mmsi, eventTime, ingestTime, row_number() OVER ( PARTITION BY mmsi ORDER BY eventTime DESC, ingestTime DESC ) AS rank " +
                    "    FROM ais_data " +
                    "    WHERE uuidKey IS NOT NULL " + condition +
                    "    SETTINGS allow_experimental_window_functions = 1 " +
                    " ) WHERE rank = 1 ) r " +
                    " INNER JOIN ( " +
                    "   SELECT uuidKey, mmsi, imo, name, sog, cog, longitude, latitude, eventTime, ingestTime, countryId, processStatus, dataVendor " +
                    "   FROM ais_data " +
                    "   WHERE uuidKey IS NOT NULL " + condition +
                    " ) t ON r.uuidKey = t.uuidKey ";

            sql += " LIMIT :limit OFFSET :offset ";

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("limit", size)
                    .setParameter("offset", page * size);
            NativeQuery queryTotal = session.createSQLQuery(sqlTotal)
                    .setParameter("limit", size)
                    .setParameter("offset", page * size);

            if (!StringUtil.isNullOrEmpty(term)) {
                String search = "%" + term.trim() + "%";
                query.setParameter("term", search);
                queryTotal.setParameter("term", search);
            }
            query.addScalar("mmsi", BigIntegerType.INSTANCE)
                //    .addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("name", StringType.INSTANCE)
                    .addScalar("imo", StringType.INSTANCE)
                 //   .addScalar("countryId", IntegerType.INSTANCE)
                    .addScalar("sog", FloatType.INSTANCE)
                    .addScalar("cog", FloatType.INSTANCE)
                    .addScalar("longitude", BigDecimalType.INSTANCE)
                    .addScalar("latitude", BigDecimalType.INSTANCE)
                //    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                //    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(AisDataDTO.class));

            List<AisDataDTO> results = query.getResultList();
            return new PageImpl<>(results, pageable, ((Number) queryTotal.getSingleResult()).longValue());
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }

    /*public Page<AisDataDTO> filterAisDataOld(AisDataFilterDTO input) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
//            Integer page = input.getPage() != null && input.getPage() > 0 ? input.getPage() : 0;
//            Pageable pageable = PageRequest.of(page, input.getSize());

            String fromTime = input.getFromTime().trim();
            String toTime = input.getToTime().trim();

            String condition = "";
            if (!StringUtil.isNullOrEmpty(input.getTerm())) {
                condition += " AND ( "
                        + " (toString(mmsi) like :term) OR "
                        + " ilike(imo, :term) OR "
                        + " (toString(longitude) like :term) OR "
                        + " (toString(latitude) like :term) "
                        + " ) ";
            }
            if (input.getMmsi() != null)
                condition += " AND ad.mmsi = :mmsi";

            if (!StringUtil.isNullOrEmpty(input.getImo()))
                condition += " AND ad.imo = :imo";

            if (input.getLongitude() != null)
                condition += " AND ad.longitude = :longitude";

            if (input.getLatitude() != null)
                condition += " AND ad.latitude = :latitude";

            if (input.getDataVendors() != null && !input.getDataVendors().isEmpty())
                condition += " AND dataVendor IN :dataVendors";

            if (input.getProcessStatus() != null && !input.getProcessStatus().isEmpty())
                condition += " AND processStatus IN :processStatus";

            condition = " ( eventTime BETWEEN :fromTime AND :toTime ) " + condition;

            String sqlTotal = " SELECT COUNT(uuidKey) FROM " + schemaMeta + ".ais_data ad WHERE " + condition;

            String sql = " SELECT ad.uuidKey, ad.mmsi, ad.imo, ad.callSign, ad.name, ad.shipType, ad.countryId, ad.rot, ad.sog, ad.cog, ad.draught," +
                         " ad.messageType, ad.eta , ad.destination, ad.longitude, ad.latitude," +
                         " ad.second, ad.communicationStateSyncState, ad.specialManeuverIndicator, ad.toStern, ad.toPort, ad.dataTerminalReady, ad.positionFixingDevice," +
                         " ad.toStarboard, ad.timeKey, ad.toBow, ad.rateOfTurn, ad.repeatIndicator, ad.transponderClass, ad.navigationStatus, ad.trueHeading, ad.raimFlag," +
                         " ad.positionAccuracy, ad.valid, ad.processStatus, ad.eventTime, ad.ingestTime, ad.timeKey" +
                         " FROM " + schemaMeta + ".ais_data ad WHERE " + condition;

            // sort
//            if (!StringUtil.isNullOrEmpty(input.getSort())) {
//                String sortItem = input.getSort().trim();
//                if (sortItem.substring(0, 1).equals("-")) {
//                    sql += " ORDER BY " + sortItem.substring(1) + " DESC ";
//                } else {
//                    sql += " ORDER BY " + sortItem + " ASC ";
//                }
//            } else {
//                sql += " ORDER BY ad.eventTime DESC, ad.ingestTime DESC ";
//            }
            // limit, offset
            sql += " LIMIT :limit OFFSET :offset ";

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime);
//                    .setParameter("limit", input.getSize())
//                    .setParameter("offset", page * input.getSize());

            NativeQuery queryTotal = session.createSQLQuery(sqlTotal)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime);

            if (!StringUtil.isNullOrEmpty(input.getTerm())) {
                String term = "%" + input.getTerm().trim() + "%";
                query.setParameter("term", term);
                queryTotal.setParameter("term", term);
            }
            if (input.getMmsi() != null) {
                query.setParameter("mmsi", input.getMmsi());
                queryTotal.setParameter("mmsi", input.getMmsi());
            }
            if ( input.getImo() != null ) {
                query.setParameter("imo", input.getImo());
                queryTotal.setParameter("imo", input.getImo());
            }
            if (input.getLongitude() != null) {
                query.setParameter("longitude", input.getLongitude());
                queryTotal.setParameter("longitude", input.getLongitude());
            }
            if (input.getLatitude() != null) {
                query.setParameter("latitude", input.getLatitude());
                queryTotal.setParameter("latitude", input.getLatitude());
            }
            if (input.getDataVendors() != null && !input.getDataVendors().isEmpty()) {
                query.setParameterList("dataVendors", input.getDataVendors());
                queryTotal.setParameterList("dataVendors", input.getDataVendors());
            }
            if (input.getProcessStatus() != null && !input.getProcessStatus().isEmpty()) {
                query.setParameterList("processStatus", input.getProcessStatus());
                queryTotal.setParameterList("processStatus", input.getProcessStatus());
            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("mmsi", BigIntegerType.INSTANCE)
                    .addScalar("imo", StringType.INSTANCE)
                    .addScalar("callSign", StringType.INSTANCE)
                    .addScalar("name", StringType.INSTANCE)
                    .addScalar("shipType", StringType.INSTANCE)
                    .addScalar("countryId", IntegerType.INSTANCE)
                    .addScalar("rot", FloatType.INSTANCE)
                    .addScalar("sog", FloatType.INSTANCE)
                    .addScalar("cog", FloatType.INSTANCE)
                    .addScalar("draught", FloatType.INSTANCE)
                    .addScalar("longitude", DoubleType.INSTANCE)
                    .addScalar("latitude", DoubleType.INSTANCE)
                    .addScalar("messageType", StringType.INSTANCE)
                    .addScalar("eta", StringType.INSTANCE)
                    .addScalar("destination", StringType.INSTANCE)
                    .addScalar("second", IntegerType.INSTANCE)
                    .addScalar("communicationStateSyncState", StringType.INSTANCE)
                    .addScalar("specialManeuverIndicator", StringType.INSTANCE)
                    .addScalar("toStern", IntegerType.INSTANCE)
                    .addScalar("toPort", IntegerType.INSTANCE)
                    .addScalar("dataTerminalReady", BooleanType.INSTANCE)
                    .addScalar("positionFixingDevice", StringType.INSTANCE)
                    .addScalar("toStarboard", IntegerType.INSTANCE)
                    .addScalar("toBow", IntegerType.INSTANCE)
                    .addScalar("rateOfTurn", IntegerType.INSTANCE)
                    .addScalar("repeatIndicator", IntegerType.INSTANCE)
                    .addScalar("transponderClass", StringType.INSTANCE)
                    .addScalar("navigationStatus", IntegerType.INSTANCE)
                    .addScalar("trueHeading", IntegerType.INSTANCE)
                    .addScalar("raimFlag", BooleanType.INSTANCE)
                    .addScalar("positionAccuracy", BooleanType.INSTANCE)
                    .addScalar("valid", BooleanType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("timeKey", BigIntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(AisDataDTO.class));
          //  return new PageImpl<>(query.getResultList(), pageable, ((Number) queryTotal.getSingleResult()).longValue());
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
        return null;
    }*/

    public List<PositionResponseDTO> getAisPositionOfShip(Integer mmsi, String startTime, String endTime, Integer limit) {
        Session session = this.clickHouseSessionFactory.openSession();
        String sql = " SELECT ad.uuidKey AS uuidKey, ad.mmsi AS mmsi, ad.name AS name, ad.imo AS imo " +
                " , ad.sog AS sog, ad.cog AS cog, ad.longitude AS longitude, ad.latitude AS latitude, ad.eventTime AS eventTime, ad.ingestTime AS ingestTime " +
                " , ad.countryId AS countryId, ad.dataVendor AS dataVendor, 'AIS' AS sourceType " +
                " FROM ais_data ad " +
                " WHERE ad.mmsi = :mmsi AND " +
                " ad.eventTime >= :startTime AND ad.eventTime < :endTime " +
                " order by ad.eventTime " +
                " limit :limit";
        NativeQuery query = session.createNativeQuery(sql);
        query.setParameter("mmsi", mmsi)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setParameter("limit", limit);
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
    }
}
