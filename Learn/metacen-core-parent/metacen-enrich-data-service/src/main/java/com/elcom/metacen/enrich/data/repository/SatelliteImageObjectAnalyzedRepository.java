package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.model.SatelliteImageObjectAnalyzed;
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
import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class SatelliteImageObjectAnalyzedRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageObjectAnalyzedRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public SatelliteImageObjectAnalyzedRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public List<SatelliteImageObjectAnalyzed> satelliteImageUuidKey(String uuid) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String sql = "SELECT * FROM " + schemaMeta + ".satellite_image_object_analyzed WHERE satelliteImageUuidKey = :satelliteImageUuidKey AND isDeleted = 0";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("satelliteImageUuidKey", uuid);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("satelliteImageUuidKey", StringType.INSTANCE)
                    .addScalar("width", DoubleType.INSTANCE)
                    .addScalar("height", DoubleType.INSTANCE)
                    .addScalar("longitude", BigDecimalType.INSTANCE)
                    .addScalar("latitude", BigDecimalType.INSTANCE)
                    .addScalar("color", StringType.INSTANCE)
                    .addScalar("imageFilePath", StringType.INSTANCE)
                    .addScalar("analyzedEngine", StringType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageObjectAnalyzed.class));
             List<SatelliteImageObjectAnalyzed> result = query.getResultList();
             return result;
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

    public SatelliteImageObjectAnalyzed findByUuid(String uuid) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String sql = "SELECT * FROM " + schemaMeta + ".satellite_image_object_analyzed WHERE uuidKey = :uuid";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("uuid", uuid);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("satelliteImageUuidKey", StringType.INSTANCE)
                    .addScalar("width", DoubleType.INSTANCE)
                    .addScalar("height", DoubleType.INSTANCE)
                    .addScalar("longitude", BigDecimalType.INSTANCE)
                    .addScalar("latitude", BigDecimalType.INSTANCE)
                    .addScalar("color", StringType.INSTANCE)
                    .addScalar("imageFilePath", StringType.INSTANCE)
                    .addScalar("analyzedEngine", StringType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageObjectAnalyzed.class));
            return (SatelliteImageObjectAnalyzed) query.getSingleResult();
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

    public SatelliteImageObjectAnalyzed delete(int isDeleted, String uuid) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".satellite_image_object_analyzed ON CLUSTER 'metacen_cluster' UPDATE "
                            + "isDeleted = ? "
                            + "where uuidKey = '" + uuid + "'";

                    pstmt = conn.prepareStatement(sql);
                    int index = 1;
                    pstmt.setInt(index++, isDeleted);
                    int updateStatus = pstmt.executeUpdate();
                    LOGGER.info("SQL: [ {} ], status return: [ {} ]", sql, updateStatus);
                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                } finally {
                    if (pstmt != null && !pstmt.isClosed()) {
                        pstmt.close();
                    }
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                }
            });
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
