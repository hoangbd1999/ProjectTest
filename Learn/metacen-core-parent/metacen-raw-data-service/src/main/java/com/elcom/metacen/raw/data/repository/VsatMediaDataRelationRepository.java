package com.elcom.metacen.raw.data.repository;

import com.elcom.metacen.dto.redis.VsatDataSource;
import com.elcom.metacen.raw.data.constant.Constant;
import com.elcom.metacen.raw.data.model.VsatMediaRelation;
import com.elcom.metacen.raw.data.model.dto.AdvanceFilterDTO;
import com.elcom.metacen.raw.data.model.dto.VsatMediaRelationFilterDTO;
import com.elcom.metacen.utils.StringUtil;
import java.util.ArrayList;
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
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

@Repository
public class VsatMediaDataRelationRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VsatMediaDataRelationRepository.class);

    public static final String OPERATOR_IS = "IS";
    public static final String OPERATOR_IS_NOT = "IS_NOT";
    public static final String OPERATOR_IS_ONE_OF = "IS_ONE_OF";
    public static final String OPERATOR_IS_NOT_ONE_OF = "IS_NOT_ONE_OF";
    public static final String OPERATOR_IS_BETWEEN = "IS_BETWEEN";
    public static final String OPERATOR_IS_NOT_BETWEEN = "IS_NOT_BETWEEN";

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    @Autowired
    public VsatMediaDataRelationRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    public Page<VsatMediaRelation> filterVsatMediaRelationRawData(VsatMediaRelationFilterDTO data) {
        Session session = this.clickHouseSessionFactory.openSession();
        try {
            Integer page = data.getPage() > 0 ? data.getPage() : 0;
            Pageable pageable = PageRequest.of(page, data.getSize());

            String fromTime = data.getFromTime().trim();
            String toTime = data.getToTime().trim();

            String condition = "";
            if (!StringUtil.isNullOrEmpty(data.getTerm())) {
                condition += " AND ( "
                        + " ilike(sourceIpTo, :term) OR "
                        + " ilike(destIpTo, :term) OR "
                        + " ilike(sourceIpFrom, :term) OR "
                        + " ilike(destIpFrom, :term) OR "
                        + " ilike(mediaTypeNameFrom, :term) OR "
                        + " ilike(mediaTypeNameTo, :term) OR "
                        + " ilike(fileTypeFrom, :term) OR "
                        + " ilike(fileTypeTo, :term) OR "
                        + " ilike(dataVendor, :term) "
                        + " ) ";
            }
            if (data.getDataSourceIds() != null && !data.getDataSourceIds().isEmpty()) {
                condition += " AND (dataSourceFrom IN :dataSourceIds OR dataSourceTo IN :dataSourceIds) ";
            }
            if (data.getMediaTypeIds() != null && !data.getMediaTypeIds().isEmpty()) {
                condition += " AND (mediaTypeIdFrom IN :mediaTypeIds OR mediaTypeIdTo IN :mediaTypeIds) ";
            }
            if (data.getProcessStatusLst() != null && !data.getProcessStatusLst().isEmpty()) {
                condition += " AND processStatus IN :processStatusLst ";
            }

            // Lọc theo cột dữ liệu
            if (!StringUtil.isNullOrEmpty(data.getDataVendor())) {
                condition += " AND ilike(dataVendor, :dataVendor) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDataSourceName())) {
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceIp())) {
                condition += " AND (ilike(sourceIpFrom, :sourceIp) OR ilike(sourceIpTo, :sourceIp)) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceName())) {
            }
            if (!StringUtil.isNullOrEmpty(data.getDestIp())) {
                condition += " AND (ilike(destIpFrom, :destIp) OR ilike(destIpTo, :destIp)) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getDestName())) {
            }
            if (!StringUtil.isNullOrEmpty(data.getMediaTypeName())) {
                condition += " AND (ilike(mediaTypeNameFrom, :mediaTypeName) OR ilike(mediaTypeNameTo, :mediaTypeName)) ";
            }
            if (!StringUtil.isNullOrEmpty(data.getFileType())) {
                condition += " AND (ilike(fileTypeFrom, :fileType) OR ilike(fileTypeTo, :fileType)) ";
            }
            if (data.getFileSize() != null) {
                condition += " AND (fileSizeFrom = :fileSize OR fileSizeTo = :fileSize) ";
            }
            if (data.getDirection() != null) {
                condition += " AND (directionFrom = :direction OR directionTo = :direction) ";
            }

            // Lọc nâng cao
            List<AdvanceFilterDTO> filterList = data.getFilter();
            condition += advanceFilterCondition(filterList);

            condition = " ( (eventTimeFrom BETWEEN :fromTime AND :toTime) OR (eventTimeTo BETWEEN :fromTime AND :toTime) ) " + condition;

            String sqlTotal = "SELECT COUNT(uuidKey) "
                    + " FROM metacen.vsat_media_relation WHERE "
                    + condition;

            String sql = "SELECT uuidKey, uuidKeyFrom, eventTimeFrom, mediaTypeIdFrom, mediaTypeNameFrom, directionFrom, fileSizeFrom, fileTypeFrom, dataSourceFrom, sourceIdFrom, destIdFrom, sourceIpFrom, destIpFrom, partNameFrom, "
                    + "uuidKeyTo, eventTimeTo, mediaTypeIdTo, mediaTypeNameTo, directionTo, fileSizeTo, fileTypeTo, dataSourceTo, sourceIdTo, destIdTo, sourceIpTo, destIpTo, partNameTo, "
                    + "partName, ingestTime, processStatus, dataVendor "
                    + "FROM metacen.vsat_media_relation WHERE "
                    + condition;

            // sort
            sql += " ORDER BY ingestTime DESC ";

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
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceIp())) {
                String keyword = "%" + data.getSourceIp().trim() + "%";
                query.setParameter("sourceIp", keyword);
                queryTotal.setParameter("sourceIp", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceName())) {
            }
            if (!StringUtil.isNullOrEmpty(data.getDestIp())) {
                String keyword = "%" + data.getDestIp().trim() + "%";
                query.setParameter("destIp", keyword);
                queryTotal.setParameter("destIp", keyword);
            }
            if (!StringUtil.isNullOrEmpty(data.getDestName())) {
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
            if (data.getDirection() != null) {
                query.setParameter("direction", data.getDirection());
                queryTotal.setParameter("direction", data.getDirection());
            }

            query.addScalar("uuidKey", StringType.INSTANCE)
                    .addScalar("uuidKeyFrom", StringType.INSTANCE)
                    .addScalar("eventTimeFrom", TimestampType.INSTANCE)
                    .addScalar("mediaTypeIdFrom", IntegerType.INSTANCE)
                    .addScalar("mediaTypeNameFrom", StringType.INSTANCE)
                    .addScalar("fileTypeFrom", StringType.INSTANCE)
                    .addScalar("directionFrom", IntegerType.INSTANCE)
                    .addScalar("fileSizeFrom", BigDecimalType.INSTANCE)
                    .addScalar("dataSourceFrom", LongType.INSTANCE)
                    // .addScalar("dataSourceNameFrom", StringType.INSTANCE)
                    .addScalar("sourceIdFrom", LongType.INSTANCE)
                    .addScalar("destIdFrom", LongType.INSTANCE)
                    // .addScalar("sourceNameFrom", StringType.INSTANCE)
                    // .addScalar("destNameFrom", StringType.INSTANCE)
                    .addScalar("sourceIpFrom", StringType.INSTANCE)
                    .addScalar("destIpFrom", StringType.INSTANCE)
                    .addScalar("partNameFrom", LongType.INSTANCE)
                    .addScalar("uuidKeyTo", StringType.INSTANCE)
                    .addScalar("eventTimeTo", TimestampType.INSTANCE)
                    .addScalar("mediaTypeIdTo", IntegerType.INSTANCE)
                    .addScalar("mediaTypeNameTo", StringType.INSTANCE)
                    .addScalar("fileTypeTo", StringType.INSTANCE)
                    .addScalar("directionTo", IntegerType.INSTANCE)
                    .addScalar("fileSizeTo", BigDecimalType.INSTANCE)
                    .addScalar("dataSourceTo", LongType.INSTANCE)
                    // .addScalar("dataSourceNameTo", StringType.INSTANCE)
                    .addScalar("sourceIdTo", LongType.INSTANCE)
                    .addScalar("destIdTo", LongType.INSTANCE)
                    // .addScalar("sourceNameTo", StringType.INSTANCE)
                    // .addScalar("destNameTo", StringType.INSTANCE)
                    .addScalar("sourceIpTo", StringType.INSTANCE)
                    .addScalar("destIpTo", StringType.INSTANCE)
                    .addScalar("partNameTo", LongType.INSTANCE)
                    .addScalar("partName", LongType.INSTANCE)
                    .addScalar("ingestTime", TimestampType.INSTANCE)
                    .addScalar("processStatus", IntegerType.INSTANCE)
                    .addScalar("dataVendor", StringType.INSTANCE)
                    .setResultTransformer(Transformers.aliasToBean(VsatMediaRelation.class));

            List<VsatMediaRelation> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                results = results.stream()
                        .map(this::enrichVsatMediaRelation)
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

    private VsatMediaRelation enrichVsatMediaRelation(VsatMediaRelation vsatMediaRelation) {
        vsatMediaRelation.setSourceNameFrom("");
        vsatMediaRelation.setDestNameFrom("");
        vsatMediaRelation.setSourceNameTo("");
        vsatMediaRelation.setDestNameTo("");

        List<VsatDataSource> vsatDataSourceLst = new ArrayList<>();
        String key = Constant.REDIS_VSAT_DATA_SOURCE_LST_KEY;
        if (this.redisTemplate.hasKey(key)) {
            vsatDataSourceLst = (List<VsatDataSource>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VSAT_DATA_SOURCE_LST_FETCH_MAX);
        }

        String dataSourceNameFrom = "";
        String dataSourceNameTo = "";
        if (vsatDataSourceLst != null && !vsatDataSourceLst.isEmpty()) {
            if (vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaRelation.getDataSourceFrom())).findFirst().isPresent()) {
                dataSourceNameFrom = vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaRelation.getDataSourceFrom())).findFirst().get().getDataSourceName();
            }
            if (vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaRelation.getDataSourceTo())).findFirst().isPresent()) {
                dataSourceNameTo = vsatDataSourceLst.parallelStream().filter(v -> Objects.equals(v.getDataSourceId(), vsatMediaRelation.getDataSourceTo())).findFirst().get().getDataSourceName();
            }
        }
        vsatMediaRelation.setDataSourceNameFrom(dataSourceNameFrom);
        vsatMediaRelation.setDataSourceNameTo(dataSourceNameTo);

        return vsatMediaRelation;
    }

    private String advanceFilterCondition(List<AdvanceFilterDTO> filterList) {
        String condition = "";
        if (filterList != null && !filterList.isEmpty()) {
            int k = 0;
            for (AdvanceFilterDTO item : filterList) {
                String field = item.getField();
                String operator = item.getOperator();
                List<String> value = item.getValue();

                if (value != null && !value.isEmpty()) {
                    k++;
                    String operatorSql = convertOperator(operator);
                    if (operator.equals(OPERATOR_IS_BETWEEN) || operator.equals(OPERATOR_IS_NOT_BETWEEN)) { // Trong khoảng / Ngoài khoảng
                        if (field.equals("Dung lượng") && !StringUtil.isNullOrEmpty(item.getUnit())) {
                            String fieldUnitFrom = convertUnitFileSizeFrom(item.getUnit());
                            String fieldUnitTo = convertUnitFileSizeTo(item.getUnit());
                            condition += " AND ( "
                                    + " (" + fieldUnitFrom + operatorSql + " :from_value_" + k + " AND :to_value_" + k + ") "
                                    + " OR (" + fieldUnitTo + operatorSql + " :from_value_" + k + " AND :to_value_" + k + ") "
                                    + " ) ";
                        }
                    } else { // Bằng / Không bằng
                        String fromFieldSql = "";
                        String toFieldSql = "";
                        if (field.equals("IP nguồn") || field.equals("IP đích")) {
                            if (field.equals("IP nguồn")) {
                                fromFieldSql = "sourceIpFrom";
                                toFieldSql = "sourceIpTo";
                            } else if (field.equals("IP đích")) {
                                fromFieldSql = "destIpFrom";
                                toFieldSql = "destIpTo";
                            }

                            String newIp = value.get(0).trim();
                            if (operator.equals(OPERATOR_IS)) {
                                String[] newIpLst = newIp.split(",");
                                if (newIpLst.length > 1) {
                                    int j = 0;
                                    int q = 0;
                                    condition += " AND ( ( ";
                                    for (String ip : newIpLst) {
                                        j++;

                                        String ipTrim = ip.trim().toUpperCase();
                                        if (ipTrim.contains("X")) {
                                            condition += " ilike(" + fromFieldSql + ", :ip_value_" + k + "_" + j + ") ";
                                        } else {
                                            condition += " " + fromFieldSql + " = :ip_value_" + k + "_" + j;
                                        }

                                        if (j < newIpLst.length) {
                                            condition += " OR ";
                                        }
                                    }
                                    condition += " ) OR ( ";
                                    for (String ip : newIpLst) {
                                        q++;

                                        String ipTrim = ip.trim().toUpperCase();
                                        if (ipTrim.contains("X")) {
                                            condition += " ilike(" + toFieldSql + ", :ip_value_" + k + "_" + q + ") ";
                                        } else {
                                            condition += " " + toFieldSql + " = :ip_value_" + k + "_" + q;
                                        }

                                        if (q < newIpLst.length) {
                                            condition += " OR ";
                                        }
                                    }
                                    condition += " ) ) ";
                                } else {
                                    newIp = newIp.toUpperCase();
                                    if (newIp.contains("X")) {
                                        condition += " AND ( "
                                                + " ilike(" + fromFieldSql + ", :ip_value_" + k + ") "
                                                + " OR ilike(" + toFieldSql + ", :ip_value_" + k + ") "
                                                + " ) ";
                                    } else {
                                        condition += " AND ( "
                                                + fromFieldSql + " = :ip_value_" + k
                                                + " OR " + toFieldSql + " = :ip_value_" + k
                                                + " ) ";
                                    }
                                }
                            } else if (operator.equals(OPERATOR_IS_NOT)) {
                                String[] newIpLst = newIp.split(",");
                                int j = 0;
                                for (String ip : newIpLst) {
                                    j++;
                                    String ipTrim = ip.trim().toUpperCase();
                                    if (ipTrim.contains("X")) {
                                        condition += " AND (NOT ilike(" + fromFieldSql + ", :ip_value_" + k + "_" + j + ") AND " + fromFieldSql + " IS NOT NULL AND " + fromFieldSql + " <> '' ) ";
                                        condition += " AND (NOT ilike(" + toFieldSql + ", :ip_value_" + k + "_" + j + ") AND " + toFieldSql + " IS NOT NULL AND " + toFieldSql + " <> '' ) ";
                                    } else {
                                        condition += " AND ( " + fromFieldSql + " <> :ip_value_" + k + "_" + j + " AND " + fromFieldSql + " IS NOT NULL AND " + fromFieldSql + " <> '' ) ";
                                        condition += " AND ( " + toFieldSql + " <> :ip_value_" + k + "_" + j + " AND " + toFieldSql + " IS NOT NULL AND " + toFieldSql + " <> '' ) ";
                                    }
                                }
                            }
                        } else if (field.equals("Nguồn thu") || field.equals("Loại dữ liệu") || field.equals("Định dạng")) {
                            if (field.equals("Nguồn thu")) {
                                fromFieldSql = "dataSourceFrom";
                                toFieldSql = "dataSourceTo";
                            } else if (field.equals("Loại dữ liệu")) {
                                fromFieldSql = "mediaTypeIdFrom";
                                toFieldSql = "mediaTypeIdTo";
                            } else if (field.equals("Định dạng")) {
                                fromFieldSql = "fileTypeFrom";
                                toFieldSql = "fileTypeTo";
                            }

                            if (operator.equals(OPERATOR_IS) || operator.equals(OPERATOR_IS_ONE_OF) || operator.equals(OPERATOR_IS_NOT) || operator.equals(OPERATOR_IS_NOT_ONE_OF)) {
                                condition += " AND ( "
                                        + fromFieldSql + operatorSql + " :filter_value_" + k
                                        + " OR " + toFieldSql + operatorSql + " :filter_value_" + k
                                        + " ) ";
                            }
                        } else if (field.equals("Tên nguồn") || field.equals("Tên đích")) {
                        }
                    }
                } else {
                    LOGGER.error("Invalid filter values!");
                }
            }
        }

        return condition;
    }

    private String convertUnitFileSizeFrom(String unit) {
        String sql = "";
        if (!StringUtil.isNullOrEmpty(unit)) {
            switch (unit) {
                case "Bytes":
                    sql = " round(fileSizeFrom, 2) ";
                    break;
                case "KB":
                    sql = " round(fileSizeFrom / 1024, 2) ";
                    break;
                case "MB":
                    sql = " round(fileSizeFrom / 1048576, 2) ";
                    break;
                case "GB":
                    sql = " round(fileSizeFrom / 1073741824, 2) ";
                    break;
                case "TB":
                    sql = " round(fileSizeFrom / 1099511627776, 2) ";
                    break;
                case "PB":
                    sql = " round(fileSizeFrom / 1125899906842624, 2) ";
                    break;
                case "EB":
                    sql = " round(fileSizeFrom / 1152921504606846976, 2) ";
                    break;
                case "ZB":
                    sql = " round(fileSizeFrom / 1180591620717411303424, 2) ";
                    break;
                case "YB":
                    sql = " round(fileSizeFrom / 1208925819614629174706176, 2) ";
                    break;
            }
        }
        return sql;
    }

    private String convertUnitFileSizeTo(String unit) {
        String sql = "";
        if (!StringUtil.isNullOrEmpty(unit)) {
            switch (unit) {
                case "Bytes":
                    sql = " round(fileSizeTo, 2) ";
                    break;
                case "KB":
                    sql = " round(fileSizeTo / 1024, 2) ";
                    break;
                case "MB":
                    sql = " round(fileSizeTo / 1048576, 2) ";
                    break;
                case "GB":
                    sql = " round(fileSizeTo / 1073741824, 2) ";
                    break;
                case "TB":
                    sql = " round(fileSizeTo / 1099511627776, 2) ";
                    break;
                case "PB":
                    sql = " round(fileSizeTo / 1125899906842624, 2) ";
                    break;
                case "EB":
                    sql = " round(fileSizeTo / 1152921504606846976, 2) ";
                    break;
                case "ZB":
                    sql = " round(fileSizeTo / 1180591620717411303424, 2) ";
                    break;
                case "YB":
                    sql = " round(fileSizeTo / 1208925819614629174706176, 2) ";
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

}
