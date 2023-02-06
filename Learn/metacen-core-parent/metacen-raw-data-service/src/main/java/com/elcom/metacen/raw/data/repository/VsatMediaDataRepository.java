package com.elcom.metacen.raw.data.repository;

import com.elcom.metacen.dto.redis.VsatDataSource;
import com.elcom.metacen.enums.MediaType;
import com.elcom.metacen.enums.MetacenProcessStatus;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.raw.data.config.ApplicationConfig;
import com.elcom.metacen.raw.data.constant.Constant;
import com.elcom.metacen.raw.data.model.dto.AdvanceFilterDTO;
import com.elcom.metacen.raw.data.model.dto.DetailMediaRelationRequestDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallStatisticFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaOverallStatisticResponseDTO;
import com.elcom.metacen.utils.StringUtil;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hibernate.Session;
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
import org.hibernate.type.BigIntegerType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;

@Repository
public class VsatMediaDataRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaDataRepository.class);

    public static final String OPERATOR_IS = "IS";
    public static final String OPERATOR_IS_NOT = "IS_NOT";
    public static final String OPERATOR_IS_ONE_OF = "IS_ONE_OF";
    public static final String OPERATOR_IS_NOT_ONE_OF = "IS_NOT_ONE_OF";
    public static final String OPERATOR_IS_BETWEEN = "IS_BETWEEN";
    public static final String OPERATOR_IS_NOT_BETWEEN = "IS_NOT_BETWEEN";
    public static final String OPERATOR_OR = "OR";
    public static final String OPERATOR_AND = "AND";

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateFormatTime = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public VsatMediaDataRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CustomVsatMediaOverallRepository customVsatMediaOverallRepository;

    public Page<VsatMediaDTO> filterVsatMediaRawData(VsatMediaFilterDTO data) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            Integer page = data.getPage() > 0 ? data.getPage() : 0;
            Pageable pageable = PageRequest.of(page, data.getSize());

            String fromTime = data.getFromTime().trim();
            String toTime = data.getToTime().trim();

            String condition = "";
            if (!StringUtil.isNullOrEmpty(data.getTerm())) {
                condition += " AND ( "
                        + " ilike(sourceName, :term) OR "
                        + " ilike(sourceIp, :term) OR "
                        + " ilike(sourcePhone, :term) OR "
                        + " ilike(destName, :term) OR "
                        + " ilike(destIp, :term) OR "
                        + " ilike(destPhone, :term) OR "
                        + " ilike(mediaTypeName, :term) OR "
                        + " ilike(dataSourceName, :term) "
                        + " ) ";
            }
            if (data.getDataSourceIds() != null && !data.getDataSourceIds().isEmpty()) {
                condition += " AND dataSourceId IN :dataSourceIds ";
            }
            if (data.getMediaTypeIds() != null && !data.getMediaTypeIds().isEmpty()) {
                condition += " AND mediaTypeId IN :mediaTypeIds ";
            }
            if (data.getProcessStatusLst() != null && !data.getProcessStatusLst().isEmpty()) {
                condition += " AND processStatus IN :processStatusLst ";
            }

            // Lọc nâng cao
            List<AdvanceFilterDTO> filterList = data.getFilter();
            condition += advanceFilterCondition(filterList);

            // Lọc theo cột dữ liệu
            if (!StringUtil.isNullOrEmpty(data.getDataVendor())) {
                condition += " AND ilike(dataVendor, :dataVendor) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDataSourceName())) {
                condition += " AND ilike(dataSourceName, :dataSourceName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceIp())) {
                condition += " AND ilike(sourceIp, :sourceIp) ";
            }
            if (data.getSourcePort() != null) {
                condition += " AND sourcePort = :sourcePort ";
            }
            if (!StringUtil.isNullOrEmpty(data.getSourcePhone())) {
                condition += " AND ilike(sourcePhone, :sourcePhone) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceName())) {
                condition += " AND ilike(sourceName, :sourceName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDestIp())) {
                condition += " AND ilike(destIp, :destIp) ";
            }
            if (data.getDestPort() != null) {
                condition += " AND destPort = :destPort ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDestPhone())) {
                condition += " AND ilike(destPhone, :destPhone) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDestName())) {
                condition += " AND ilike(destName, :destName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getMediaTypeName())) {
                condition += " AND ilike(mediaTypeName, :mediaTypeName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getFileType())) {
                condition += " AND ilike(fileType, :fileType) ";
            }
            if (data.getFileSize() != null) {
                condition += " AND fileSize = :fileSize ";
            }

            condition = " (eventTime BETWEEN :fromTime AND :toTime) " + condition;

            String sqlTotal = " SELECT COUNT(uuidKey) FROM metacen.vsat_media WHERE " + condition;

            String sql = " SELECT uuidKey, mediaTypeId, mediaTypeName, sourceId, sourceName, sourceIp, sourcePort, sourcePhone, destId, destName, destIp, destPort, destPhone "
                    + " , filePath, fileName, fileType, fileSize, dataSourceId, dataSourceName, direction, partName, eventTime, ingestTime, processStatus, dataVendor "
                    + " FROM metacen.vsat_media WHERE "
                    + condition;

            // sort
            if (!StringUtil.isNullOrEmpty(data.getSort())) {
                String sortItem = data.getSort().trim();
                if (sortItem.substring(0, 1).equals("-")) {
                    sql += " ORDER BY " + sortItem.substring(1) + " DESC ";
                } else {
                    sql += " ORDER BY " + sortItem;
                }
            } else {
                sql += " ORDER BY eventTime DESC ";
            }

            // limit, offset
            sql += " LIMIT :limit OFFSET :offset ";

            NativeQuery query = session.createSQLQuery(sql)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime)
                    .setParameter("limit", data.getSize())
                    .setParameter("offset", page * data.getSize());
            NativeQuery queryTotal = session.createSQLQuery(sqlTotal)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime);

            if (!StringUtil.isNullOrEmpty(data.getTerm())) {
                String term = "%" + data.getTerm().trim() + "%";
                query.setParameter("term", term);
                queryTotal.setParameter("term", term);
            }
            if (data.getDataSourceIds() != null && !data.getDataSourceIds().isEmpty()) {
                query.setParameter("dataSourceIds", data.getDataSourceIds());
                queryTotal.setParameter("dataSourceIds", data.getDataSourceIds());
            }
            if (data.getMediaTypeIds() != null && !data.getMediaTypeIds().isEmpty()) {
                query.setParameter("mediaTypeIds", data.getMediaTypeIds());
                queryTotal.setParameter("mediaTypeIds", data.getMediaTypeIds());
            }
            if (data.getProcessStatusLst() != null && !data.getProcessStatusLst().isEmpty()) {
                query.setParameter("processStatusLst", data.getProcessStatusLst());
                queryTotal.setParameter("processStatusLst", data.getProcessStatusLst());
            }

            // Lọc nâng cao
            if (filterList != null && !filterList.isEmpty()) {
                int k = 0;
                for (AdvanceFilterDTO item : filterList) {
                    String field = item.getField();
                    String operator = item.getOperator();
                    List<String> value = item.getValue();
                    if (value != null && !value.isEmpty()) {
                        k++;
                        if (operator.equals(OPERATOR_IS_BETWEEN) || operator.equals(OPERATOR_IS_NOT_BETWEEN)) { // Trong khoảng / Ngoài khoảng
                            List<Double> newValue = value.stream().map(Double::parseDouble).collect(Collectors.toList());
                            query.setParameter("from_value_" + k, newValue.get(0));
                            query.setParameter("to_value_" + k, newValue.get(1));

                            if (queryTotal != null) {
                                queryTotal.setParameter("from_value_" + k, newValue.get(0));
                                queryTotal.setParameter("to_value_" + k, newValue.get(1));
                            }
                        } else { // Bằng / Không bằng
                            if (field.equals("Nguồn thu") || field.equals("Loại dữ liệu")) {
                                List<Integer> newValue = new ArrayList<>();
                                for (String s : value) {
                                    if (s.contains(",")) {
                                        String[] arrTmp = s.split(",");
                                        for (String s2 : arrTmp) {
                                            newValue.add(Integer.parseInt(s2));
                                        }
                                    } else {
                                        newValue.add(Integer.parseInt(s));
                                    }
                                }

                                query.setParameter("filter_value_" + k, newValue);

                                if (queryTotal != null) {
                                    queryTotal.setParameter("filter_value_" + k, newValue);
                                }
                            } else if (field.equals("IP nguồn") || field.equals("IP đích")) {
                                String newIp = value.get(0).trim();
                                if (operator.equals(OPERATOR_IS)) {
                                    String[] newIpLst = newIp.split(",");
                                    if (newIpLst.length > 1) {
                                        int j = 0;
                                        for (String ip : newIpLst) {
                                            j++;
                                            String ipTrim = ip.trim().toUpperCase();
                                            if (ipTrim.contains("X")) {
                                                ipTrim = ipTrim.replace("X", "%");
                                            }

                                            query.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                            if (queryTotal != null) {
                                                queryTotal.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                            }
                                        }
                                    } else {
                                        newIp = newIp.toUpperCase();
                                        if (newIp.contains("X")) {
                                            newIp = newIp.replace("X", "%");
                                        }

                                        query.setParameter("ip_value_" + k, newIp);
                                        if (queryTotal != null) {
                                            queryTotal.setParameter("ip_value_" + k, newIp);
                                        }
                                    }
                                } else if (operator.equals(OPERATOR_IS_NOT)) {
                                    String[] newIpLst = newIp.split(",");
                                    int j = 0;
                                    for (String ip : newIpLst) {
                                        j++;

                                        String ipTrim = ip.trim().toUpperCase();
                                        if (ipTrim.contains("X")) {
                                            ipTrim = ipTrim.replace("X", "%");
                                        }

                                        query.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                        if (queryTotal != null) {
                                            queryTotal.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                        }
                                    }
                                }
                            } else {
                                query.setParameter("filter_value_" + k, value);
                                if (queryTotal != null) {
                                    queryTotal.setParameter("filter_value_" + k, value);
                                }
                            }
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(data.getDataVendor())) {
                String keyword = "%" + data.getDataVendor().trim() + "%";
                query.setParameter("dataVendor", keyword);
                queryTotal.setParameter("dataVendor", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getDataSourceName())) {
                String keyword = "%" + data.getDataSourceName().trim() + "%";
                query.setParameter("dataSourceName", keyword);
                queryTotal.setParameter("dataSourceName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceIp())) {
                String keyword = "%" + data.getSourceIp().trim() + "%";
                query.setParameter("sourceIp", keyword);
                queryTotal.setParameter("sourceIp", keyword);
            }
            if (data.getSourcePort() != null) {
                query.setParameter("sourcePort", data.getSourcePort());
                queryTotal.setParameter("sourcePort", data.getSourcePort());
            }
            if (!StringUtil.isNullOrEmpty(data.getSourcePhone())) {
                String keyword = "%" + data.getSourcePhone().trim() + "%";
                query.setParameter("sourcePhone", keyword);
                queryTotal.setParameter("sourcePhone", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceName())) {
                String keyword = "%" + data.getSourceName().trim() + "%";
                query.setParameter("sourceName", keyword);
                queryTotal.setParameter("sourceName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getDestIp())) {
                String keyword = "%" + data.getDestIp().trim() + "%";
                query.setParameter("destIp", keyword);
                queryTotal.setParameter("destIp", keyword);
            }
            if (data.getDestPort() != null) {
                query.setParameter("destPort", data.getDestPort());
                queryTotal.setParameter("destPort", data.getDestPort());
            }
            if (!StringUtil.isNullOrEmpty(data.getDestPhone())) {
                String keyword = "%" + data.getDestPhone().trim() + "%";
                query.setParameter("destPhone", keyword);
                queryTotal.setParameter("destPhone", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getDestName())) {
                String keyword = "%" + data.getDestName().trim() + "%";
                query.setParameter("destName", keyword);
                queryTotal.setParameter("destName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getMediaTypeName())) {
                String keyword = "%" + data.getMediaTypeName().trim() + "%";
                query.setParameter("mediaTypeName", keyword);
                queryTotal.setParameter("mediaTypeName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getFileType())) {
                String keyword = "%" + data.getFileType().trim() + "%";
                query.setParameter("fileType", keyword);
                queryTotal.setParameter("fileType", keyword);
            }
            if (data.getFileSize() != null) {
                query.setParameter("fileSize", data.getFileSize());
                queryTotal.setParameter("fileSize", data.getFileSize());
            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("mediaTypeId", LongType.INSTANCE)
                    .addScalar("mediaTypeName", StringType.INSTANCE)
                    .addScalar("sourceId", BigIntegerType.INSTANCE)
                    .addScalar("sourceName", StringType.INSTANCE)
                    .addScalar("sourceIp", StringType.INSTANCE)
                    .addScalar("sourcePort", LongType.INSTANCE)
                    .addScalar("sourcePhone", StringType.INSTANCE)
                    .addScalar("destId", BigIntegerType.INSTANCE)
                    .addScalar("destName", StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("destPort", LongType.INSTANCE)
                    .addScalar("destPhone", StringType.INSTANCE)
                    .addScalar("filePath", StringType.INSTANCE)
                    .addScalar("fileName", StringType.INSTANCE)
                    .addScalar("fileType", StringType.INSTANCE)
                    .addScalar("fileSize", BigIntegerType.INSTANCE)
                    .addScalar("dataSourceId", LongType.INSTANCE)
                    .addScalar("dataSourceName", StringType.INSTANCE)
                    .addScalar("direction", IntegerType.INSTANCE)
                    .addScalar("partName", LongType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDTO.class));

            List<VsatMediaDTO> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                results = results.stream()
                        .map(this::enrichVsatMedia)
                        .collect(Collectors.toList());
            }

            Object result = queryTotal != null ? queryTotal.getSingleResult() : null;
            return new PageImpl<>(results, pageable, result != null ? ((Number) result).longValue() : 0);
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

    public Page<VsatMediaOverallDTO> filterVsatMediaDataOverall(VsatMediaOverallFilterDTO data) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            Integer page = data.getPage() > 0 ? data.getPage() : 0;
            Pageable pageable = PageRequest.of(page, data.getSize());

            String fromTime = data.getFromTime().trim();
            String toTime = data.getToTime().trim();

            String condition = "";
            if (!StringUtil.isNullOrEmpty(data.getTerm())) {
                condition += " AND ( "
                        + " ilike(sourceName, :term) OR "
                        + " ilike(sourceIp, :term) OR "
                        + " ilike(sourcePhone, :term) OR "
                        + " ilike(destName, :term) OR "
                        + " ilike(destIp, :term) OR "
                        + " ilike(destPhone, :term) OR "
                        + " ilike(mediaTypeName, :term) OR "
                        + " ilike(dataSourceName, :term) "
                        + " ) ";
            }
            if (data.getDataVendorLst() != null && !data.getDataVendorLst().isEmpty()) {
                condition += " AND dataVendor IN :dataVendorLst ";
            }
            if (data.getDataSourceIds() != null && !data.getDataSourceIds().isEmpty()) {
                condition += " AND dataSourceId IN :dataSourceIds ";
            }
            if (data.getMediaTypeIds() != null && !data.getMediaTypeIds().isEmpty()) {
                condition += " AND mediaTypeId IN :mediaTypeIds ";
            }

            // Lọc nâng cao
            List<AdvanceFilterDTO> filterList = data.getFilter();
            condition += advanceFilterCondition(filterList);

            // Lọc theo cột dữ liệu
            if (!StringUtil.isNullOrEmpty(data.getDataVendor())) {
                condition += " AND ilike(dataVendor, :dataVendor) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDataSourceName())) {
                condition += " AND ilike(dataSourceName, :dataSourceName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceIp())) {
                condition += " AND ilike(sourceIp, :sourceIp) ";
            }
            if (data.getSourcePort() != null) {
                condition += " AND sourcePort = :sourcePort ";
            }
            if (data.getSourceId() != null) {
                condition += " AND sourceId = :sourceId ";
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceName())) {
                condition += " AND ilike(sourceName, :sourceName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDestIp())) {
                condition += " AND ilike(destIp, :destIp) ";
            }
            if (data.getDestPort() != null) {
                condition += " AND destPort = :destPort ";
            }
            if (data.getDestId() != null) {
                condition += " AND destId = :destId ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDestName())) {
                condition += " AND ilike(destName, :destName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getMediaTypeName())) {
                condition += " AND ilike(mediaTypeName, :mediaTypeName) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getFileType())) {
                condition += " AND ilike(fileType, :fileType) ";
            }
            if (data.getDirection() != null) {
                condition += " AND direction = :direction ";
            }
            if (!StringUtil.isNullOrEmpty(data.getEventTime())) {
                condition += " AND eventTime = :eventTime ";
            }
            if (!StringUtil.isNullOrEmpty(data.getProcessTime())) {
                condition += " AND processTime = :processTime ";
            }

            condition = " (eventTime BETWEEN :fromTime AND :toTime) " + condition;

            String sqlSelect = " SELECT vm.uuidKey, vm.mediaTypeId, vm.mediaTypeName, vm.sourceId, vm.sourceName, vm.sourceIp, vm.sourcePort, vm.sourcePhone, vm.destId, vm.destName, vm.destIp, vm.destPort, vm.destPhone, "
                    + " vm.filePath, vm.fileName, vm.fileType, vm.fileSize, vm.dataSourceId, vm.dataSourceName, vm.direction, vm.eventTime as eventTime, vm.ingestTime as ingestTime, vm.dataVendor as dataVendor, "
                    + " if(dps.processStatus = 0, -1, dps.processStatus) AS processStatus, if(dps.processStatus = 0, null, dps.ingestTime) AS processTime ";
            String sqlSelectTotal = " SELECT COUNT(if(dps.processStatus = 0, -1, dps.processStatus) AS processStatus) ";

            String sqlCondition = " FROM "
                    + " ( "
                    + "   SELECT uuidKey, mediaTypeId, mediaTypeName, sourceId, sourceName, sourceIp, sourcePort, sourcePhone, destId, destName, destIp, destPort, destPhone, "
                    + "       filePath, fileName, fileType, fileSize, dataSourceId, dataSourceName, direction, eventTime, ingestTime, dataVendor "
                    + "   FROM metacen.vsat_media "
                    + "   WHERE " + condition
                    + " ) as vm "
                    + " LEFT JOIN "
                    + " ( "
                    + "	  SELECT * "
                    + "	  FROM metacen.data_process_status final "
                    + "	  WHERE (eventTime BETWEEN :fromTime AND :toTime) "
                    + " ) as dps "
                    + " ON toString(vm.uuidKey) = dps.recordId ";
            if (data.getProcessStatusLst() != null && !data.getProcessStatusLst().isEmpty()) {
                sqlCondition += " WHERE processStatus IN :processStatusLst ";
            }

            String sql = sqlSelect + sqlCondition;
            String sqlTotal = sqlSelectTotal + sqlCondition;

            // sort
            if (!StringUtil.isNullOrEmpty(data.getSort())) {
                String sortItem = data.getSort().trim();
                if (sortItem.substring(0, 1).equals("-")) {
                    sql += " ORDER BY vm." + sortItem.substring(1) + " DESC ";
                } else {
                    sql += " ORDER BY vm." + sortItem;
                }
            } else {
                sql += " ORDER BY vm.eventTime DESC ";
            }

            // limit, offset
            sql += " LIMIT :limit OFFSET :offset ";

            NativeQuery query = session.createSQLQuery(sql)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime)
                    .setParameter("limit", data.getSize())
                    .setParameter("offset", page * data.getSize());
            NativeQuery queryTotal = session.createSQLQuery(sqlTotal)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime);

            if (!StringUtil.isNullOrEmpty(data.getTerm())) {
                String term = "%" + data.getTerm().trim() + "%";
                query.setParameter("term", term);
                queryTotal.setParameter("term", term);
            }
            if (data.getDataVendorLst() != null && !data.getDataVendorLst().isEmpty()) {
                query.setParameter("dataVendorLst", data.getDataVendorLst());
                queryTotal.setParameter("dataVendorLst", data.getDataVendorLst());
            }
            if (data.getDataSourceIds() != null && !data.getDataSourceIds().isEmpty()) {
                query.setParameter("dataSourceIds", data.getDataSourceIds());
                queryTotal.setParameter("dataSourceIds", data.getDataSourceIds());
            }
            if (data.getMediaTypeIds() != null && !data.getMediaTypeIds().isEmpty()) {
                query.setParameter("mediaTypeIds", data.getMediaTypeIds());
                queryTotal.setParameter("mediaTypeIds", data.getMediaTypeIds());
            }
            if (data.getProcessStatusLst() != null && !data.getProcessStatusLst().isEmpty()) {
                query.setParameter("processStatusLst", data.getProcessStatusLst());
                queryTotal.setParameter("processStatusLst", data.getProcessStatusLst());
            }

            // Lọc nâng cao
            if (filterList != null && !filterList.isEmpty()) {
                int k = 0;
                for (AdvanceFilterDTO item : filterList) {
                    String field = item.getField();
                    String operator = item.getOperator();
                    List<String> value = item.getValue();
                    if (value != null && !value.isEmpty()) {
                        k++;
                        if (operator.equals(OPERATOR_IS_BETWEEN) || operator.equals(OPERATOR_IS_NOT_BETWEEN)) { // Trong khoảng / Ngoài khoảng
                            List<Double> newValue = value.stream().map(Double::parseDouble).collect(Collectors.toList());
                            query.setParameter("from_value_" + k, newValue.get(0));
                            query.setParameter("to_value_" + k, newValue.get(1));

                            if (queryTotal != null) {
                                queryTotal.setParameter("from_value_" + k, newValue.get(0));
                                queryTotal.setParameter("to_value_" + k, newValue.get(1));
                            }
                        } else { // Bằng / Không bằng
                            if (field.equals("Nguồn thu") || field.equals("Loại dữ liệu")) {
                                List<Integer> newValue = new ArrayList<>();
                                for (String s : value) {
                                    if (s.contains(",")) {
                                        String[] arrTmp = s.split(",");
                                        for (String s2 : arrTmp) {
                                            newValue.add(Integer.parseInt(s2));
                                        }
                                    } else {
                                        newValue.add(Integer.parseInt(s));
                                    }
                                }

                                query.setParameter("filter_value_" + k, newValue);

                                if (queryTotal != null) {
                                    queryTotal.setParameter("filter_value_" + k, newValue);
                                }
                            } else if (field.equals("IP nguồn") || field.equals("IP đích")) {
                                String newIp = value.get(0).trim();
                                if (operator.equals(OPERATOR_IS)) {
                                    String[] newIpLst = newIp.split(",");
                                    if (newIpLst.length > 1) {
                                        int j = 0;
                                        for (String ip : newIpLst) {
                                            j++;
                                            String ipTrim = ip.trim().toUpperCase();
                                            if (ipTrim.contains("X")) {
                                                ipTrim = ipTrim.replace("X", "%");
                                            }

                                            query.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                            if (queryTotal != null) {
                                                queryTotal.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                            }
                                        }
                                    } else {
                                        newIp = newIp.toUpperCase();
                                        if (newIp.contains("X")) {
                                            newIp = newIp.replace("X", "%");
                                        }

                                        query.setParameter("ip_value_" + k, newIp);
                                        if (queryTotal != null) {
                                            queryTotal.setParameter("ip_value_" + k, newIp);
                                        }
                                    }
                                } else if (operator.equals(OPERATOR_IS_NOT)) {
                                    String[] newIpLst = newIp.split(",");
                                    int j = 0;
                                    for (String ip : newIpLst) {
                                        j++;

                                        String ipTrim = ip.trim().toUpperCase();
                                        if (ipTrim.contains("X")) {
                                            ipTrim = ipTrim.replace("X", "%");
                                        }

                                        query.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                        if (queryTotal != null) {
                                            queryTotal.setParameter("ip_value_" + k + "_" + j, ipTrim);
                                        }
                                    }
                                }
                            } else {
                                query.setParameter("filter_value_" + k, value);
                                if (queryTotal != null) {
                                    queryTotal.setParameter("filter_value_" + k, value);
                                }
                            }
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(data.getDataVendor())) {
                String keyword = "%" + data.getDataVendor().trim() + "%";
                query.setParameter("dataVendor", keyword);
                queryTotal.setParameter("dataVendor", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getDataSourceName())) {
                String keyword = "%" + data.getDataSourceName().trim() + "%";
                query.setParameter("dataSourceName", keyword);
                queryTotal.setParameter("dataSourceName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceIp())) {
                String keyword = "%" + data.getSourceIp().trim() + "%";
                query.setParameter("sourceIp", keyword);
                queryTotal.setParameter("sourceIp", keyword);
            }
            if (data.getSourcePort() != null) {
                query.setParameter("sourcePort", data.getSourcePort());
                queryTotal.setParameter("sourcePort", data.getSourcePort());
            }
            if (data.getSourceId() != null) {
                query.setParameter("sourceId", data.getSourceId());
                queryTotal.setParameter("sourceId", data.getSourceId());
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceName())) {
                String keyword = "%" + data.getSourceName().trim() + "%";
                query.setParameter("sourceName", keyword);
                queryTotal.setParameter("sourceName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getDestIp())) {
                String keyword = "%" + data.getDestIp().trim() + "%";
                query.setParameter("destIp", keyword);
                queryTotal.setParameter("destIp", keyword);
            }
            if (data.getDestPort() != null) {
                query.setParameter("destPort", data.getDestPort());
                queryTotal.setParameter("destPort", data.getDestPort());
            }
            if (data.getDestId() != null) {
                query.setParameter("destId", data.getDestId());
                queryTotal.setParameter("destId", data.getDestId());
            }
            if (!StringUtil.isNullOrEmpty(data.getDestName())) {
                String keyword = "%" + data.getDestName().trim() + "%";
                query.setParameter("destName", keyword);
                queryTotal.setParameter("destName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getMediaTypeName())) {
                String keyword = "%" + data.getMediaTypeName().trim() + "%";
                query.setParameter("mediaTypeName", keyword);
                queryTotal.setParameter("mediaTypeName", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getFileType())) {
                String keyword = "%" + data.getFileType().trim() + "%";
                query.setParameter("fileType", keyword);
                queryTotal.setParameter("fileType", keyword);
            }
            if (data.getDirection() != null) {
                query.setParameter("direction", data.getDirection());
                queryTotal.setParameter("direction", data.getDirection());
            }
            if (!StringUtil.isNullOrEmpty(data.getEventTime())) {
                Date eventTime = dateFormat.parse(data.getEventTime());
                query.setParameter("eventTime", eventTime);
                queryTotal.setParameter("eventTime", eventTime);
            }
            if (!StringUtil.isNullOrEmpty(data.getProcessTime())) {
                Date processTime = dateFormat.parse(data.getProcessTime());
                query.setParameter("processTime", processTime);
                queryTotal.setParameter("processTime", processTime);
            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("mediaTypeId", LongType.INSTANCE)
                    .addScalar("mediaTypeName", StringType.INSTANCE)
                    .addScalar("sourceId", BigIntegerType.INSTANCE)
                    .addScalar("sourceName", StringType.INSTANCE)
                    .addScalar("sourceIp", StringType.INSTANCE)
                    .addScalar("sourcePort", LongType.INSTANCE)
                    .addScalar("sourcePhone", StringType.INSTANCE)
                    .addScalar("destId", BigIntegerType.INSTANCE)
                    .addScalar("destName", StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("destPort", LongType.INSTANCE)
                    .addScalar("destPhone", StringType.INSTANCE)
                    .addScalar("filePath", StringType.INSTANCE)
                    .addScalar("fileName", StringType.INSTANCE)
                    .addScalar("fileType", StringType.INSTANCE)
                    .addScalar("fileSize", BigIntegerType.INSTANCE)
                    .addScalar("dataSourceId", LongType.INSTANCE)
                    .addScalar("dataSourceName", StringType.INSTANCE)
                    .addScalar("direction", IntegerType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDTO.class));

            List<VsatMediaOverallDTO> vsatMediaOverallList = new ArrayList<>();
            List<VsatMediaDTO> results = query.getResultList();
            if (results == null || results.isEmpty()) {
                return new PageImpl<>(vsatMediaOverallList, pageable, 0);
            }

            // Get media content from Elastic Search
            List<VsatMediaOverallDTO> vsatMediaSuccessLst = new ArrayList<>();
            List<String> vsatMediaIds = results.stream()
                    .filter(item -> item.getProcessStatus() == MetacenProcessStatus.SUCCESS.code())
                    .map(VsatMediaDTO::getUuidKey)
                    .collect(Collectors.toList());
            if (!vsatMediaIds.isEmpty()) {
                vsatMediaSuccessLst = customVsatMediaOverallRepository.findByUuidList(vsatMediaIds);
            }

            //  enrich vsat media overall info
            vsatMediaOverallList = enrichVsatMediaOverall(results, vsatMediaSuccessLst);
            Object result = queryTotal != null ? queryTotal.getSingleResult() : null;
            return new PageImpl<>(vsatMediaOverallList, pageable, ((Number) result).longValue());
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

    public VsatMediaOverallStatisticResponseDTO vsatMediaOverallStatistic(VsatMediaOverallStatisticFilterDTO data) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String fromTime = data.getFromTime().trim();
            String toTime = data.getToTime().trim();

            String condition = "";
            if (data.getSourceId() != null) {
                condition += " AND sourceId = :sourceId ";
            } else {
                return null;
            }

            condition = " (eventTime BETWEEN :fromTime AND :toTime) " + condition;

            String sql = " SELECT countIf(vm.mediaTypeId == 1) as totalAudio, "
                    + " countIf(vm.mediaTypeId == 2) as totalVideo, "
                    + " countIf(vm.mediaTypeId == 3) as totalWeb, "
                    + " countIf(vm.mediaTypeId == 4) as totalEmail, "
                    + " countIf(vm.mediaTypeId == 5) as totalTransferFile, "
                    + " countIf(vm.mediaTypeId == 8) as totalUndefined, "
                    + " countIf(dps.processStatus == 0) as totalRawData, "
                    + " countIf(dps.processStatus != 0) as totalAnalyzedData "
                    + " FROM "
                    + " ( "
                    + "   SELECT uuidKey, mediaTypeId "
                    + "   FROM metacen.vsat_media "
                    + "   WHERE " + condition
                    + " ) as vm "
                    + " LEFT JOIN "
                    + " ( "
                    + "	  SELECT * "
                    + "	  FROM metacen.data_process_status final "
                    + "	  WHERE (eventTime BETWEEN :fromTime AND :toTime) "
                    + " ) as dps "
                    + " ON toString(vm.uuidKey) = dps.recordId ";

            NativeQuery query = session.createSQLQuery(sql)
                    .setParameter("fromTime", fromTime)
                    .setParameter("toTime", toTime);

            if (data.getSourceId() != null) {
                query.setParameter("sourceId", data.getSourceId());
            }

            Object[] temp = (Object[]) query.getSingleResult();
            int audio = 0;
            int video = 0;
            int web = 0;
            int email = 0;
            int transferFile = 0;
            int undefined = 0;
            int rawData = 0;
            int analyzedData = 0;
            int totalData = 0;
            if (temp != null) {
                audio = Integer.parseInt(temp[0].toString());
                video = Integer.parseInt(temp[1].toString());
                web = Integer.parseInt(temp[2].toString());
                email = Integer.parseInt(temp[3].toString());
                transferFile = Integer.parseInt(temp[4].toString());
                undefined = Integer.parseInt(temp[5].toString());
                rawData = Integer.parseInt(temp[6].toString());
                analyzedData = Integer.parseInt(temp[7].toString());
                totalData = rawData + analyzedData;
            }

            VsatMediaOverallStatisticResponseDTO vsatMediaOverallStatisticResponseDTO = new VsatMediaOverallStatisticResponseDTO();
            vsatMediaOverallStatisticResponseDTO.setAudio(audio);
            vsatMediaOverallStatisticResponseDTO.setVideo(video);
            vsatMediaOverallStatisticResponseDTO.setWeb(web);
            vsatMediaOverallStatisticResponseDTO.setEmail(email);
            vsatMediaOverallStatisticResponseDTO.setTransferFile(transferFile);
            vsatMediaOverallStatisticResponseDTO.setUndefined(undefined);
            vsatMediaOverallStatisticResponseDTO.setRawData(rawData);
            vsatMediaOverallStatisticResponseDTO.setAnalyzedData(analyzedData);
            vsatMediaOverallStatisticResponseDTO.setTotalData(totalData);

            return vsatMediaOverallStatisticResponseDTO;
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

    public MessageContent getDetailMediaRelation(DetailMediaRelationRequestDTO data) {
        List<VsatMediaDTO> lstMediaRaw = new ArrayList<>();
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            String condition = "";
            if (data.getPartName() != null && data.getPartName() != 0) {
                condition += " AND partName = :partName ";
            }

            String sql = "SELECT uuidKey, mediaTypeId, mediaTypeName, sourceId, sourceName, sourceIp, sourcePort, sourcePhone, destId, destName, destIp, destPort, destPhone, "
                    + " filePath, fileName, fileType, fileSize, dataSourceId, dataSourceName, direction, partName, eventTime, ingestTime, processStatus, dataVendor "
                    + " FROM metacen.vsat_media WHERE uuidKey IN (:uuidKeyFrom, :uuidKeyTo) ";
            sql = sql + condition;

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("uuidKeyFrom", data.getUuidKeyFrom())
                    .setParameter("uuidKeyTo", data.getUuidKeyTo());
            if (data.getPartName() != null && data.getPartName() != 0) {
                query.setParameter("partName", data.getPartName());
            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("mediaTypeId", LongType.INSTANCE)
                    .addScalar("mediaTypeName", StringType.INSTANCE)
                    .addScalar("sourceId", BigIntegerType.INSTANCE)
                    .addScalar("sourceName", StringType.INSTANCE)
                    .addScalar("sourceIp", StringType.INSTANCE)
                    .addScalar("sourcePort", LongType.INSTANCE)
                    .addScalar("sourcePhone", StringType.INSTANCE)
                    .addScalar("destId", BigIntegerType.INSTANCE)
                    .addScalar("destName", StringType.INSTANCE)
                    .addScalar("destIp", StringType.INSTANCE)
                    .addScalar("destPort", LongType.INSTANCE)
                    .addScalar("destPhone", StringType.INSTANCE)
                    .addScalar("filePath", StringType.INSTANCE)
                    .addScalar("fileName", StringType.INSTANCE)
                    .addScalar("fileType", StringType.INSTANCE)
                    .addScalar("fileSize", BigIntegerType.INSTANCE)
                    .addScalar("dataSourceId", LongType.INSTANCE)
                    .addScalar("dataSourceName", StringType.INSTANCE)
                    .addScalar("direction", IntegerType.INSTANCE)
                    .addScalar("partName", LongType.INSTANCE)
                    .addScalar("eventTime", TimestampType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMediaDTO.class));

            lstMediaRaw = query.getResultList();
            if (lstMediaRaw != null && !lstMediaRaw.isEmpty()) {
                lstMediaRaw = lstMediaRaw.stream()
                        .map(this::enrichVsatMedia)
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.disconnect();
                session.close();
            }
        }

        MessageContent messageContent = new MessageContent();
        messageContent.setData(lstMediaRaw);
        messageContent.setStatus(HttpStatus.OK.value());
        messageContent.setMessage(HttpStatus.OK.toString());

        return messageContent;
    }

    public static void main(String[] args) {
        Long l1 = Long.parseLong("18446744073162492315");
        System.out.println("s: " + l1);
    }

    public List<Long> findObjLstOnMedia(String fromTime, String toTime, List<Integer> mediaTypes, List<String> fileTypes) {

        if (StringUtil.isNullOrEmpty(fromTime) || StringUtil.isNullOrEmpty(toTime)) {
            return null;
        }

        Session session = null;
        try {
            session = this.clickHouseSessionFactory.openSession();

            String condition = " eventTime BETWEEN :fromTime AND :toTime AND sourceId IS NOT NULL AND sourceId > 0 ";

            if (mediaTypes != null && !mediaTypes.isEmpty()) {
                condition += " AND mediaTypeId IN :mediaTypes ";
            }

            if (fileTypes != null && !fileTypes.isEmpty()) {
                condition += " AND fileType IN :fileTypes ";
            }

            String sql = " SELECT DISTINCT sourceId AS objId FROM vsat_media WHERE " + condition;

            NativeQuery query = session.createNativeQuery(sql)
                    .setParameter("fromTime", fromTime.trim())
                    .setParameter("toTime", toTime.trim());
            query.addScalar("objId", LongType.INSTANCE);

            if (mediaTypes != null && !mediaTypes.isEmpty()) {
                query.setParameterList("mediaTypes", mediaTypes);
            }

            if (fileTypes != null && !fileTypes.isEmpty()) {
                query.setParameterList("fileTypes", fileTypes);
            }

            return (List<Long>) query.getResultList();

        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            this.closeSession(session);
        }
        return null;
    }

    private String advanceFilterCondition(List<AdvanceFilterDTO> filterList) {
        String condition = "";
        if (filterList != null && !filterList.isEmpty()) {
            int k = 0;
            for (AdvanceFilterDTO item : filterList) {
                String field = item.getField();
                String fieldSql = convertField(field);
                String operator = item.getOperator();
                List<String> value = item.getValue();
                String unitSql = convertSizeToBytes(item.getUnit());
                if (value != null && !value.isEmpty()) {
                    k++;
                    String operatorSql = convertOperator(operator);
                    if (operator.equals(OPERATOR_IS_BETWEEN)) { // Trong khoảng
                        if (field.equals("Dung lượng")) {
                            if (!StringUtil.isNullOrEmpty(unitSql)) {
                                condition += " AND " + unitSql + operatorSql + " :from_value_" + k + " AND :to_value_" + k;
                            }
                        } else {
                            condition += " AND (toFloat64(" + fieldSql + ") " + operatorSql + " :from_value_" + k + " AND :to_value_" + k + ") ";
                        }
                    } else if (operator.equals(OPERATOR_IS_NOT_BETWEEN)) { // Ngoài khoảng
                        if (field.equals("Dung lượng")) {
                            if (!StringUtil.isNullOrEmpty(unitSql)) {
                                condition += " AND (" + unitSql + operatorSql + " :from_value_" + k + " AND :to_value_" + k + ") ";
                            }
                        } else {
                            condition += " AND (toFloat64(" + fieldSql + ") " + operatorSql + " :from_value_" + k + " AND :to_value_" + k + ") ";
                        }
                    } else { // Bằng / Không bằng
                        if (field.equals("IP nguồn") || field.equals("IP đích")) {
                            String newIp = value.get(0).trim();
                            if (operator.equals(OPERATOR_IS)) {
                                String[] newIpLst = newIp.split(",");
                                if (newIpLst.length > 1) {
                                    int j = 0;
                                    condition += " AND ( ";
                                    for (String ip : newIpLst) {
                                        j++;

                                        String ipTrim = ip.trim().toUpperCase();
                                        if (ipTrim.contains("X")) {
                                            condition += " ilike(" + fieldSql + ", :ip_value_" + k + "_" + j + ") ";
                                        } else {
                                            condition += " " + fieldSql + " = :ip_value_" + k + "_" + j;
                                        }

                                        if (j < newIpLst.length) {
                                            condition += " OR ";
                                        }
                                    }
                                    condition += " ) ";
                                } else {
                                    newIp = newIp.toUpperCase();
                                    if (newIp.contains("X")) {
                                        condition += " AND ilike(" + fieldSql + ", :ip_value_" + k + ") ";
                                    } else {
                                        condition += " AND " + fieldSql + " = :ip_value_" + k;
                                    }
                                }
                            } else if (operator.equals(OPERATOR_IS_NOT)) {
                                String[] newIpLst = newIp.split(",");
                                int j = 0;
                                for (String ip : newIpLst) {
                                    j++;
                                    String ipTrim = ip.trim().toUpperCase();
                                    if (ipTrim.contains("X")) {
                                        condition += " AND NOT ilike(" + fieldSql + ", :ip_value_" + k + "_" + j + ") AND " + fieldSql + " IS NOT NULL AND " + fieldSql + " <> '' ";
                                    } else {
                                        condition += " AND " + fieldSql + " <> :ip_value_" + k + "_" + j + " AND " + fieldSql + " IS NOT NULL AND " + fieldSql + " <> '' ";
                                    }
                                }
                            }
                        } else { // Các trường lọc còn lại
                            if (operator.equals(OPERATOR_IS) || operator.equals(OPERATOR_IS_ONE_OF)) {
                                condition += " AND " + fieldSql + operatorSql + " :filter_value_" + k;
                            } else if (operator.equals(OPERATOR_IS_NOT) || operator.equals(OPERATOR_IS_NOT_ONE_OF)) {
                                condition += " AND (" + fieldSql + operatorSql + " :filter_value_" + k + ") ";
                            }
                        }
                    }
                } else {
                    LOGGER.error("Invalid filter values!");
                }
            }
        }

        return condition;
    }

    private String convertField(String field) {
        String fieldSql = null;
        switch (field) {
            case "Nguồn thu":
                fieldSql = "dataSourceId";
                break;
            case "Loại dữ liệu":
                fieldSql = "mediaTypeId";
                break;
            case "Định dạng":
                fieldSql = "fileType";
                break;
            case "ID nguồn":
                fieldSql = "sourceId";
                break;
            case "ID đích":
                fieldSql = "destId";
                break;
            case "Tên nguồn":
                fieldSql = "sourceName";
                break;
            case "Tên đích":
                fieldSql = "destName";
                break;
            case "IP nguồn":
                fieldSql = "sourceIp";
                break;
            case "IP đích":
                fieldSql = "destIp";
                break;
            case "Port nguồn":
                fieldSql = "sourcePort";
                break;
            case "Port đích":
                fieldSql = "destPort";
                break;
            case "Chiều dữ liệu":
                fieldSql = "direction";
                break;
            case "Dung lượng":
                fieldSql = "fileSize";
                break;
        }

        return fieldSql;
    }

    private String convertSizeToBytes(String unit) {
        String sql = "";
        if (!StringUtil.isNullOrEmpty(unit)) {
            switch (unit) {
                case "Bytes":
                    sql = " round(fileSize, 2) ";
                    break;
                case "KB":
                    sql = " round(fileSize / 1024, 2) ";
                    break;
                case "MB":
                    sql = " round(fileSize / 1048576, 2) ";
                    break;
                case "GB":
                    sql = " round(fileSize / 1073741824, 2) ";
                    break;
                case "TB":
                    sql = " round(fileSize / 1099511627776, 2) ";
                    break;
                case "PB":
                    sql = " round(fileSize / 1125899906842624, 2) ";
                    break;
                case "EB":
                    sql = " round(fileSize / 1152921504606846976, 2) ";
                    break;
                case "ZB":
                    sql = " round(fileSize / 1180591620717411303424, 2) ";
                    break;
                case "YB":
                    sql = " round(fileSize / 1208925819614629174706176, 2) ";
                    break;
            }
        }
        return sql;
    }

    private String convertOperator(String operator) {
        String operatorSql = "";
        if (operator.equals(OPERATOR_IS) || operator.equals(OPERATOR_IS_ONE_OF)) {
            operatorSql = " IN ";
        } else if (operator.equals(OPERATOR_IS_NOT) || operator.equals(OPERATOR_IS_NOT_ONE_OF)) {
            operatorSql = " NOT IN ";
        } else if (operator.equals(OPERATOR_IS_BETWEEN)) {
            operatorSql = " BETWEEN ";
        } else if (operator.equals(OPERATOR_IS_NOT_BETWEEN)) {
            operatorSql = " NOT BETWEEN ";
        }

        return operatorSql;
    }

    private VsatMediaDTO enrichVsatMedia(VsatMediaDTO vsatMediaDTO) {
//        List<VsatDataSource> vsatDataSourceLst = new ArrayList<>();
//        String key = Constant.REDIS_VSAT_DATA_SOURCE_LST_KEY;
//        if (this.redisTemplate.hasKey(key)) {
//            vsatDataSourceLst = (List<VsatDataSource>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VSAT_DATA_SOURCE_LST_FETCH_MAX);
//        }
//
//        String sourceName = "";
//        String destName = "";
//        if (vsatDataSourceLst != null && !vsatDataSourceLst.isEmpty()) {
//            if (vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaDTO.getSourceId())).findFirst().isPresent()) {
//                sourceName = vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaDTO.getSourceId())).findFirst().get().getDataSourceName();
//            }
//            if (vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaDTO.getDestId())).findFirst().isPresent()) {
//                destName = vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaDTO.getDestId())).findFirst().get().getDataSourceName();
//            }
//        }
//        vsatMediaDTO.setSourceName(sourceName);
//        vsatMediaDTO.setDestName(destName);
        vsatMediaDTO.setFilePathLocal(vsatMediaDTO.getFilePath());
        vsatMediaDTO.setFilePath(vsatMediaDTO.getFilePath().replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, ApplicationConfig.MEDIA_LINK_ROOT_API));

        return vsatMediaDTO;
    }

    private List<VsatMediaOverallDTO> enrichVsatMediaOverall(List<VsatMediaDTO> vsatMediaDTOLst, List<VsatMediaOverallDTO> vsatMediaSuccessLst) {
        List<VsatMediaOverallDTO> results = new ArrayList<>();
        for (VsatMediaDTO vsatMediaDTO : vsatMediaDTOLst) {
            String fileContentUtf8 = "";
            String fileContentGB18030 = "";
            String mailFrom = "";
            String mailReplyTo = "";
            String mailTo = "";
            String mailAttachments = "";
            String mailContents = "";
            String mailSubject = "";
            String mailScanVirus = "";
            String mailScanResult = "";
            String mailUserAgent = "";
            String mailContentLanguage = "";
            String mailXMail = "";
            String mailRaw = "";
            if ((vsatMediaSuccessLst != null && !vsatMediaSuccessLst.isEmpty())
                    && vsatMediaDTO.getProcessStatus() == MetacenProcessStatus.SUCCESS.code()) {
                VsatMediaOverallDTO vsatMediaSuccess = vsatMediaSuccessLst
                        .stream()
                        .filter(item -> item.getId().equals(vsatMediaDTO.getUuidKey()))
                        .findFirst()
                        .orElse(null);
                if (vsatMediaSuccess != null) {
                    fileContentUtf8 = vsatMediaSuccess.getFileContentUtf8();
                    fileContentGB18030 = vsatMediaSuccess.getFileContentGB18030();
                    mailFrom = vsatMediaSuccess.getMailFrom();
                    mailReplyTo = vsatMediaSuccess.getMailReplyTo();
                    mailTo = vsatMediaSuccess.getMailTo();
                    mailAttachments = vsatMediaSuccess.getMailAttachments();
                    mailContents = vsatMediaSuccess.getMailContents();
                    mailSubject = vsatMediaSuccess.getMailSubject();
                    mailScanVirus = vsatMediaSuccess.getMailScanVirus();
                    mailScanResult = vsatMediaSuccess.getMailScanResult();
                    mailUserAgent = vsatMediaSuccess.getMailUserAgent();
                    mailContentLanguage = vsatMediaSuccess.getMailContentLanguage();
                    mailXMail = vsatMediaSuccess.getMailXMail();
                    mailRaw = vsatMediaSuccess.getMailRaw();
                }
            }

            VsatMediaOverallDTO vsatMediaOverallDTO = VsatMediaOverallDTO.builder()
                    .id(vsatMediaDTO.getUuidKey())
                    .vsatMediaUuidKey(vsatMediaDTO.getUuidKey())
                    .mediaTypeId(vsatMediaDTO.getMediaTypeId())
                    .mediaTypeName(vsatMediaDTO.getMediaTypeName())
                    .sourceId(vsatMediaDTO.getSourceId())
                    .sourceName(vsatMediaDTO.getSourceName())
                    .sourceIp(vsatMediaDTO.getSourceIp())
                    .sourcePort(vsatMediaDTO.getSourcePort())
                    .destId(vsatMediaDTO.getDestId())
                    .destName(vsatMediaDTO.getDestName())
                    .destIp(vsatMediaDTO.getDestIp())
                    .destPort(vsatMediaDTO.getDestPort())
                    .filePathLocal(vsatMediaDTO.getFilePath())
                    .filePath(vsatMediaDTO.getFilePath().replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, ApplicationConfig.MEDIA_LINK_ROOT_API))
                    .fileType(vsatMediaDTO.getFileType())
                    .fileSize(vsatMediaDTO.getFileSize())
                    .fileContentUtf8(fileContentUtf8)
                    .fileContentGB18030(fileContentGB18030)
                    .mailFrom(mailFrom)
                    .mailReplyTo(mailReplyTo)
                    .mailTo(mailTo)
                    .mailAttachments(mailAttachments)
                    .mailContents(mailContents)
                    .mailSubject(mailSubject)
                    .mailScanVirus(mailScanVirus)
                    .mailScanResult(mailScanResult)
                    .mailUserAgent(mailUserAgent)
                    .mailContentLanguage(mailContentLanguage)
                    .mailXMail(mailXMail)
                    .mailRaw(mailRaw)
                    .dataSourceId(vsatMediaDTO.getDataSourceId())
                    .dataSourceName(vsatMediaDTO.getDataSourceName())
                    .direction(vsatMediaDTO.getDirection())
                    .dataVendor(vsatMediaDTO.getDataVendor())
                    .analyzedEngine("MVMA-01")
                    .eventTime(vsatMediaDTO.getEventTime())
                    .ingestTime(vsatMediaDTO.getIngestTime())
                    .processTime(vsatMediaDTO.getProcessTime())
                    .processStatus(vsatMediaDTO.getProcessStatus())
                    .build();

            results.add(vsatMediaOverallDTO);
        };

        return results;
    }

}
