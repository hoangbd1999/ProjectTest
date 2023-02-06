package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.config.ApplicationConfig;
import com.elcom.metacen.enrich.data.model.SatelliteImageChanges;
import com.elcom.metacen.enrich.data.model.SatelliteImageObjectAnalyzed;
import com.elcom.metacen.enrich.data.model.dto.*;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SatelliteImageChangeRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageChangeRepository.class);


    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public SatelliteImageChangeRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public SatelliteImageChanges insert(SatelliteImageChangeRequestDTO satelliteImageChangeRequestDTO) {
        Session session = null;
        String schemaMeta = this.getSchemaMeta();
        try {
            session = this.clickHouseSessionFactory.openSession();

            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    PreparedStatement pstmt = null;
                    try {
                        String sql = " INSERT INTO " + schemaMeta + ".satellite_image_changes ( uuidKey, tileNumber, timeFileOrigin, timeFileCompare, " +
                                     " imagePathFileOrigin, imagePathFileCompare, createdBy, ingestTime, isDeleted ) " +
                                     " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
                        
                        pstmt = conn.prepareStatement(sql);

                        pstmt.setString(1, satelliteImageChangeRequestDTO.getUuidKey());
                        pstmt.setString(2, satelliteImageChangeRequestDTO.getTileNumber());
                        pstmt.setString(3, satelliteImageChangeRequestDTO.getTimeFileOrigin());
                        pstmt.setString(4, satelliteImageChangeRequestDTO.getTimeFileCompare());
                        pstmt.setString(5, satelliteImageChangeRequestDTO.getImagePathFileOrigin());
                        pstmt.setString(6, satelliteImageChangeRequestDTO.getImagePathFileCompare());
                        pstmt.setString(7, satelliteImageChangeRequestDTO.getCreatedBy());
                        pstmt.setString(8,satelliteImageChangeRequestDTO.getIngestTime());
                        pstmt.setInt(9, satelliteImageChangeRequestDTO.getIsDeleted());
                        
                        LOGGER.info("SQL-create: [ {} ], status: [ {} ]", sql, pstmt.executeUpdate() > 0);
                        
                    } catch (Exception ex) {
                        LOGGER.error("ex: ", ex);
                    } finally {
                        if ( pstmt != null && !pstmt.isClosed() )
                            pstmt.close();
                        if ( conn != null && !conn.isClosed() )
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

    public Page<SatelliteImageChangeResponseDTO> filterSatelliteImageChange(SatelliteImageChangeFilterDTO req) {
        Session session = null;
        String schemaMeta = this.getSchemaMeta();
        try {
            
            session = this.clickHouseSessionFactory.openSession();
            
            Integer page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 0;
            Pageable pageable = PageRequest.of(page, req.getSize() != null && req.getSize() > 0 ? req.getSize() : 0);

            String fromTime = req.getFromTime().trim();
            String toTime = req.getToTime().trim();

            String condition = "";

            if (req.getTileNumberLst() != null && !req.getTileNumberLst().isEmpty()) {
                condition += " AND tileNumber IN :tileNumberLst ";
            }
            if (req.getCreatedByLst() != null && !req.getCreatedByLst().isEmpty()) {
                condition += " AND createdBy IN :createByLst ";
            }
            if (req.getProcessStatusLst() != null && !req.getProcessStatusLst().isEmpty()) {
                condition += " AND processStatus IN :processStatusLst ";
            }
            if (!StringUtil.isNullOrEmpty(req.getTileNumber())) {
                condition += " AND ilike(tileNumber, :tileNumber) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getTimeFileOrigin())) {
                condition += " AND (timeFileOrigin BETWEEN :timeFileOriginFromTime AND :timeFileOriginToTime) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getTimeFileCompare())) {
                condition += " AND (timeFileCompare BETWEEN :timeFileCompareFromTime AND :timeFileCompareToTime) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getTimeReceiveResult())) {
                condition += " AND (timeReceiveResult BETWEEN :timeReceiveResultFromTime AND :timeReceiveResultToTime) AND processStatus not in(1) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getCreatedBy())) {
                condition += " AND ilike(createdBy, :createdBy) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getIngestTime())) {
                condition += " AND (ingestTime BETWEEN :ingestTimeFromTime AND :ingestTimeToTime) ";
            }
            if (req.getProcessStatus() != null) {
                condition += " AND processStatus = :processStatus";
            }
            if (!StringUtil.isNullOrEmpty(req.getTerm())) {
                String checkUuidCondition = "";
                try {
                    UUID.fromString(req.getTerm());
                    checkUuidCondition += " (uuidKey = :term) OR ";
                } catch (Exception ignore) {}
                condition += " AND ( " +
                        checkUuidCondition +
                        "ilike(tileNumber, :keyword) OR " +
                        "ilike(createdBy, :keyword) " +
                        " ) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                condition = " (ingestTime BETWEEN :fromTime AND :toTime) " + condition;
            } else {
                condition = " uuidKey is not null " + condition;
            }
            String sqlTotal = " SELECT COUNT(uuidKey) "
                    + " FROM " + schemaMeta + ".satellite_image_changes sic WHERE isDeleted = 0 AND "
                    + condition;

            String sql = " SELECT uuidKey, tileNumber, timeFileOrigin, timeFileCompare, imagePathFileOrigin, imagePathFileCompare, timeReceiveResult, createdBy, ingestTime, "
                    + " processStatus, retryTimes, isDeleted "
                    + " FROM " + schemaMeta + ".satellite_image_changes sic WHERE isDeleted = 0 AND "
                    + condition;
            // sort
            if (!StringUtil.isNullOrEmpty(req.getSort())) {
                String sortItem = req.getSort().trim();
                if (sortItem.substring(0, 1).equals("-")) {
                    sql += " ORDER BY " + sortItem.substring(1) + " DESC ";
                } else {
                    sql += " ORDER BY " + sortItem;
                }
            } else {
                sql += " ORDER BY ingestTime DESC ";
            }

            // limit, offset
            sql += " LIMIT :limit OFFSET :offset ";

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("limit", req.getSize())
                    .setParameter("offset", page * req.getSize());
            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                query.setParameter("fromTime", fromTime);
                query.setParameter("toTime", toTime);
            }

            NativeQuery queryTotal = session.createSQLQuery(sqlTotal);
            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                queryTotal.setParameter("fromTime", fromTime);
                queryTotal.setParameter("toTime", toTime);
            }
            if (req.getTileNumberLst() != null && !req.getTileNumberLst().isEmpty()) {
                query.setParameter("tileNumberLst", req.getTileNumberLst());
                queryTotal.setParameter("tileNumberLst", req.getTileNumberLst());
            }
            if (req.getCreatedByLst() != null && !req.getCreatedByLst().isEmpty()) {
                query.setParameter("createByLst", req.getCreatedByLst());
                queryTotal.setParameter("createByLst", req.getCreatedByLst());
            }
            if (req.getProcessStatusLst() != null && !req.getProcessStatusLst().isEmpty()) {
                query.setParameter("processStatusLst", req.getProcessStatusLst());
                queryTotal.setParameter("processStatusLst", req.getProcessStatusLst());
            }
            if (!StringUtil.isNullOrEmpty(req.getTileNumber())) {
                String keyword = "%" + req.getTileNumber().trim() + "%";
                query.setParameter("tileNumber", keyword);
                queryTotal.setParameter("tileNumber", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getTimeFileOrigin())) {
                query.setParameter("timeFileOriginFromTime", DateUtils.parse(req.getTimeFileOrigin() + " 00:00:00"));
                queryTotal.setParameter("timeFileOriginFromTime", DateUtils.parse(req.getTimeFileOrigin() + " 00:00:00"));

                query.setParameter("timeFileOriginToTime", DateUtils.parse(req.getTimeFileOrigin() + " 23:59:59"));
                queryTotal.setParameter("timeFileOriginToTime", DateUtils.parse(req.getTimeFileOrigin() + " 23:59:59"));
            }
            if (!StringUtil.isNullOrEmpty(req.getTimeFileCompare())) {
                query.setParameter("timeFileCompareFromTime", DateUtils.parse(req.getTimeFileCompare() + " 00:00:00"));
                queryTotal.setParameter("timeFileCompareFromTime", DateUtils.parse(req.getTimeFileCompare() + " 00:00:00"));

                query.setParameter("timeFileCompareToTime", DateUtils.parse(req.getTimeFileCompare() + " 23:59:59"));
                queryTotal.setParameter("timeFileCompareToTime", DateUtils.parse(req.getTimeFileCompare() + " 23:59:59"));
            }
            if (!StringUtil.isNullOrEmpty(req.getTimeReceiveResult())) {
                query.setParameter("timeReceiveResultFromTime", DateUtils.parse(req.getTimeReceiveResult() + " 00:00:00"));
                queryTotal.setParameter("timeReceiveResultFromTime", DateUtils.parse(req.getTimeReceiveResult() + " 00:00:00"));

                query.setParameter("timeReceiveResultToTime", DateUtils.parse(req.getTimeReceiveResult() + " 23:59:59"));
                queryTotal.setParameter("timeReceiveResultToTime", DateUtils.parse(req.getTimeReceiveResult() + " 23:59:59"));
            }
            if (!StringUtil.isNullOrEmpty(req.getCreatedBy())) {
                String keyword = "%" + req.getCreatedBy().trim() + "%";
                query.setParameter("createdBy", keyword);
                queryTotal.setParameter("createdBy", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getIngestTime())) {
                query.setParameter("ingestTimeFromTime", DateUtils.parse(req.getIngestTime() + " 00:00:00"));
                queryTotal.setParameter("ingestTimeFromTime", DateUtils.parse(req.getIngestTime() + " 00:00:00"));

                query.setParameter("ingestTimeToTime", DateUtils.parse(req.getIngestTime() + " 23:59:59"));
                queryTotal.setParameter("ingestTimeToTime", DateUtils.parse(req.getIngestTime() + " 23:59:59"));
            }
            if (req.getProcessStatus() != null) {
                query.setParameter("processStatus", req.getProcessStatus());
                queryTotal.setParameter("processStatus", req.getProcessStatus());
            }
            if (!StringUtil.isNullOrEmpty(req.getTerm())) {
                String keyword = "%" + req.getTerm().trim() + "%";
                try {
                    query.setParameter("term", req.getTerm());
                    queryTotal.setParameter("term", req.getTerm());
                } catch (Exception ignored) {}
                query.setParameter("keyword", keyword);
                queryTotal.setParameter("keyword", keyword);
            }
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("tileNumber", StringType.INSTANCE)
                    .addScalar("timeFileOrigin", TimestampType.INSTANCE)
                    .addScalar("timeFileCompare", TimestampType.INSTANCE)
                    .addScalar("imagePathFileOrigin", StringType.INSTANCE)
                    .addScalar("imagePathFileCompare", StringType.INSTANCE)
                    .addScalar("timeReceiveResult", TimestampType.INSTANCE)
                    .addScalar("createdBy", StringType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("retryTimes", IntegerType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageChangeResponseDTO.class));

            List<SatelliteImageChangeResponseDTO> results = query.getResultList();
            
            if( results != null && !results.isEmpty() )
                return new PageImpl<>(results, pageable, ((Number) queryTotal.getSingleResult()).longValue());
            
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

    public SatelliteImageChanges findByUuid(String uuid) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String sql = " SELECT * FROM " + schemaMeta + ".satellite_image_changes WHERE uuidKey = :uuid ";
            NativeQuery query = session.createNativeQuery(sql)
                                       .setParameter("uuid", uuid);
            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("tileNumber", StringType.INSTANCE)
                    .addScalar("timeFileOrigin", TimestampType.INSTANCE)
                    .addScalar("timeFileCompare", TimestampType.INSTANCE)
                    .addScalar("imagePathFileOrigin", StringType.INSTANCE)
                    .addScalar("imagePathFileCompare", StringType.INSTANCE)
                    .addScalar("timeReceiveResult", TimestampType.INSTANCE)
                    .addScalar("createdBy", StringType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("retryTimes", IntegerType.INSTANCE)
                    .addScalar("isDeleted", IntegerType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageChanges.class));
            return (SatelliteImageChanges) query.getSingleResult();
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

    public SatelliteImageChanges delete(int isDeleted, String uuid) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".satellite_image_changes ON CLUSTER 'metacen_cluster' UPDATE "
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
