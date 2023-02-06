package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.model.dto.VsatAisFilterListRequestDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatAisResponseDTO;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.utils.StringUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
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

    public MessageContent searchAisListAllGeneral(VsatAisFilterListRequestDTO input) {
        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();

            String fromTime = input.getFromTime().trim();
            String toTime = input.getToTime().trim();

            String condition = "";
            String orderBy = "";

            if (!StringUtil.isNullOrEmpty(input.getMmsi())) {
                condition += " AND mmsi = :mmsi AND ( longitude <> 0 AND latitude <> 20 ) ";
                orderBy = " ORDER BY eventTime "; // Nếu tìm theo 1 mssi cụ thể, thì sort theo eventTime tăng dần
            }
            condition = "AND ( eventTime BETWEEN :fromTime AND :toTime ) " + condition;

            String sql = " SELECT mmsi, draught, destination, dimA, dimB, dimC, dimD, name, callSign, rot, sog, cog "
                    + " , longitude, latitude, eventTime, sourceIp, destIp, typeId, countryId, dataSourceName,dataSourceId"
                    + " FROM vsat_ais WHERE 1 = 1 ";

            // limit, offset
            sql += condition + orderBy + " LIMIT :limit ";

            NativeQuery query = session.createNativeQuery(sql);

            if (!StringUtil.isNullOrEmpty(input.getFromTime()) && !StringUtil.isNullOrEmpty(input.getToTime())) {
                query.setParameter("fromTime", fromTime);
                query.setParameter("toTime", toTime);
            }
            query.setParameter("limit", input.getLimit());

            if (!StringUtil.isNullOrEmpty(input.getMmsi()))
                query.setParameter("mmsi", input.getMmsi().trim());

            query.addScalar("mmsi", BigIntegerType.INSTANCE)
//                .addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("name", StringType.INSTANCE)
                    .addScalar("callSign", StringType.INSTANCE)
//                .addScalar("imo", StringType.INSTANCE)
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
//                .addScalar("dataVendor", StringType.INSTANCE)
//                .addScalar("processStatus", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatAisResponseDTO.class));

            List<VsatAisResponseDTO> results = query.getResultList();
            return new MessageContent(results);

        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }

    public MessageContent findDetailVessel(Long mmsi) {
        Session session = openSession();
        try {

            String sql = " SELECT ai.mmsi, ai.imo, ai.draught, (ai.dimA + ai.dimB) AS length, (ai.dimC + ai.dimD) AS width , ai.name, ai.callSign "
                    + " , ai.rot AS rot, ai.sog AS sog, ai.cog AS cog, ai.longitude AS longitude, ai.latitude AS latitude, ai.eventTime AS eventTime, ai.sourceIp AS sourceIp, ai.destIp AS destIp "
                    + " , ai.typeId AS typeId, ai.countryId AS countryId, ai.dataSourceName AS dataSourceName "
                    + " FROM vsat_ais ai "
                    + " WHERE ai.mmsi = :mmsi ";
            NativeQuery query = session.createSQLQuery(sql)
                    .setParameter("mmsi", mmsi);
            query.addScalar("mmsi", BigIntegerType.INSTANCE)
//                .addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("name", StringType.INSTANCE)
                    .addScalar("callSign", StringType.INSTANCE)
//                .addScalar("imo", StringType.INSTANCE)
                    .addScalar("countryId", IntegerType.INSTANCE)
                    .addScalar("typeId", IntegerType.INSTANCE)
                    .addScalar("length", LongType.INSTANCE)
                    .addScalar("width", LongType.INSTANCE)
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
//                .addScalar("dataVendor", StringType.INSTANCE)
//                .addScalar("processStatus", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatAisResponseDTO.class));

            VsatAisResponseDTO result = (VsatAisResponseDTO) query.uniqueResult();
            return new MessageContent(result);
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            closeSession(session);
        }
        return null;
    }
}
