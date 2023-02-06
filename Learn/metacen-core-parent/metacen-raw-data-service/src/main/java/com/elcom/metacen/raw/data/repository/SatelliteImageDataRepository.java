package com.elcom.metacen.raw.data.repository;

import com.elcom.metacen.raw.data.config.ApplicationConfig;
import com.elcom.metacen.raw.data.model.SatelliteImageData;
import com.elcom.metacen.raw.data.model.dto.SatelliteImageDataDTO;
import com.elcom.metacen.raw.data.model.dto.SatelliteImageDataFilterDTO;
import com.elcom.metacen.utils.DateUtils;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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

    public Page<SatelliteImageDataDTO> filterSatelliteImageData(SatelliteImageDataFilterDTO req) {
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

            if (!StringUtil.isNullOrEmpty(req.getDataVendor())) {
                condition += " AND ilike(dataVendor, :dataVendor) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getSatelliteName())) {
                condition += " AND ilike(satelliteName, :satelliteName) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getCoordinates())) {
                condition += " AND ( "
                        + " ilike(toString(originLongitude), :coordinates) OR "
                        + " ilike(toString(originLatitude), :coordinates) OR "
                        + " ilike(toString(cornerLongitude), :coordinates) OR "
                        + " ilike(toString(cornerLatitude), :coordinates) "
                        + " ) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getCaptureTime())) {
                condition += " AND (captureTime BETWEEN :captureFromTime AND :captureToTime) ";
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
                condition = " (captureTime BETWEEN :fromTime AND :toTime) " + condition;
            } else {
                condition = " uuidKey is not null " + condition;
            }

            String sqlTotal = " SELECT COUNT(uuidKey) "
                    + " FROM " + schemaMeta + ".satellite_image_data sid WHERE "
                    + condition;

            String sql = " SELECT uuidKey, satelliteName, missionId, productLevel, baseLineNumber, relativeOrbitNumber, tileNumber, originLongitude, originLatitude, cornerLongitude, cornerLatitude, "
                    + " rootDataFolderPath, geoWmsUrl, geoWorkSpace, geoLayerName, captureTime, secondTime, ingestTime, processStatus, dataVendor "
                    + " FROM " + schemaMeta + ".satellite_image_data sid WHERE "
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
                    sql += " ORDER BY captureTime DESC,ingestTime ";
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

            if (!StringUtil.isNullOrEmpty(req.getDataVendor())) {
                String keyword = "%" + req.getDataVendor().trim() + "%";
                query.setParameter("dataVendor", keyword);
                queryTotal.setParameter("dataVendor", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getSatelliteName())) {
                String keyword = "%" + req.getSatelliteName().trim() + "%";
                query.setParameter("satelliteName", keyword);
                queryTotal.setParameter("satelliteName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getCoordinates())) {
                String keyword = "%" + req.getCoordinates().trim() + "%";
                query.setParameter("coordinates", keyword);
                queryTotal.setParameter("coordinates", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getCaptureTime())) {
                query.setParameter("captureFromTime", DateUtils.parse(req.getCaptureTime() + " 00:00:00"));
                queryTotal.setParameter("captureFromTime", DateUtils.parse(req.getCaptureTime() + " 00:00:00"));

                query.setParameter("captureToTime", DateUtils.parse(req.getCaptureTime() + " 23:59:59"));
                queryTotal.setParameter("captureToTime", DateUtils.parse(req.getCaptureTime() + " 23:59:59"));
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
                    .addScalar("originLongitude", BigDecimalType.INSTANCE)
                    .addScalar("originLatitude", BigDecimalType.INSTANCE)
                    .addScalar("cornerLongitude", BigDecimalType.INSTANCE)
                    .addScalar("cornerLatitude", BigDecimalType.INSTANCE)
                    .addScalar("rootDataFolderPath", StringType.INSTANCE)
                    .addScalar("geoWmsUrl", StringType.INSTANCE)
                    .addScalar("geoWorkSpace", StringType.INSTANCE)
                    .addScalar("geoLayerName", StringType.INSTANCE)
                    .addScalar("captureTime", TimestampType.INSTANCE)
                    .addScalar("secondTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageDataDTO.class));

            List<SatelliteImageDataDTO> results = query.getResultList();
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

    public SatelliteImageData findByUuid(String uuid) {
        Session session = this.clickHouseSessionFactory.openSession();
        String schemaMeta = this.getSchemaMeta();
        try {
            String sql = "SELECT * FROM " + schemaMeta + ".satellite_image_data WHERE uuidKey = :uuid";
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
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageData.class));
            return (SatelliteImageData) query.getSingleResult();
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

    public Page<SatelliteImageDataDTO> filterSatelliteImageDataForMap(SatelliteImageDataFilterDTO req) {
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

            if (!StringUtil.isNullOrEmpty(req.getDataVendor())) {
                condition += " AND ilike(dataVendor, :dataVendor) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getSatelliteName())) {
                condition += " AND ilike(satelliteName, :satelliteName) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getCoordinates())) {
                condition += " AND ( "
                        + " ilike(toString(originLongitude), :coordinates) OR "
                        + " ilike(toString(originLatitude), :coordinates) OR "
                        + " ilike(toString(cornerLongitude), :coordinates) OR "
                        + " ilike(toString(cornerLatitude), :coordinates) "
                        + " ) ";
            }
            if (!StringUtil.isNullOrEmpty(req.getCaptureTime())) {
                condition += " AND ( captureTime BETWEEN :captureFromTime AND :captureToTime ) ";
            }

            if (!StringUtil.isNullOrEmpty(req.getFromTime()) && !StringUtil.isNullOrEmpty(req.getToTime())) {
                condition = " ( captureTime BETWEEN :fromTime AND :toTime ) " + condition;
            } else {
                condition = " uuidKey is not null " + condition;
            }

            String columnFetch = " uuidKey, satelliteName, missionId, productLevel, baseLineNumber, relativeOrbitNumber, tileNumber, originLongitude, originLatitude, cornerLongitude, cornerLatitude,  rootDataFolderPath, geoWmsUrl, geoWorkSpace, geoLayerName, captureTime, secondTime, ingestTime, processStatus, dataVendor ";

            String sql = " SELECT " + columnFetch
                    + " FROM "
                    + "  ( SELECT * FROM "
                    + "     ( SELECT uuidKey, tileNumber, captureTime, ingestTime, row_number() OVER ( PARTITION BY tileNumber ORDER BY captureTime DESC, ingestTime DESC ) AS rank "
                    + "      FROM " + schemaMeta + ".satellite_image_data "
                    + "      WHERE " + condition + " SETTINGS allow_experimental_window_functions = 1 ) "
                    + "   WHERE rank = 1 ) r "
                    + " INNER JOIN "
                    + "  ( SELECT " + columnFetch
                    + "   FROM " + schemaMeta + ".satellite_image_data "
                    + "   WHERE " + condition + " ) t "
                    + //"   ON ( r.tileNumber = t.tileNumber AND r.captureTime = t.captureTime AND r.ingestTime = t.ingestTime ) " +
                    "   ON r.uuidKey = t.uuidKey LIMIT :limit OFFSET 0 ";

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
            if (!StringUtil.isNullOrEmpty(req.getDataVendor())) {
                String keyword = "%" + req.getDataVendor().trim() + "%";
                query.setParameter("dataVendor", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getSatelliteName())) {
                String keyword = "%" + req.getSatelliteName().trim() + "%";
                query.setParameter("satelliteName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getCoordinates())) {
                String keyword = "%" + req.getCoordinates().trim() + "%";
                query.setParameter("coordinates", keyword);
            }
            if (!StringUtil.isNullOrEmpty(req.getCaptureTime())) {
                query.setParameter("captureFromTime", DateUtils.parse(req.getCaptureTime() + " 00:00:00"));
                query.setParameter("captureToTime", DateUtils.parse(req.getCaptureTime() + " 23:59:59"));
            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("satelliteName", StringType.INSTANCE)
                    .addScalar("missionId", StringType.INSTANCE)
                    .addScalar("productLevel", StringType.INSTANCE)
                    .addScalar("baseLineNumber", StringType.INSTANCE)
                    .addScalar("relativeOrbitNumber", StringType.INSTANCE)
                    .addScalar("tileNumber", StringType.INSTANCE)
                    .addScalar("originLongitude", BigDecimalType.INSTANCE)
                    .addScalar("originLatitude", BigDecimalType.INSTANCE)
                    .addScalar("cornerLongitude", BigDecimalType.INSTANCE)
                    .addScalar("cornerLatitude", BigDecimalType.INSTANCE)
                    .addScalar("rootDataFolderPath", StringType.INSTANCE)
                    .addScalar("geoWmsUrl", StringType.INSTANCE)
                    .addScalar("geoWorkSpace", StringType.INSTANCE)
                    .addScalar("geoLayerName", StringType.INSTANCE)
                    .addScalar("captureTime", TimestampType.INSTANCE)
                    .addScalar("secondTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(SatelliteImageDataDTO.class));

            List<SatelliteImageDataDTO> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                results = results.stream()
                        .map(x -> {
                            String imageFilePathLocal = x.getRootDataFolderPath() + "/infor.jpg";
                            x.setImageFilePathLocal(imageFilePathLocal);
                            x.setImageFilePath(imageFilePathLocal.replace(ApplicationConfig.SATELLITE_ROOT_FOLDER_INTERNAL, ApplicationConfig.SATELLITE_MEDIA_LINK_ROOT_API));
                            return x;
                        })
                        .collect(toList());
            }

            return new PageImpl<>(results, PageRequest.of(0, req.getSize()), results != null ? (long) results.size() : 0L);
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
