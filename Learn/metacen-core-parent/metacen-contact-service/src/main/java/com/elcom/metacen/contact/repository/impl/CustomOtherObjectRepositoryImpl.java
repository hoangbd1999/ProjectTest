package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.repository.CustomKeywordDataRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.ObjectRelationship;
import com.elcom.metacen.contact.model.OtherObject;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.CustomOtherObjectRepository;
import com.elcom.metacen.contact.repository.KeywordRepository;
import com.elcom.metacen.contact.service.CommonService;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.contact.service.ObjectRelationshipService;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.ObjectMapperUtils;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 *
 * @author hoangbd
 */
@Component
public class CustomOtherObjectRepositoryImpl extends BaseCustomRepositoryImpl<OtherObject> implements CustomOtherObjectRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOtherObjectRepositoryImpl.class);

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private ObjectRelationshipService objectRelationshipService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CustomKeywordDataRepository customKeywordDataRepository;

    @Autowired
    private KeywordDataRepository keywordDataRepository;

    @Override
    public Page<OtherObjectResponseDTO> search(OtherObjectFilterDTO otherObjectFilterDTO, Pageable pageable) {
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(otherObjectFilterDTO.getTerm())) {
            String termReplaceAll = "";
            String term = otherObjectFilterDTO.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + otherObjectFilterDTO.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("id").regex(term, "i"),
                    Criteria.where("name").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (otherObjectFilterDTO.getCountryIds() != null && !otherObjectFilterDTO.getCountryIds().isEmpty()) {
            andCriterias.add(Criteria.where("countryId").in(otherObjectFilterDTO.getCountryIds()));
        }
        if (otherObjectFilterDTO.getSideIds() != null && !otherObjectFilterDTO.getSideIds().isEmpty()) {
            andCriterias.add(Criteria.where("sideId").in(otherObjectFilterDTO.getSideIds()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // matchKeywordStage
        Criteria criteriaKeyword = new Criteria();
//        if (otherObjectFilterDTO.getKeywordIds() != null && !otherObjectFilterDTO.getKeywordIds().isEmpty()) {
//            criteriaKeyword = Criteria.where("keywordUuidLst").in(otherObjectFilterDTO.getKeywordIds());
//        }
        long elementsToSkip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long maxElements = pageable.getPageSize();
        AggregateKeywordDataObjectGeneralInfoDTO refObjects = null;
        if (otherObjectFilterDTO.getKeywordIds() != null && !otherObjectFilterDTO.getKeywordIds().isEmpty()) {
            refObjects = customKeywordDataRepository.findByKeywordIdsAndType(otherObjectFilterDTO.getKeywordIds(), 1, elementsToSkip, maxElements);
            criteriaKeyword = Criteria.where("uuid").in(refObjects.getPaginatedRefIdMap().keySet());
        }
        MatchOperation matchKeywordStage = Aggregation.match(criteriaKeyword);

        // sortStage
        AggregationResults<OtherObjectResponseDTO> output = null;
        if (otherObjectFilterDTO.getSort().equals("countryName") || otherObjectFilterDTO.getSort().equals("-countryName")) {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(otherObjectFilterDTO.getSort())) {
                String sortItem = otherObjectFilterDTO.getSort();
                if (sortItem.substring(0, 1).equals("-")) {
                    sortItem = "-name";
                    sortStage = sort(Sort.Direction.DESC, sortItem.substring(1));
                } else {
                    sortItem = "name";
                    sortStage = sort(Sort.Direction.ASC, sortItem);
                }
            }
            Aggregation aggregation = Aggregation.newAggregation(
                    sortStage,
                    Aggregation.lookup("other_object", "id", "country_id", "other_object"),
                    unwind("other_object", false),
                    Aggregation.match(Criteria.where("other_object.is_deleted").is(0)),
                    Aggregation.lookup("side", "other_object.side_id", "uuid", "side_info"),
                    unwind("side_info", true),
              //      Aggregation.lookup("keyword_data", "other_object.uuid", "ref_id", "objectKeywordInfo"),
                    project("id", "name")
                            .andExpression("'$id'").as("countryId")
                            .andExpression("'$name'").as("countryName")
                            .andExpression("'$other_object._id'").as("_id")
                            .andExpression("'$other_object.uuid'").as("uuid")
                            .andExpression("'$other_object.name'").as("name")
                            .andExpression("'$other_object.description'").as("description")
                            .andExpression("'$other_object.image_lst'").as("imageLst")
                            .andExpression("'$other_object.file_attachment_lst'").as("fileAttachmentLst")
                            .andExpression("'$other_object.created_by'").as("createdBy")
                            .andExpression("'$other_object.created_date'").as("createdDate")
                            .andExpression("'$other_object.modified_by'").as("modifiedBy")
                            .andExpression("'$other_object.modified_date'").as("modifiedDate")
                            .andExpression("'$side_info.uuid'").as("sideId")
                            .andExpression("'$side_info.name'").as("sideName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, Countries.class, OtherObjectResponseDTO.class);
        } else {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(otherObjectFilterDTO.getSort())) {
                String sortItem = otherObjectFilterDTO.getSort();
                if (sortItem.substring(0, 1).equals("-")) {
                    sortStage = sort(Sort.Direction.DESC, sortItem.substring(1));
                } else {
                    sortStage = sort(Sort.Direction.ASC, sortItem);
                }
            }
            Aggregation aggregation = Aggregation.newAggregation(
                    matchStage,
                    sortStage,
                    Aggregation.lookup("side", "side_id", "uuid", "sideInfo"),
                    unwind("sideInfo", true),
                    Aggregation.lookup("countries", "country_id", "id", "countryInfo"),
                    unwind("countryInfo", true),
             //       Aggregation.lookup("keyword_data", "uuid", "ref_id", "objectKeywordInfo"),
                    project("uuid", "name", "countryId", "description", "sideId", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, OtherObject.class, OtherObjectResponseDTO.class);
        }
        List<OtherObjectResponseDTO> results = output.getMappedResults();
        if (!results.isEmpty()) {
//            results = results.stream()
//                    .map(x -> {
//                        List<Keyword> keywordLst = keywordRepository.findByUuidInAndIsDeleted(x.getKeywordUuidLst(), DataDeleteStatus.NOT_DELETED.code());
//                        List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
//                        x.setKeywordLst(keywordDtoLst);
//                        return x;
//                    })
//                    .collect(Collectors.toList());
            mapKeywordDtoToResults(results);
        }

        // total
        long total = mongoOps.count(Query.query(allCriteria).limit(-1).skip(-1), domain);
        if(results.isEmpty()){
            total = 0;
        }
        return new PageImpl<>(results, pageable, total);
    }
    private void mapKeywordDtoToResults(List<OtherObjectResponseDTO> results) {
        Map<String, List<String>> refIdKeywordUuidsMap = keywordDataRepository.findByRefIdIn(results.stream().map(OtherObjectResponseDTO::getUuid).collect(toList()))
                .stream()
                .collect(groupingBy(KeywordData::getRefId, mapping(KeywordData::getKeywordId, toList())));
        List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(
                keywordService.findKeywordsByUuidList(
                        refIdKeywordUuidsMap.values().stream()
                                .flatMap(Collection::stream)
                                .collect(toList())), KeywordDTO.class);
        Map<String, KeywordDTO> keywordDTOMap = keywordDtoLst.stream()
                .collect(toMap(KeywordDTO::getUuid, identity()));
        Map<String, List<KeywordDTO>> result1 = refIdKeywordUuidsMap.entrySet().stream()
                .map(entry -> {
                    String refId = entry.getKey();
                    List<KeywordDTO> collect = entry.getValue().stream()
                            .map(keywordUuid -> keywordDTOMap.getOrDefault(keywordUuid, null))
                            .collect(toList());
                    return Map.entry(entry.getKey(), collect);
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        results.forEach(result -> {
            result.setKeywordLst(result1.getOrDefault(result.getUuid(), null));
        });
    }
    @Override
    public OtherObjectResponseDTO findOtherObjectByUuid(String uuid) {
        try {
            Criteria criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code())
                    .andOperator(Criteria.where("uuid").is(uuid));

            MatchOperation matchStage = Aggregation.match(criteria);
            Aggregation aggregation = Aggregation.newAggregation(
                    matchStage,
                    Aggregation.lookup("side", "side_id", "uuid", "sideInfo"),
                    unwind("sideInfo", true),
                    Aggregation.lookup("countries", "country_id", "id", "countryInfo"),
                    unwind("countryInfo", true),
                    Aggregation.lookup("keyword_data", "uuid", "ref_id", "objectKeywordInfo"),
                    project("uuid", "name", "countryId", "description", "sideId", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst")
            );

            AggregationResults<OtherObjectResponseDTO> output = mongoOps.aggregate(aggregation, OtherObject.class, OtherObjectResponseDTO.class);
            OtherObjectResponseDTO result = output.getUniqueMappedResult();
            if (result != null) {

                // keywordLst
                List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(result.getKeywordUuidLst());
                List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
                result.setKeywordLst(keywordDtoLst);

                // relationshipLst
                List<ObjectRelationshipDeltailDTO> relationshipLst = new ArrayList<>();

                List<ObjectRelationship> objectRelationshipLst = objectRelationshipService.getRelationshipsBySourceObjectId(ObjectType.OTHER_OBJECT.name(), uuid);
                Map<String, List<ObjectRelationship>> objRelationshipMapLst = objectRelationshipLst
                        .stream()
                        .collect(Collectors.groupingBy(ObjectRelationship::getDestObjectType));
                for (Map.Entry<String, List<ObjectRelationship>> entry : objRelationshipMapLst.entrySet()) {
                    String destObjectType = entry.getKey();
                    List<ObjectRelationship> objectRelationshipGroupLst = entry.getValue();
                    List<String> destObjectIds = objectRelationshipGroupLst.stream()
                            .map(ObjectRelationship::getDestObjectId)
                            .collect(Collectors.toList());

                    Map<String, ObjectGeneralInfoDTO> objectGeneralInfoMap = commonService.buildObjectGeneralInfoMap(destObjectType, destObjectIds);
                    for (ObjectRelationship objectRelationship : objectRelationshipGroupLst) {
                        ObjectRelationshipDeltailDTO objectRelationshipDeltailDTO = new ObjectRelationshipDeltailDTO();
                        objectRelationshipDeltailDTO.setNo(objectRelationship.getNo());
                        objectRelationshipDeltailDTO.setFromTime(DateUtils.format(objectRelationship.getFromTime()));
                        objectRelationshipDeltailDTO.setToTime(DateUtils.format(objectRelationship.getToTime()));
                        objectRelationshipDeltailDTO.setDestObjectInfo(objectGeneralInfoMap.get(objectRelationship.getDestObjectId()));
                        objectRelationshipDeltailDTO.setRelationshipType(objectRelationship.getRelationshipType());
                        objectRelationshipDeltailDTO.setNote(objectRelationship.getNote());
                        relationshipLst.add(objectRelationshipDeltailDTO);
                    }
                }
                Collections.sort(relationshipLst, (f1, f2)->{return f1.getNo().compareTo(f2.getNo());});
                result.setRelationshipLst(relationshipLst);
            }
            return result;
        } catch (Exception ex) {
            LOGGER.error("Error: " + ex);
            ex.printStackTrace();
        }
        return null;
    }
}
