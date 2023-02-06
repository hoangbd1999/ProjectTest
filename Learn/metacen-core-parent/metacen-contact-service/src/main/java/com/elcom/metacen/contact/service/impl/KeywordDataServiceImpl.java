/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.converter.CustomProjectAggregationOperation;
import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.CustomKeywordDataRepository;
import com.elcom.metacen.contact.repository.CustomKeywordRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.ObjectMapperUtils;
import com.elcom.metacen.utils.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author Admin
 */
@Service
public class KeywordDataServiceImpl implements KeywordDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordDataServiceImpl.class);

    @Autowired
    KeywordDataRepository keywordDataRepository;

    @Autowired
    CustomKeywordRepository customKeywordRepository;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    protected MongoOperations mongoOps;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private CustomKeywordDataRepository customKeywordDataRepository;

    @Override
    public KeywordData insert(KeywordGrantRequestDTO keywordGrantRequestDTO, String keyWordId) {
        try {
            KeywordData keywordData = new KeywordData();
            keywordData.setUuid(UUID.randomUUID().toString());
            keywordData.setType(keywordGrantRequestDTO.getType());
            keywordData.setRefId(keywordGrantRequestDTO.getRefId());
            keywordData.setRefType(keywordGrantRequestDTO.getRefType());
            keywordData.setKeywordId(keyWordId);
            keywordData.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            keywordData.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));

            KeywordData response = keywordDataRepository.save(keywordData);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save keywordData failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public KeywordData save(KeywordDataRequestDTO keywordDataRequestDTO) {
        KeywordData keywordData = new KeywordData();
        List<String> listData = keywordDataRequestDTO.getKeywordIds();
        for (int i = 0; i < listData.size(); i++) {
            keywordData.setUuid(UUID.randomUUID().toString());
            keywordData.setType(keywordDataRequestDTO.getType());
            keywordData.setRefId(keywordDataRequestDTO.getRefId());
            keywordData.setRefType(keywordDataRequestDTO.getRefType());
            keywordData.setKeywordId(listData.get(i));
            keywordData.setCreatedDate(DateUtils.convertToLocalDateTime(new Date()));
            keywordData.setModifiedDate(DateUtils.convertToLocalDateTime(new Date()));
        }
        KeywordData response = keywordDataRepository.save(keywordData);
        return response;
    }

    @Override
    public List<KeywordData> findByRefId(String refId, String keywordIds) {
        List<KeywordData> keywordData = keywordDataRepository.findByRefIdAndKeywordId(refId, keywordIds);
        return keywordData;
    }

    @Override
    public List<KeywordData> findByRefId(String refId) {
        List<KeywordData> keywordData = keywordDataRepository.findByRefId(refId);
        return keywordData;
    }

    @Override
    public List<KeywordData> findByRefIdAndType(String refId, Integer type) {
        List<KeywordData> keywordData = keywordDataRepository.findByRefIdAndType(refId, type);
        return keywordData;
    }

    @Override
    public List<KeywordData> delete(List<KeywordData> keywordData) {
        try {
            keywordDataRepository.deleteAll(keywordData);
            return null;
        } catch (Exception ex) {
            LOGGER.error("delete keywordData failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<KeywordDataObjectGeneralInfoDTO> getKeywordDataObject(KeyworDataObject keyworDataObject) {
        Integer page = keyworDataObject.getPage() > 0 ? keyworDataObject.getPage() : 0;
        Pageable pageable = PageRequest.of(page, keyworDataObject.getSize());
        Criteria criteria;
        criteria = Criteria.where("type").is(1);

        List<Criteria> andCriteriasOne = new ArrayList<>();
        List<Criteria> andCriteriasTwo = new ArrayList<>();

        if (keyworDataObject.getObjectTypeLst() != null && !keyworDataObject.getObjectTypeLst().isEmpty()) {
            List<String> objectTypeLst = keyworDataObject.getObjectTypeLst();
            if (objectTypeLst.stream().anyMatch(ObjectType.VEHICLE.name()::equals)) {
                List<String> objectTypeNewList = new ArrayList<String>(objectTypeLst);
                objectTypeNewList.remove(ObjectType.VEHICLE.name());
                objectTypeNewList.addAll(Arrays.asList(ObjectType.AIRPLANE.name(), ObjectType.VESSEL.name(), ObjectType.OTHER_VEHICLE.name()));

                andCriteriasOne.add(Criteria.where("objectType").in(objectTypeNewList));
            } else {
                andCriteriasOne.add(Criteria.where("objectType").in(objectTypeLst));
            }
        }

        if (!StringUtil.isNullOrEmpty(keyworDataObject.getTerm())) {
            String termReplaceAll = "";
            String term = keyworDataObject.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + keyworDataObject.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("id").regex(term, "i"),
                    Criteria.where("name").regex(term, "i")
            );
            andCriteriasTwo.add(termCriteria);
        }
//        if (keyworDataObject.getKeywordIds() != null && !keyworDataObject.getKeywordIds().isEmpty() || keyworDataObject.getObjectIds() != null && !keyworDataObject.getObjectIds().isEmpty()) {
//            Criteria keywordCriteria = new Criteria();
//            keywordCriteria.orOperator(
//                    Criteria.where("keywordIds").in(keyworDataObject.getKeywordIds()),
//                    Criteria.where("uuid").in(keyworDataObject.getObjectIds())
//
//            );
//            andCriteriasTwo.add(keywordCriteria);
//        }

        long elementsToSkip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long maxElements = pageable.getPageSize();

        // Tạm thời chưa sử dụng tới objectIds
        AggregateKeywordDataObjectGeneralInfoDTO refObjects = null;
        if (keyworDataObject.getKeywordIds() != null && !keyworDataObject.getKeywordIds().isEmpty()) {
            refObjects = customKeywordDataRepository.findByKeywordIdsAndType(keyworDataObject.getKeywordIds(), 1, elementsToSkip, maxElements);
            if (refObjects != null) {
                Criteria keywordCriteria = Criteria.where("uuid").in(refObjects.getPaginatedRefIdMap().keySet());
                andCriteriasTwo.add(keywordCriteria);
            }
        }

        Criteria criteriaOne = new Criteria();
        if (andCriteriasOne.size() > 0) {
            criteriaOne = criteriaOne.andOperator(andCriteriasOne.stream().toArray(Criteria[]::new));
        }
        Criteria criteriaTwo = new Criteria();
        if (andCriteriasTwo.size() > 0) {
            criteriaTwo = criteriaTwo.andOperator(andCriteriasTwo.stream().toArray(Criteria[]::new));
        }

        MatchOperation matchOneStage = Aggregation.match(criteriaOne);
        MatchOperation matchTwoStage = Aggregation.match(criteriaTwo);

        String query1 = "{ $addFields: { objectType: 'PEOPLE' } }";
        String query2 = "{ $match: { is_deleted: 0 } }";
        String query3 = "{ $unionWith: { coll: 'organisation', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'ORGANISATION'}}] } }";
        String query4 = "{ $unionWith: { coll: 'aero_airplane_info', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'AIRPLANE'}}] } }";
        String query5 = "{ $unionWith: { coll: 'marine_vessel_info', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'VESSEL'}}] } }";
        String query6 = "{ $unionWith: { coll: 'other_vehicle', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'OTHER_VEHICLE'}}] } }";
        String query7 = "{ $unionWith: { coll: 'event', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'EVENT'}}] } }";
        String query8 = "{ $unionWith: { coll: 'areas', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'AREA'}}] } }";
        String query9 = "{ $unionWith: { coll: 'infrastructure', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'INFRASTRUCTURE'}}] } }";
        String query10 = "{ $unionWith: { coll: 'other_object', pipeline: [{ $match: { is_deleted: 0 } }, { $set: { objectType: 'OTHER_OBJECT'}}] } }";
        String query11 = "{ $facet: { paginatedResults: [{ $skip: " + (refObjects == null ? elementsToSkip : 0) + " }, { $limit: " + maxElements + " }], totalCount: [{ $count: 'count' }] } }";
        String query12 = "{ $unwind: '$totalCount' }";

        List<AggregationOperation> aggregationOperations = new ArrayList<>();
        aggregationOperations.add(new CustomProjectAggregationOperation(query1));
        aggregationOperations.add(new CustomProjectAggregationOperation(query2));
        aggregationOperations.add(new CustomProjectAggregationOperation(query3));
        aggregationOperations.add(new CustomProjectAggregationOperation(query4));
        aggregationOperations.add(new CustomProjectAggregationOperation(query5));
        aggregationOperations.add(new CustomProjectAggregationOperation(query6));
        aggregationOperations.add(new CustomProjectAggregationOperation(query7));
        aggregationOperations.add(new CustomProjectAggregationOperation(query8));
        aggregationOperations.add(new CustomProjectAggregationOperation(query9));
        aggregationOperations.add(new CustomProjectAggregationOperation(query10));
        aggregationOperations.add(project("id", "uuid", "name", "objectType", "mmsi"));
        aggregationOperations.add(matchOneStage);
//        aggregationOperations.add(Aggregation.lookup("keyword_data", "uuid", "ref_id", "objKeyword"));
//        aggregationOperations.add(project("id", "uuid", "name", "objectType").andExpression("'$objKeyword.keyword_id'").as("keywordIds"));
        aggregationOperations.add(matchTwoStage);
    //    aggregationOperations.add(new CustomProjectAggregationOperation(query11));
//        aggregationOperations.add(project("id", "uuid", "name", "objectType", "keywordIds").andExpression("{$size: '$keywordIds'}").as("sizeKeywordMap"));
       // aggregationOperations.add(matchTwoStage);
//        aggregationOperations.add(sort(Sort.Direction.DESC, "sizeKeywordMap"));
        if(refObjects == null) aggregationOperations.add(sort(Sort.Direction.ASC, "name"));
        aggregationOperations.add(new CustomProjectAggregationOperation(query11));
        aggregationOperations.add(new CustomProjectAggregationOperation(query12));

        TypedAggregation<People> aggregation = Aggregation.newAggregation(
                People.class,
                aggregationOperations
        );
        AggregationResults<AggregateKeywordDataObjectGeneralInfoDTO> output = mongoOps.aggregate(aggregation, AggregateKeywordDataObjectGeneralInfoDTO.class);
        List<AggregateKeywordDataObjectGeneralInfoDTO> aggregateObjectGeneralInfoDTOLst = output.getMappedResults();

        List<KeywordDataObjectGeneralInfoDTO> results = new ArrayList<>();
        long total = 0;
        if (!aggregateObjectGeneralInfoDTOLst.isEmpty()) {
            results = aggregateObjectGeneralInfoDTOLst.get(0).getPaginatedResults();
            total = refObjects != null && refObjects.getTotalCount() != null ? refObjects.getTotalCount().getCount() : aggregateObjectGeneralInfoDTOLst.get(0).getTotalCount().getCount();

            if (refObjects != null) {
                // Sắp xếp theo thứ tự keyword
                Map<String, Integer> finalRefObjectIds = refObjects.getPaginatedRefIdMap();
                Comparator<Pair<String, Integer>> comparator = Comparator.comparing(Pair::getRight);
                Comparator<Pair<String, Integer>> reversed = comparator.reversed();
                results = results.stream()
                        .map(result -> Pair.of(result, finalRefObjectIds.getOrDefault(result.getUuid(), 0)))
                        .sorted(Comparator.comparingInt(Pair<KeywordDataObjectGeneralInfoDTO, Integer>::getRight).reversed())
                        .map(Pair::getLeft)
                        .collect(toList());
            }
//            results = results.stream()
//                    .map(x -> {
//                        List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(x.getKeywordIds());
//                        List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
//                        x.setKeywordLst(keywordDtoLst);
//
//                        return x;
//                    })
//                    .collect(Collectors.toList());

        }

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public KeywordData findByRefIdAndType(String refId, List<String> keywordIds, Integer type) {
        KeywordData keywordData = keywordDataRepository.findByRefIdAndKeywordIdInAndType(refId, keywordIds, type);
        return keywordData;
    }

    @Override
    public boolean save(KeywordDataDTO keywordDataDTO) {
        try {
            List<String> keywordIds = keywordDataDTO.getKeywordIds();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                List<KeywordData> keywordDataList = new ArrayList<>();
                Date now = new Date();
                for (String keywordId : keywordIds) {
                    KeywordData keywordData = new KeywordData();
                    keywordData.setUuid(UUID.randomUUID().toString());
                    keywordData.setRefId(keywordDataDTO.getRefId());
                    keywordData.setRefType(keywordDataDTO.getRefType());
                    keywordData.setType(1);
                    keywordData.setKeywordId(keywordId);
                    keywordData.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    keywordData.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    keywordDataList.add(keywordData);
                }

                keywordDataRepository.saveAll(keywordDataList);
            }

            return true;
        } catch (Exception ex) {
            LOGGER.error("Save object keyword failed >>> {}", ex.toString());
            return false;
        }
    }

    @Override
    public boolean update(KeywordDataDTO keywordDataDTO) {
        try {
            // delete object keyword
            deleteObjectKeyword(keywordDataDTO.getRefId());

            // insert object keyword
            List<String> keywordIds = keywordDataDTO.getKeywordIds();
            if (keywordIds != null && !keywordIds.isEmpty()) {
                List<KeywordData> keywordDataList = new ArrayList<>();
                Date now = new Date();
                for (String keywordId : keywordIds) {
                    KeywordData keywordData = new KeywordData();
                    keywordData.setUuid(UUID.randomUUID().toString());
                    keywordData.setRefId(keywordDataDTO.getRefId());
                    keywordData.setRefType(keywordDataDTO.getRefType());
                    keywordData.setType(1);
                    keywordData.setKeywordId(keywordId);
                    keywordData.setCreatedDate(DateUtils.convertToLocalDateTime(now));
                    keywordData.setModifiedDate(DateUtils.convertToLocalDateTime(now));

                    keywordDataList.add(keywordData);
                }

                keywordDataRepository.saveAll(keywordDataList);
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Update object keyword failed >>> {}", ex.toString());
            return false;
        }
    }

    @Override
    public List<KeywordData> findByKeywordId(String keywordId) {
        return keywordDataRepository.findByKeywordId(keywordId);
    }

    private void deleteObjectKeyword(String refId) {
        keywordDataRepository.deleteByRefId(refId);
    }
}
