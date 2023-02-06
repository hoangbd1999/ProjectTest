package com.elcom.metacen.group.detect.repository.clickhouse;

import com.elcom.metacen.group.detect.model.dto.VsatAisDTO;
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
import java.util.List;

@Repository
public class VsatAisDataRepository extends BaseClickHouseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatAisDataRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public VsatAisDataRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public List<VsatAisDTO> getVsatAisWithCellId(long start, long end, Integer level) {
        Session session = clickHouseSessionFactory.openSession();
//        String sql = "SELECT va.name, va.objId, va.longitude, va.latitude, va.eventTime, geoToH3(longitude, latitude, 7) as cellId " +
//                "FROM vsat_ais va WHERE va.partName >= :start AND va.partName < :end";
        String sql = "SELECT va.typeId, va.name, va.mmsi, va.longitude, va.latitude, va.eventTime, geoToH3(longitude, latitude, :level) as cellId " +
                "FROM vsat_ais va WHERE va.timeKey >= :start AND va.timeKey < :end " +
//                "AND (va.mmsi = '413278590' OR va.mmsi = '413453970' OR va.mmsi = '413407150' OR va.mmsi = '413239740') " +
//                "AND (va.mmsi = '413402370' OR va.mmsi = '413457380') " +
                "ORDER BY va.eventTime ASC " +
                "LIMIT 2000000";
        NativeQuery query = session.createNativeQuery(sql);
        query.addScalar("name", StringType.INSTANCE)
                .addScalar("longitude", BigDecimalType.INSTANCE)
                .addScalar("latitude", BigDecimalType.INSTANCE)
                .addScalar("eventTime", LocalDateTimeType.INSTANCE)
//                .addScalar("objId", StringType.INSTANCE)
                .addScalar("mmsi", BigIntegerType.INSTANCE)
                .addScalar("cellId", LongType.INSTANCE)
                .addScalar("typeId", IntegerType.INSTANCE);
        query.setParameter("start", start)
                .setParameter("end", end)
                .setParameter("level", level);
        query.setResultTransformer(Transformers.aliasToBean(VsatAisDTO.class));
        return (List<VsatAisDTO>) query.getResultList();
    }
}
