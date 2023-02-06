package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.model.SatelliteImageData;
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
public class SatelliteImageDataRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageDataRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public SatelliteImageDataRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public List<SatelliteImageData> findByTileNumber(String tileNumber) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String sql = "SELECT * FROM " + schemaMeta + ".satellite_image_data WHERE tileNumber = :tileNumber ORDER BY captureTime DESC ";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("tileNumber", tileNumber);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("satelliteName", StringType.INSTANCE)
                    .addScalar("missionId", StringType.INSTANCE)
                    .addScalar("productLevel", StringType.INSTANCE)
                    .addScalar("baseLineNumber", StringType.INSTANCE)
                    .addScalar("relativeOrbitNumber", StringType.INSTANCE)
                    .addScalar("tileNumber", StringType.INSTANCE)
                    .addScalar("originLongitude", FloatType.INSTANCE)
                    .addScalar("originLatitude", FloatType.INSTANCE)
                    .addScalar("cornerLongitude", FloatType.INSTANCE)
                    .addScalar("cornerLatitude", FloatType.INSTANCE)
                    .addScalar("rootDataFolderPath", StringType.INSTANCE)
                    .addScalar("geoWmsUrl", StringType.INSTANCE)
                    .addScalar("geoWorkSpace", StringType.INSTANCE)
                    .addScalar("geoLayerName", StringType.INSTANCE)
                    .addScalar("captureTime", TimestampType.INSTANCE)
                    .addScalar("secondTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageData.class));
            List<SatelliteImageData> results = query.getResultList();
            return results;
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }

        return null;
    }

}
