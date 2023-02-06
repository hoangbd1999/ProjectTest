package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.*;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.CustomKeywordDataRepository;
import com.elcom.metacen.contact.repository.CustomPeopleRepository;
import com.elcom.metacen.contact.repository.KeywordRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import com.elcom.metacen.contact.service.CommonService;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.contact.service.ObjectRelationshipService;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.ObjectMapperUtils;
import com.elcom.metacen.utils.StringUtil;
import com.mongodb.client.model.Accumulators;
import org.hibernate.criterion.Distinct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

/**
 * @author hoangbd
 */
@Component
public class CustomPeopleRepositoryImpl extends BaseCustomRepositoryImpl<People> implements CustomPeopleRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPeopleRepositoryImpl.class);

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
    public Page<PeopleResponseDTO> search(PeopleFilterDTO peopleFilterDTO, Pageable pageable) {
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(peopleFilterDTO.getTerm())) {
            String termReplaceAll = "";
            String term = peopleFilterDTO.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + peopleFilterDTO.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("id").regex(term, "i"),
                    Criteria.where("name").regex(term, "i"),
                    Criteria.where("mobileNumber").regex(term, "i"),
                    Criteria.where("email").regex(term, "i"),
                    Criteria.where("level").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (peopleFilterDTO.getCountryIds() != null && !peopleFilterDTO.getCountryIds().isEmpty()) {
            andCriterias.add(Criteria.where("countryId").in(peopleFilterDTO.getCountryIds()));
        }
        if (peopleFilterDTO.getGenders() != null && !peopleFilterDTO.getGenders().isEmpty()) {
            andCriterias.add(Criteria.where("gender").in(peopleFilterDTO.getGenders()));
        }
        if (peopleFilterDTO.getSideIds() != null && !peopleFilterDTO.getSideIds().isEmpty()) {
            andCriterias.add(Criteria.where("sideId").in(peopleFilterDTO.getSideIds()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // matchKeywordStage
        Criteria criteriaKeyword = new Criteria();
//        if (peopleFilterDTO.getKeywordIds() != null && !peopleFilterDTO.getKeywordIds().isEmpty()) {
//            criteriaKeyword = Criteria.where("keywordUuidLst").in(peopleFilterDTO.getKeywordIds());
//        }
        long elementsToSkip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long maxElements = pageable.getPageSize();
        AggregateKeywordDataObjectGeneralInfoDTO refObjects = null;
        if (peopleFilterDTO.getKeywordIds() != null && !peopleFilterDTO.getKeywordIds().isEmpty()) {
            refObjects = customKeywordDataRepository.findByKeywordIdsAndType(peopleFilterDTO.getKeywordIds(), 1, elementsToSkip, maxElements);
            criteriaKeyword = Criteria.where("uuid").in(refObjects.getPaginatedRefIdMap().keySet());
        }
        MatchOperation matchKeywordStage = Aggregation.match(criteriaKeyword);

        // sortStage
        AggregationResults<PeopleResponseDTO> output = null;
        if (peopleFilterDTO.getSort().equals("countryName") || peopleFilterDTO.getSort().equals("-countryName")) {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(peopleFilterDTO.getSort())) {
                String sortItem = peopleFilterDTO.getSort();
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
                    Aggregation.lookup("people_info", "id", "country_id", "people_info"),
                    unwind("people_info", false),
                    Aggregation.match(Criteria.where("people_info.is_deleted").is(0)),
                    Aggregation.lookup("side", "people_info.side_id", "uuid", "side_info"),
                    unwind("side_info", true),
    //                Aggregation.lookup("keyword_data", "people_info.uuid", "ref_id", "objectKeywordInfo"),
                    project("id", "name")
                            .andExpression("'$id'").as("countryId")
                            .andExpression("'$name'").as("countryName")
                            .andExpression("'$people_info._id'").as("_id")
                            .andExpression("'$people_info.uuid'").as("uuid")
                            .andExpression("'$people_info.name'").as("name")
                            .andExpression("'$people_info.mobile_number'").as("mobileNumber")
                            .andExpression("'$people_info.email'").as("email")
                            .andExpression("'$people_info.date_of_birth'").as("dateOfBirth")
                            .andExpression("'$people_info.gender'").as("gender")
                            .andExpression("'$people_info.address'").as("address")
                            .andExpression("'$people_info.level'").as("level")
                            .andExpression("'$people_info.description'").as("description")
                            .andExpression("'$people_info.image_lst'").as("imageLst")
                            .andExpression("'$people_info.file_attachment_lst'").as("fileAttachmentLst")
                            .andExpression("'$people_info.created_by'").as("createdBy")
                            .andExpression("'$people_info.created_date'").as("createdDate")
                            .andExpression("'$people_info.modified_by'").as("modifiedBy")
                            .andExpression("'$people_info.modified_date'").as("modifiedDate")
                            .andExpression("'$side_info.uuid'").as("sideId")
                            .andExpression("'$side_info.name'").as("sideName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, Countries.class, PeopleResponseDTO.class);
        } else {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(peopleFilterDTO.getSort())) {
                String sortItem = peopleFilterDTO.getSort();
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
              //      Aggregation.lookup("keyword_data", "uuid", "ref_id", "objectKeywordInfo"),
                    project("uuid", "name", "mobileNumber", "email", "countryId", "dateOfBirth", "gender", "address", "level", "description", "sideId", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, People.class, PeopleResponseDTO.class);
        }
        List<PeopleResponseDTO> results = output.getMappedResults();
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

    private void mapKeywordDtoToResults(List<PeopleResponseDTO> results) {
        Map<String, List<String>> refIdKeywordUuidsMap = keywordDataRepository.findByRefIdIn(results.stream().map(PeopleResponseDTO::getUuid).collect(toList()))
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

     // map keyword findPeopleByUuid do not use mapping keyword_data in query (DO NOT DELETE)
//    private void mapKeywordDtoToResultsTest(PeopleResponseDTO results) {
//
//        List<KeywordData> refIdKeywordUuidsMap = keywordDataRepository.findByRefIdIn(results.getUuid());
//        List<String> KeywordUuidLst = new ArrayList<>();
//        for (KeywordData keyword : refIdKeywordUuidsMap){
//            KeywordUuidLst.add(keyword.getKeywordId());
//        }
//        List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(KeywordUuidLst);
//        List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
//        results.setKeywordLst(keywordDtoLst);
//        List<String> listData = new ArrayList<>();
//        for (KeywordData oke : refIdKeywordUuidsMap){
//            listData.add(oke.getKeywordId());
//        }
//        results.setKeywordUuidLst(listData);
//    }

    @Override
    public PeopleResponseDTO findPeopleByUuid(String uuid) {
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
                    project("uuid", "name", "mobileNumber", "email", "countryId", "dateOfBirth", "gender", "address", "level", "description", "sideId", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst")
            );

            AggregationResults<PeopleResponseDTO> output = mongoOps.aggregate(aggregation, People.class, PeopleResponseDTO.class);
            PeopleResponseDTO result = output.getUniqueMappedResult();
            if (result != null) {

                // keywordLst
          //    mapKeywordDtoToResultsTest(result);
                List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(result.getKeywordUuidLst());
                List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
                result.setKeywordLst(keywordDtoLst);

                // relationshipLst
                List<ObjectRelationshipDeltailDTO> relationshipLst = new ArrayList<>();

                List<ObjectRelationship> objectRelationshipLst = objectRelationshipService.getRelationshipsBySourceObjectId(ObjectType.PEOPLE.name(), uuid);
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
                Collections.sort(relationshipLst, (f1, f2) -> {
                    return f1.getNo().compareTo(f2.getNo());
                });
                result.setRelationshipLst(relationshipLst);
            }
            return result;
        } catch (Exception ex) {
            LOGGER.error("Error: " + ex);
            ex.printStackTrace();
        }

        return null;
    }

    public List<People> findListPeople(List listId) {
        Criteria criteria;
        criteria = Criteria.where("uuidkey").in(listId);
        Query query = new Query().addCriteria(criteria);
        List<People> listPeople = mongoOps.find(query, domain);
        return listPeople;
    }
}
