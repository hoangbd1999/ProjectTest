package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.model.SatelliteImageChanges;
import com.elcom.metacen.enrich.data.model.VsatMediaDataObjectAnalyzed;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeFilterDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeResponseDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaDataObjectAnalyzedRequestDTO;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.StringUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class VsatMediaDataObjectAnalyzedRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaDataObjectAnalyzedRepository.class);


    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public VsatMediaDataObjectAnalyzedRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public VsatMediaDataObjectAnalyzed insert(VsatMediaDataObjectAnalyzedRequestDTO req) {
        Session session = null;
        String schemaMeta = this.getSchemaMeta();
        try {
            session = this.clickHouseSessionFactory.openSession();

            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT into " + schemaMeta + ".vsat_media_data_object_analyzed (uuidKey, vsatMediaDataAnalyzedUuidKey, objectId, objectMmsi, objectUuid, "
                                + "objectType, objectName, ingestTime, isDeleted) "
                                + "VALUES (?,?,?,?,?,?,?,?,?)";
                        pstmt = conn.prepareStatement(sql);

                        pstmt.setString(1, req.getUuidKey());
                        pstmt.setString(2, req.getVsatMediaDataAnalyzedUuidKey());
                        pstmt.setString(3, req.getObjectId());
                        pstmt.setString(4, req.getObjectMmsi());
                        pstmt.setString(5, req.getObjectUuid());
                        pstmt.setString(6, req.getObjectType());
                        pstmt.setString(7, req.getObjectName());;
                        pstmt.setString(8,req.getIngestTime());
                        pstmt.setInt(9, req.getIsDeleted());
                        int insertStatus = pstmt.executeUpdate();
                        LOGGER.info("SQL: [ {} ], status return: [ {} ]", sql, insertStatus);
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

    public VsatMediaDataObjectAnalyzed findByUuidAndIsDeleted(String uuid, int isDeleted) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String sql = "SELECT * FROM " + this.getSchemaMeta() + ".vsat_media_data_object_analyzed WHERE uuidKey = :uuidKey AND isDeleted = :isDeleted";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("uuidKey", uuid)
                    .setParameter("isDeleted", isDeleted);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("vsatMediaDataAnalyzedUuidKey", StringType.INSTANCE)
                    .addScalar("objectId", StringType.INSTANCE)
                    .addScalar("objectMmsi", StringType.INSTANCE)
                    .addScalar("objectUuid", StringType.INSTANCE)
                    .addScalar("objectType", StringType.INSTANCE)
                    .addScalar("objectName", StringType.INSTANCE)
                    .addScalar("ingestTime", DateType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)

                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDataObjectAnalyzed.class));
            return (VsatMediaDataObjectAnalyzed) query.getSingleResult();
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

    public List<VsatMediaDataObjectAnalyzed> findAllByIsDeleted(String vsatMediaDataAnalyzedUuidKey, int isDeleted) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String sql = "SELECT * FROM " + this.getSchemaMeta() + ".vsat_media_data_object_analyzed WHERE vsatMediaDataAnalyzedUuidKey = :vsatMediaDataAnalyzedUuidKey AND isDeleted = :isDeleted";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("isDeleted", isDeleted)
                    .setParameter("vsatMediaDataAnalyzedUuidKey", vsatMediaDataAnalyzedUuidKey);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("vsatMediaDataAnalyzedUuidKey", StringType.INSTANCE)
                    .addScalar("objectId", StringType.INSTANCE)
                    .addScalar("objectMmsi", StringType.INSTANCE)
                    .addScalar("objectUuid", StringType.INSTANCE)
                    .addScalar("objectType", StringType.INSTANCE)
                    .addScalar("objectName", StringType.INSTANCE)
                    .addScalar("ingestTime", DateType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDataObjectAnalyzed.class));

            List<VsatMediaDataObjectAnalyzed> results = query.getResultList();
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
    public List<VsatMediaDataObjectAnalyzed> findAllByObjectUuid(String objectUuid, int isDeleted) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String sql = "SELECT * FROM " + this.getSchemaMeta() + ".vsat_media_data_object_analyzed WHERE objectUuid = :objectUuid AND isDeleted = :isDeleted";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("isDeleted", isDeleted)
                    .setParameter("objectUuid", objectUuid);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("vsatMediaDataAnalyzedUuidKey", StringType.INSTANCE)
                    .addScalar("objectId", StringType.INSTANCE)
                    .addScalar("objectMmsi", StringType.INSTANCE)
                    .addScalar("objectUuid", StringType.INSTANCE)
                    .addScalar("objectType", StringType.INSTANCE)
                    .addScalar("objectName", StringType.INSTANCE)
                    .addScalar("ingestTime", DateType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDataObjectAnalyzed.class));

            List<VsatMediaDataObjectAnalyzed> results = query.getResultList();
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
    public VsatMediaDataObjectAnalyzed checkExist(String vsatMediaDataAnalyzedUuidKey, int isDeleted) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".vsat_media_data_object_analyzed ON CLUSTER 'metacen_cluster' DELETE "
                            + "where isDeleted = ? AND vsatMediaDataAnalyzedUuidKey = '" + vsatMediaDataAnalyzedUuidKey + "'";

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

    public void updateNameObjectInternal(String objectUuid,String objectName, int isDeleted) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".vsat_media_data_object_analyzed ON CLUSTER 'metacen_cluster' UPDATE "
                            + "isDeleted = ?, objectName = ?"
                            + "where objectUuid = '" + objectUuid + "'";

                    pstmt = conn.prepareStatement(sql);
                    int index = 1;
                    pstmt.setInt(index++, isDeleted);
                    pstmt.setString(index++, objectName);
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
    }

    public VsatMediaDataObjectAnalyzed delete(int isDeleted, String uuid) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".vsat_media_data_object_analyzed ON CLUSTER 'metacen_cluster' UPDATE "
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

    public Boolean findByObjectUuid(String objectUuid, String objectType, int isDeleted) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String sql = "SELECT * FROM " + this.getSchemaMeta() + ".vsat_media_data_object_analyzed WHERE objectUuid = :objectUuid AND objectType = :objectType AND isDeleted = :isDeleted LIMIT 1";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("objectUuid", objectUuid)
                    .setParameter("objectType", objectType)
                    .setParameter("isDeleted", isDeleted);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("vsatMediaDataAnalyzedUuidKey", StringType.INSTANCE)
                    .addScalar("objectId", StringType.INSTANCE)
                    .addScalar("objectMmsi", StringType.INSTANCE)
                    .addScalar("objectUuid", StringType.INSTANCE)
                    .addScalar("objectType", StringType.INSTANCE)
                    .addScalar("objectName", StringType.INSTANCE)
                    .addScalar("ingestTime", DateType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)

                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDataObjectAnalyzed.class));
            return query.getSingleResult() != null ? true : false;
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }
    }
}
