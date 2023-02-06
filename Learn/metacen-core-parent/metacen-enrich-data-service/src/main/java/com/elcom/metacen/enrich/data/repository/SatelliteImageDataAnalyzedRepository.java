package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.config.ApplicationConfig;
import com.elcom.metacen.enrich.data.model.SatelliteImageDataAnalyzed;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageDataAnalyzedDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageDataAnalyzedFilterDTO;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.StringUtil;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
public class SatelliteImageDataAnalyzedRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageDataAnalyzedRepository.class);

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public SatelliteImageDataAnalyzedRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public List<SatelliteImageDataAnalyzedDTO> filterSatelliteImageDataForMap(SatelliteImageDataAnalyzedFilterDTO req) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {

            String fromTime = req.getFromTime().trim();
            String toTime = req.getToTime().trim();

            String condition = "";
            if (req.getDataVendorLst() != null && !req.getDataVendorLst().isEmpty()) {
                condition += " AND dataVendor IN :dataVendorLst ";
            }
            if (req.getTileNumberLst() != null && !req.getTileNumberLst().isEmpty()) {
                condition += " AND tileNumber IN :tileNumberLst ";
            }
            if (req.getProcessStatusLst() != null && !req.getProcessStatusLst().isEmpty()) {
                condition += " AND processStatus IN :processStatusLst ";
            }
            if (req.getCommentLst() != null && !req.getCommentLst().isEmpty()) {
                condition += " AND isNoted IN :isNoted ";
            }

            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                condition = " ( processTime BETWEEN :fromTime AND :toTime ) " + condition;
            } else {
                condition = " uuidKey is not null " + condition;
            }

            String columnFetch = " uuidKey, satelliteName, missionId, productLevel, baseLineNumber, relativeOrbitNumber, tileNumber, originLongitude, originLatitude, cornerLongitude, cornerLatitude,  rootDataFolderPath, geoWmsUrl, geoWorkSpace, geoLayerName, captureTime, secondTime, processTime, ingestTime, processStatus, isNoted, dataVendor ";

            String sql = " SELECT " + columnFetch
                    + " FROM "
                    + "  ( SELECT * "
                    + "   FROM "
                    + "     ( SELECT tileNumber, "
                    + "             processTime, "
                    + "             ingestTime, "
                    + "             row_number() OVER (PARTITION BY tileNumber "
                    + "                                ORDER BY processTime DESC, ingestTime DESC) AS rank "
                    + "      FROM " + schemaMeta + ".satellite_image_data_analyzed "
                    + "      WHERE " + condition + " SETTINGS allow_experimental_window_functions = 1 ) "
                    + "   WHERE rank = 1 ) r "
                    + " INNER JOIN "
                    + "  ( SELECT " + columnFetch
                    + "   FROM " + schemaMeta + ".satellite_image_data_analyzed "
                    + "   WHERE " + condition + " ) t "
                    + "   ON ( r.tileNumber = t.tileNumber AND r.processTime = t.processTime AND r.ingestTime = t.ingestTime ) "
                    + "  LIMIT :limit OFFSET 0 ";

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("limit", req.getSize());
            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                query.setParameter("fromTime", fromTime);
                query.setParameter("toTime", toTime);
            }
            if (req.getDataVendorLst() != null && !req.getDataVendorLst().isEmpty()) {
                query.setParameter("dataVendorLst", req.getDataVendorLst());
            }
            if (req.getTileNumberLst() != null && !req.getTileNumberLst().isEmpty()) {
                query.setParameter("tileNumberLst", req.getTileNumberLst());
            }
            if (req.getProcessStatusLst() != null && !req.getProcessStatusLst().isEmpty()) {
                query.setParameter("processStatusLst", req.getProcessStatusLst());
            }
            if (req.getCommentLst() != null && !req.getCommentLst().isEmpty()) {
                query.setParameter("isNoted", req.getCommentLst());
            }
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
                    .addScalar("processTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("isNoted", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageDataAnalyzedDTO.class));

            List<SatelliteImageDataAnalyzedDTO> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                results = results.stream()
                        .map(x -> {
                            String imageFilePathLocal = x.getRootDataFolderPath() + "/infor.jpg";
                            x.setImageFilePathLocal(imageFilePathLocal);
                            x.setImageFilePath(imageFilePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
                            return x;
                        })
                        .collect(Collectors.toList());
            }

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

    public Page<SatelliteImageDataAnalyzedDTO> filterSatelliteImageData(SatelliteImageDataAnalyzedFilterDTO req) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            Integer page = req.getPage() > 0 ? req.getPage() : 0;
            Pageable pageable = PageRequest.of(page, req.getSize());

            String fromTime = req.getFromTime().trim();
            String toTime = req.getToTime().trim();

            String condition = "";
            if (req.getDataVendorLst() != null && !req.getDataVendorLst().isEmpty()) {
                condition += " AND dataVendor IN :dataVendorLst ";
            }
            if (req.getTileNumberLst() != null && !req.getTileNumberLst().isEmpty()) {
                condition += " AND tileNumber IN :tileNumberLst ";
            }
            if (req.getProcessStatusLst() != null && !req.getProcessStatusLst().isEmpty()) {
                condition += " AND processStatus IN :processStatusLst ";
            }
            if (req.getCommentLst() != null && !req.getCommentLst().isEmpty()) {
                condition += " AND isNoted IN :isNoted ";
            }
            if (!StringUtil.isNullOrEmpty(req.getTerm())) {
                String checkUuidCondition = "";
                try {
                    UUID uuid = UUID.fromString(req.getTerm());
                    checkUuidCondition += " (uuidKey = :term) OR ";
                } catch (Exception ignored) {
                }
                condition += " AND ("
                        + checkUuidCondition
                        + " ilike(dataVendor, :keyword) OR "
                        + " ilike(satelliteName, :keyword) OR "
                        + " ilike(toString(originLongitude), :keyword) OR "
                        + " ilike(toString(originLatitude), :keyword) OR "
                        + " ilike(toString(cornerLongitude), :keyword) OR "
                        + " ilike(toString(cornerLatitude), :keyword) OR "
                        + " ilike(tileNumber, :keyword)"
                        + " ) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                condition = " (processTime BETWEEN :fromTime AND :toTime) " + condition;
            } else {
                condition = " uuidKey is not null " + condition;
            }

            String sqlTotal = " SELECT COUNT(uuidKey) "
                    + " FROM " + schemaMeta + ".satellite_image_data_analyzed sid WHERE "
                    + condition;

            String sql = " SELECT uuidKey, satelliteName, missionId, productLevel, baseLineNumber, relativeOrbitNumber, tileNumber, originLongitude, originLatitude, cornerLongitude, cornerLatitude, "
                    + " rootDataFolderPath, geoWmsUrl, geoWorkSpace, geoLayerName, captureTime, secondTime, processTime, ingestTime, processStatus, isNoted, dataVendor "
                    + " FROM " + schemaMeta + ".satellite_image_data_analyzed sid WHERE "
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
                sql += " ORDER BY processTime DESC ";
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
            if (req.getDataVendorLst() != null && !req.getDataVendorLst().isEmpty()) {
                query.setParameter("dataVendorLst", req.getDataVendorLst());
                queryTotal.setParameter("dataVendorLst", req.getDataVendorLst());
            }
            if (req.getTileNumberLst() != null && !req.getTileNumberLst().isEmpty()) {
                query.setParameter("tileNumberLst", req.getTileNumberLst());
                queryTotal.setParameter("tileNumberLst", req.getTileNumberLst());
            }
            if (req.getProcessStatusLst() != null && !req.getProcessStatusLst().isEmpty()) {
                query.setParameter("processStatusLst", req.getProcessStatusLst());
                queryTotal.setParameter("processStatusLst", req.getProcessStatusLst());
            }
            if (req.getCommentLst() != null && !req.getCommentLst().isEmpty()) {
                query.setParameter("isNoted", req.getCommentLst());
                queryTotal.setParameter("isNoted", req.getCommentLst());
            }
            if (!StringUtil.isNullOrEmpty(req.getTerm())) {
                String keyword = "%" + req.getTerm().trim() + "%";
                try {
                    query.setParameter("term", req.getTerm());
                    queryTotal.setParameter("term", req.getTerm());
                } catch (Exception e) {
                }
                query.setParameter("keyword", keyword);
                queryTotal.setParameter("keyword", keyword);
            }
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
                    .addScalar("processTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("isNoted", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageDataAnalyzedDTO.class));

            List<SatelliteImageDataAnalyzedDTO> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                results = results.stream()
                        .map(x -> {
                            String imageFilePathLocal = x.getRootDataFolderPath() + "/infor.jpg";
                            x.setImageFilePathLocal(imageFilePathLocal);
                            x.setImageFilePath(imageFilePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));

                            return x;
                        })
                        .collect(Collectors.toList());
            }

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

    public SatelliteImageDataAnalyzed findByUuid(String uuid) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String sql = "SELECT * FROM " + schemaMeta + ".satellite_image_data_analyzed WHERE uuidKey = :uuid ";
            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("uuid", uuid);
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
                    .addScalar("processTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("isNoted", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageDataAnalyzed.class));
            return (SatelliteImageDataAnalyzed) query.getSingleResult();
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

    public SatelliteImageDataAnalyzed noteChange(int isNoted, String uuid) {
        Session session = null;
        String schemaLocal = this.getSchemaMetaLocal();
        try {
            session = this.clickHouseSessionFactory.openSession();
            session.doWork(conn -> {

                PreparedStatement pstmt = null;
                try {
                    String sql = " ALTER TABLE " + schemaLocal + ".satellite_image_data_analyzed ON CLUSTER 'metacen_cluster' UPDATE "
                            + "isNoted = ? "
                            + "where uuidKey = '" + uuid + "'";

                    pstmt = conn.prepareStatement(sql);
                    int index = 1;
                    pstmt.setInt(index++, isNoted);
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
