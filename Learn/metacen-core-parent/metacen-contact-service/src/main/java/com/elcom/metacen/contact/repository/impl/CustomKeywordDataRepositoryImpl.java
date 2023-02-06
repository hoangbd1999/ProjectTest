package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.dto.AggregateKeywordDataObjectGeneralInfoDTO;
import com.elcom.metacen.contact.model.dto.KeywordDataDTO;
import com.elcom.metacen.contact.repository.CustomKeywordDataRepository;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Repository
public class CustomKeywordDataRepositoryImpl extends BaseCustomRepositoryImpl<KeywordData> implements CustomKeywordDataRepository {
    @Override
    public AggregateKeywordDataObjectGeneralInfoDTO findByKeywordIdsAndType(List<String> keywordIds, Integer type, Long skip, Long limit) {
        Criteria criteria = new Criteria();
        criteria.andOperator(
                Criteria.where("keyword_id").in(keywordIds),
                Criteria.where("type").is(type));
        MatchOperation match = Aggregation.match(criteria);
        GroupOperation group = Aggregation.group("ref_id").count().as("numCount");
        ProjectionOperation project = Aggregation.project("numCount").and("_id").as("refId");
        SortOperation sort = Aggregation.sort(Sort.by("numCount").descending())
                .and(Sort.by("refId").ascending());
        FacetOperation facet = Aggregation.facet(Aggregation.skip(skip), Aggregation.limit(limit)).as("paginatedRefIds")
                .and(Aggregation.count().as("count")).as("totalCount");
        UnwindOperation unwind = Aggregation.unwind("totalCount");
        Aggregation aggregation = Aggregation.newAggregation(match, group, project, sort, facet, unwind);
        AggregationResults<AggregateKeywordDataObjectGeneralInfoDTO> results = mongoOps.aggregate(aggregation, KeywordData.class, AggregateKeywordDataObjectGeneralInfoDTO.class);
        return results.getMappedResults().size() == 0 ?
                new AggregateKeywordDataObjectGeneralInfoDTO()
                        .setPaginatedRefIds(new ArrayList<>())
                        .setPaginatedResults(new ArrayList<>())
                        .setPaginatedRefIdMap(new HashMap<>())
                        .setTotalCount(null)
                : results.getMappedResults().get(0)
                .setPaginatedRefIdMap(results.getMappedResults().get(0).getPaginatedRefIds().stream()
                        .collect(toMap(KeywordDataDTO::getRefId, KeywordDataDTO::getNumCount)));
    }
}
