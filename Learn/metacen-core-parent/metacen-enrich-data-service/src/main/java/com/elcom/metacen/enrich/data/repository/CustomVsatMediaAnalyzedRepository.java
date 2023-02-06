package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.config.ApplicationConfig;
import com.elcom.metacen.enrich.data.model.dto.AdvanceFilterDTO;
import com.elcom.metacen.enrich.data.model.dto.ConvertSizeDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaAnalyzedDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatMediaAnalyzedFilterDTO;
import com.elcom.metacen.utils.StringUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;

import org.hibernate.SessionFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Repository
public class CustomVsatMediaAnalyzedRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomVsatMediaAnalyzedRepository.class);

    public static final String OPERATOR_IS = "IS";
    public static final String OPERATOR_IS_NOT = "IS_NOT";
    public static final String OPERATOR_IS_ONE_OF = "IS_ONE_OF";
    public static final String OPERATOR_IS_NOT_ONE_OF = "IS_NOT_ONE_OF";
    public static final String OPERATOR_IS_BETWEEN = "IS_BETWEEN";
    public static final String OPERATOR_IS_NOT_BETWEEN = "IS_NOT_BETWEEN";
    public static final String OPERATOR_OR = "OR";
    public static final String OPERATOR_AND = "AND";

    @Autowired
    @Qualifier("clickHouseSession")
    SessionFactory clickHouseSessionFactory;

    private static final String MEDIA_INDEX = "media_analyzed";
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateFormatTime = new SimpleDateFormat("yyyy-MM-dd");

    public static final Integer MAX_VIEW_LENGTH_CONTENT = 30;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public CustomVsatMediaAnalyzedRepository(@Qualifier("clickHouseSession") EntityManagerFactory factory, @Qualifier("chDatasource") DataSource dataSource) {
        super(factory, dataSource);
    }

    public Page<VsatMediaAnalyzedDTO> filterVsatMediaAnalyzed(VsatMediaAnalyzedFilterDTO data) {
        try {
            Integer page = data.getPage() > 0 ? data.getPage() : 0;
            Pageable pageable = PageRequest.of(page, data.getSize());

            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withPageable(pageable);
            nativeSearchQueryBuilder.withTrackTotalHits(true); // https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-your-data.html#track-total-hits

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            if (!StringUtil.isNullOrEmpty(data.getTerm())) {
                String term = data.getTerm().trim();
                QueryBuilder matchQueryFirst = QueryBuilders.simpleQueryStringQuery(term).field("fileContentUtf8").analyzeWildcard(true);
                QueryBuilder matchQuerySecond = QueryBuilders.simpleQueryStringQuery(term).field("fileContentGB18030").analyzeWildcard(true);
                QueryBuilder matchQueryThird = QueryBuilders.simpleQueryStringQuery(term).field("mailSubject").analyzeWildcard(true);
                QueryBuilder matchQueryFour = QueryBuilders.simpleQueryStringQuery(term).field("mailContents").analyzeWildcard(true);
                QueryBuilder matchQueryFive = QueryBuilders.simpleQueryStringQuery(term).field("mailFrom").analyzeWildcard(true);
                QueryBuilder matchQuerySix = QueryBuilders.simpleQueryStringQuery(term).field("mailTo").analyzeWildcard(true);
                QueryBuilder matchQuerySeven = QueryBuilders.simpleQueryStringQuery(term).field("mailAttachments").analyzeWildcard(true);

                List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
                filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQueryFirst,
                        ScoreFunctionBuilders.weightFactorFunction(10)));
                filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQuerySecond,
                        ScoreFunctionBuilders.weightFactorFunction(10)));
                filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQueryThird,
                        ScoreFunctionBuilders.weightFactorFunction(7)));
                filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQueryFour,
                        ScoreFunctionBuilders.weightFactorFunction(7)));
                filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQueryFive,
                        ScoreFunctionBuilders.weightFactorFunction(5)));
                filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQuerySix,
                        ScoreFunctionBuilders.weightFactorFunction(5)));
                filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQuerySeven,
                        ScoreFunctionBuilders.weightFactorFunction(5)));

                FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
                filterFunctionBuilders.toArray(builders);

                FunctionScoreQueryBuilder termQueryBuilder = QueryBuilders.functionScoreQuery(builders)
                        .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
                        .setMinScore(2);

                nativeSearchQueryBuilder.withQuery(termQueryBuilder);
            }
            if (!StringUtil.isNullOrEmpty(data.getFromTime()) && !StringUtil.isNullOrEmpty(data.getToTime())) {
                Date from = dateFormat.parse(data.getFromTime());
                Date to = dateFormat.parse(data.getToTime());
                boolQueryBuilder.must(QueryBuilders.rangeQuery("processTime").from(from.getTime()).to(to.getTime()));
            }

            if (data.getDataVendorLst() != null && !data.getDataVendorLst().isEmpty()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                data.getDataVendorLst().forEach((dataVendor) -> {
                    boolQuery.should(QueryBuilders.matchPhraseQuery("dataVendor", dataVendor));
                });

                boolQueryBuilder.must(boolQuery);
            }
            if (data.getDataSourceIds() != null && !data.getDataSourceIds().isEmpty()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                data.getDataSourceIds().forEach((dataSourceId) -> {
                    boolQuery.should(QueryBuilders.termQuery("dataSourceId", dataSourceId));
                });

                boolQueryBuilder.must(boolQuery);
            }
            if (data.getMediaTypeIds() != null && !data.getMediaTypeIds().isEmpty()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                data.getMediaTypeIds().forEach((mediaTypeId) -> {
                    boolQuery.should(QueryBuilders.termQuery("mediaTypeId", mediaTypeId));
                });

                boolQueryBuilder.must(boolQuery);
            }
            if (data.getProcessStatusLst() != null && !data.getProcessStatusLst().isEmpty()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                data.getProcessStatusLst().forEach((processStatus) -> {
                    boolQuery.should(QueryBuilders.termQuery("processStatus", processStatus));
                });

                boolQueryBuilder.must(boolQuery);
            }
            // Lọc nâng cao
            List<AdvanceFilterDTO> filterList = data.getFilter();
            if (filterList != null && !filterList.isEmpty()) {
                int k = 0;
                for (AdvanceFilterDTO item : filterList) {
                    String field = item.getField();
                    String fieldQuery = convertField(field);
                    String operator = item.getOperator();
                    List<String> value = item.getValue();
                    if (value != null && !value.isEmpty()) {
                        // IN , NOT IN
                        if (operator.equals(OPERATOR_IS_BETWEEN)) { // Trong khoảng
                            List<Double> newValueFileSize = value.stream().map(Double::parseDouble).collect(Collectors.toList());
                            Double fromValue = newValueFileSize.get(0);
                            Double toValue = newValueFileSize.get(1);
                            ConvertSizeDTO unitFileSize = convertSizeToBytes(item.getUnit(), fromValue, toValue);
                            if (field.equals("Dung lượng")) {
                                if (!StringUtil.isNullOrEmpty(String.valueOf(unitFileSize))) {
                                    boolQueryBuilder.must(QueryBuilders.rangeQuery("fileSize").gte(unitFileSize.getFileSizeFromValue()).lte(unitFileSize.getFileSizeToValue()));
                                }
                            } else {
                                boolQueryBuilder.must(QueryBuilders.rangeQuery("fileSize").gte(unitFileSize.getFileSizeFromValue()).lte(unitFileSize.getFileSizeToValue()));
                            }
                        } else if (operator.equals(OPERATOR_IS_NOT_BETWEEN)) { // Ngoài khoảng
                            List<Double> newValue = value.stream().map(Double::parseDouble).collect(Collectors.toList());
                            Double fromValue = newValue.get(0);
                            Double toValue = newValue.get(1);
                            ConvertSizeDTO unitFileSize = convertSizeToBytes(item.getUnit(), fromValue, toValue);
                            if (field.equals("Dung lượng")) {
                                if (!StringUtil.isNullOrEmpty(String.valueOf(unitFileSize))) {
                                    boolQueryBuilder.mustNot(QueryBuilders.rangeQuery("fileSize").gte(unitFileSize.getFileSizeFromValue()).lte(unitFileSize.getFileSizeToValue()));
                                }
                            } else {
                                boolQueryBuilder.mustNot(QueryBuilders.rangeQuery("fileSize").gte(unitFileSize.getFileSizeFromValue()).lte(unitFileSize.getFileSizeToValue()));
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
                                List<Integer> ipValue = newValue;
                                if (operator.equals(OPERATOR_IS)) {
                                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                    ipValue.forEach((search) -> {
                                        boolQuery.should(QueryBuilders.termQuery(fieldQuery, search));
                                    });
                                    boolQueryBuilder.must(boolQuery);

                                } else if (operator.equals(OPERATOR_IS_NOT)) {
                                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                    ipValue.forEach((search) -> {
                                        boolQuery.should(QueryBuilders.termQuery(fieldQuery, search));
                                    });
                                    boolQueryBuilder.mustNot(boolQuery);
                                }
                            } else if (field.equals("Tên nguồn") || field.equals("Tên đích") || field.equals("Định dạng")) {
                                List<String> filterValue = value;
                                if (operator.equals(OPERATOR_IS) || operator.equals(OPERATOR_IS_ONE_OF)) {
                                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                    filterValue.forEach((search) -> {
                                        boolQuery.should(QueryBuilders.matchPhraseQuery(fieldQuery, search));
                                    });
                                    boolQueryBuilder.must(boolQuery);
                                } else if (operator.equals(OPERATOR_IS_NOT) || operator.equals(OPERATOR_IS_NOT_ONE_OF)) {
                                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                    filterValue.forEach((search) -> {
                                        boolQuery.should(QueryBuilders.matchPhraseQuery(fieldQuery, search));
                                    });
                                    boolQueryBuilder.mustNot(boolQuery);
                                }
                            } else if (field.equals("IP nguồn") || field.equals("IP đích")) {
                                List<String> newValue = new ArrayList<>();
                                for (String s : value) {
                                    if (s.contains(",")) {
                                        String[] arrTmp = s.split(",");
                                        for (String s2 : arrTmp) {
                                            newValue.add(s2);
                                        }
                                    } else {
                                        newValue.add(s);
                                    }
                                }
                                List<String> ipValue = newValue;
                                if (operator.equals(OPERATOR_IS)) {
                                    for (String ip : ipValue) {
                                        String ipTrim = ip.trim().toUpperCase();
                                        String ipValueFinal = ipTrim;
                                        if (ipTrim.contains("X")) {
                                            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                            ipValue.forEach((search) -> {
                                                boolQuery.should(QueryBuilders.queryStringQuery(search).field(fieldQuery).analyzeWildcard(true));
                                            });
                                            boolQueryBuilder.must(boolQuery);
                                        } else {
                                            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                            ipValue.forEach((search) -> {
                                                boolQuery.should(QueryBuilders.termQuery(fieldQuery, search));
                                            });
                                            boolQueryBuilder.must(boolQuery);
                                        }
                                    }
                                } else if (operator.equals(OPERATOR_IS_NOT)) {
                                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                    ipValue.forEach((search) -> {
                                        boolQuery.should(QueryBuilders.termQuery(fieldQuery, search));
                                    });
                                    boolQueryBuilder.mustNot(boolQuery);
                                }
                            } else { // Các trường lọc còn lại
                                List<String> filterValue = value;
                                if (operator.equals(OPERATOR_IS) || operator.equals(OPERATOR_IS_ONE_OF)) {
                                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                    filterValue.forEach((search) -> {
                                        boolQuery.should(QueryBuilders.termQuery(fieldQuery, search));
                                    });
                                    boolQueryBuilder.must(boolQuery);
                                } else if (operator.equals(OPERATOR_IS_NOT) || operator.equals(OPERATOR_IS_NOT_ONE_OF)) {
                                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                                    filterValue.forEach((search) -> {
                                        boolQuery.should(QueryBuilders.termQuery(fieldQuery, search));
                                    });
                                    boolQueryBuilder.mustNot(boolQuery);
                                }
                            }
                        }
                    }
                }
            }
            // Lọc theo cột dữ liệu
            if (!StringUtil.isNullOrEmpty(data.getUuid())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("id", data.getUuid()));
            }
            if (!StringUtil.isNullOrEmpty(data.getDataVendor())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("dataVendor", data.getDataVendor()));
            }
            if (!StringUtil.isNullOrEmpty(data.getDataSourceName())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("dataSourceName", data.getDataSourceName()));
            }

            if (!StringUtil.isNullOrEmpty(data.getSourceIp())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("sourceIp", data.getSourceIp()));
            }
            if (data.getSourcePort() != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("sourcePort", data.getSourcePort()));
            }
            if (data.getSourceId() != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("sourceId", data.getSourceId()));
            }
            if (!StringUtil.isNullOrEmpty(data.getSourceName())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("sourceName", data.getSourceName()));
            }

            if (!StringUtil.isNullOrEmpty(data.getDestIp())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("destIp", data.getDestIp()));
            }
            if (data.getDestPort() != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("destPort", data.getDestPort()));
            }
            if (data.getDestId() != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("destId", data.getDestId()));
            }
            if (!StringUtil.isNullOrEmpty(data.getDestName())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("destName", data.getDestName()));
            }

            if (!StringUtil.isNullOrEmpty(data.getMediaTypeName())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("mediaTypeName", data.getMediaTypeName()));
            }
            if (!StringUtil.isNullOrEmpty(data.getFileType())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("fileType", data.getFileType()));
            }

            if (!StringUtil.isNullOrEmpty(data.getEventTime())) {
//                String from = data.getEventTime() + " 00:00:00";
//                String to = data.getEventTime() + " 23:59:59";
//                Date eventTimeFrom = dateFormat.parse(from);
//                Date eventTimeTo = dateFormat.parse(to);
//                boolQueryBuilder.must(QueryBuilders.rangeQuery("eventTime").gte(eventTimeFrom.getTime()).lte(eventTimeTo.getTime()));
                String from = data.getEventTime();
                Date eventTimeFrom = dateFormat.parse(from);
                boolQueryBuilder.must(QueryBuilders.matchQuery("eventTime",eventTimeFrom.getTime()));
            }
            if (!StringUtil.isNullOrEmpty(data.getProcessTime())) {
//                String from = data.getProcessTime() + " 00:00:00";
//                String to = data.getProcessTime() + " 23:59:59";
//                Date processTimeFrom = dateFormat.parse(from);
//                Date processTimeTo = dateFormat.parse(to);
//                boolQueryBuilder.must(QueryBuilders.rangeQuery("processTime").gte(processTimeFrom.getTime()).lte(processTimeTo.getTime()));
                String from = data.getProcessTime();
                Date processTimeFrom = dateFormat.parse(from);
                boolQueryBuilder.must(QueryBuilders.matchQuery("processTime",processTimeFrom.getTime()));
            }

            // withFilter
            nativeSearchQueryBuilder.withFilter(boolQueryBuilder);

            // Sort
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("processTime").order(SortOrder.DESC));

            NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
            LOGGER.info("DSL Query: {}", searchQuery.getQuery() != null ? searchQuery.getQuery().toString() : "");
            LOGGER.info("DSL Filter: {}", searchQuery.getFilter() != null ? searchQuery.getFilter().toString() : "");

            List<VsatMediaAnalyzedDTO> vsatMediaAnalyzedList = new ArrayList<>();
            SearchHits<Object> searchHits = elasticsearchOperations
                    .search(searchQuery,
                            Object.class,
                            IndexCoordinates.of(MEDIA_INDEX));
            if (searchHits.getTotalHits() <= 0) {
                return new PageImpl<>(vsatMediaAnalyzedList, pageable, 0);
            }

            vsatMediaAnalyzedList = searchHits.stream().map((s) -> enrichVsatMediaAnalyzed(s.getContent(), s.getId())).collect(Collectors.toList());
            return new PageImpl<>(vsatMediaAnalyzedList, pageable, searchHits.getTotalHits());
        } catch (Exception ex) {
            LOGGER.error("ERROR filter vsat media analyzed: ", ex);
            ex.printStackTrace();
        }

        return null;
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
            case "Dung lượng":
                fieldSql = "fileSize";
                break;
        }

        return fieldSql;
    }

    private ConvertSizeDTO convertSizeToBytes(String unit, Double fromValue, Double toValue) {
        ConvertSizeDTO convertSizeDTO = new ConvertSizeDTO();
        Double fileSizeFromValue = null;
        Double fileSizeToValue = null;
        if (!StringUtil.isNullOrEmpty(unit)) {
            switch (unit) {
                case "Bytes":
                    fileSizeFromValue = fromValue;
                    fileSizeToValue = toValue;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "KB":
                    fileSizeFromValue = fromValue * 1024;
                    fileSizeToValue = toValue * 1024;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "MB":
                    fileSizeFromValue = fromValue * 1048576;
                    fileSizeToValue = toValue * 1048576;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "GB":
                    fileSizeFromValue = fromValue * 1073741824;
                    fileSizeToValue = toValue * 1073741824;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "TB":
                    fileSizeFromValue = fromValue * 1099511627776l;
                    fileSizeToValue = toValue * 1099511627776l;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "PB":
                    fileSizeFromValue = fromValue * 1125899906842624l;
                    fileSizeToValue = toValue * 1125899906842624l;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "EB":
                    fileSizeFromValue = fromValue * 1152921504606846976l;
                    fileSizeToValue = toValue * 1152921504606846976l;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "ZB":
                    fileSizeFromValue = fromValue * 1180591620717411303424d;
                    fileSizeToValue = toValue * 1180591620717411303424d;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
                case "YB":
                    fileSizeFromValue = fromValue * 1208925819614629174706176d;
                    fileSizeToValue = toValue * 1208925819614629174706176d;
                    convertSizeDTO.setFileSizeFromValue(fileSizeFromValue);
                    convertSizeDTO.setFileSizeToValue(fileSizeToValue);
                    break;
            }
        }
        return convertSizeDTO;
    }

    private VsatMediaAnalyzedDTO enrichVsatMediaAnalyzed(Object objectVsatMedia, String uuid) {
        VsatMediaAnalyzedDTO vsatMediaAnalyzed = modelMapper.map(objectVsatMedia, VsatMediaAnalyzedDTO.class);

        String fileContentUtf8 = vsatMediaAnalyzed.getFileContentUtf8();
        String fileContentGB18030 = vsatMediaAnalyzed.getFileContentGB18030();
        String mailContents = vsatMediaAnalyzed.getMailContents();
        String mailTo = vsatMediaAnalyzed.getMailTo();
        String mailRaw = vsatMediaAnalyzed.getMailRaw();
        String mailAttachments = vsatMediaAnalyzed.getMailAttachments();
        if (!StringUtil.isNullOrEmpty(fileContentUtf8) && fileContentUtf8.length() > MAX_VIEW_LENGTH_CONTENT) {
            fileContentUtf8 = fileContentUtf8.substring(0, MAX_VIEW_LENGTH_CONTENT) + "...";
        }
        if (!StringUtil.isNullOrEmpty(fileContentGB18030) && fileContentGB18030.length() > MAX_VIEW_LENGTH_CONTENT) {
            fileContentGB18030 = fileContentGB18030.substring(0, MAX_VIEW_LENGTH_CONTENT) + "...";
        }
        if (!StringUtil.isNullOrEmpty(mailContents) && mailContents.length() > MAX_VIEW_LENGTH_CONTENT) {
            mailContents = mailContents.substring(0, MAX_VIEW_LENGTH_CONTENT) + "...";
        }
        if (!StringUtil.isNullOrEmpty(mailTo) && mailTo.length() > MAX_VIEW_LENGTH_CONTENT) {
            mailTo = mailTo.substring(0, MAX_VIEW_LENGTH_CONTENT) + "...";
        }
        if (!StringUtil.isNullOrEmpty(mailRaw) && mailRaw.length() > MAX_VIEW_LENGTH_CONTENT) {
            mailRaw = mailRaw.substring(0, MAX_VIEW_LENGTH_CONTENT) + "...";
        }
        if (!StringUtil.isNullOrEmpty(mailAttachments) && mailAttachments.length() > MAX_VIEW_LENGTH_CONTENT) {
            mailAttachments = mailAttachments.substring(0, MAX_VIEW_LENGTH_CONTENT) + "...";
        }

        vsatMediaAnalyzed.setId(uuid);
        vsatMediaAnalyzed.setFileContentUtf8(fileContentUtf8);
        vsatMediaAnalyzed.setFileContentGB18030(fileContentGB18030);
        vsatMediaAnalyzed.setMailContents(mailContents);
        vsatMediaAnalyzed.setMailTo(mailTo);
        vsatMediaAnalyzed.setMailRaw(mailRaw);
        vsatMediaAnalyzed.setMailAttachments(mailAttachments);
        vsatMediaAnalyzed.setFilePathLocal(vsatMediaAnalyzed.getFilePath());
        vsatMediaAnalyzed.setFilePath(vsatMediaAnalyzed.getFilePath().replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, ApplicationConfig.MEDIA_LINK_ROOT_API));

        return vsatMediaAnalyzed;
    }

    public Object findByUuid(String id) {
        QueryBuilder queryBuilder = QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchPhraseQuery("id", id));
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        SearchHits<Object> searchHits = elasticsearchOperations
                .search(searchQuery,
                        Object.class,
                        IndexCoordinates.of(MEDIA_INDEX));
        return searchHits.getSearchHit(0).getContent();
    }
}
