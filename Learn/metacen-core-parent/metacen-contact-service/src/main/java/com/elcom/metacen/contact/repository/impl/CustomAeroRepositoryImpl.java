package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.AeroAirplaneInfo;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasResponseDTO;
import com.elcom.metacen.contact.repository.CustomKeywordDataRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.ObjectRelationship;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroFilterDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroResponseDTO;
import com.elcom.metacen.contact.repository.CustomAeroRepository;
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
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class CustomAeroRepositoryImpl extends BaseCustomRepositoryImpl<AeroAirplaneInfo> implements CustomAeroRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAeroRepositoryImpl.class);

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

    public Page<AeroResponseDTO> search(AeroFilterDTO aeroFilterDTO, Pageable pageable) {
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(aeroFilterDTO.getTerm())) {
            String termReplaceAll = "";
            String term = aeroFilterDTO.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + aeroFilterDTO.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("id").regex(term, "i"),
                    Criteria.where("name").regex(term, "i"),
                    Criteria.where("model").regex(term, "i"),
                    Criteria.where("permanent_base").regex(term, "i"),
                    Criteria.where("dim_length").regex(term, "i"),
                    Criteria.where("dim_width").regex(term, "i"),
                    Criteria.where("dim_height").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (aeroFilterDTO.getCountryIds() != null && !aeroFilterDTO.getCountryIds().isEmpty()) {
            andCriterias.add(Criteria.where("countryId").in(aeroFilterDTO.getCountryIds()));
        }
        if (aeroFilterDTO.getSideIds() != null && !aeroFilterDTO.getSideIds().isEmpty()) {
            andCriterias.add(Criteria.where("sideId").in(aeroFilterDTO.getSideIds()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // matchKeywordStage
        Criteria criteriaKeyword = new Criteria();
//        if (aeroFilterDTO.getKeywordIds() != null && !aeroFilterDTO.getKeywordIds().isEmpty()) {
//            criteriaKeyword = Criteria.where("keywordUuidLst").in(aeroFilterDTO.getKeywordIds());
//        }
        long elementsToSkip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long maxElements = pageable.getPageSize();
        AggregateKeywordDataObjectGeneralInfoDTO refObjects = null;
        if (aeroFilterDTO.getKeywordIds() != null && !aeroFilterDTO.getKeywordIds().isEmpty()) {
            refObjects = customKeywordDataRepository.findByKeywordIdsAndType(aeroFilterDTO.getKeywordIds(), 1, elementsToSkip, maxElements);
            criteriaKeyword = Criteria.where("uuid").in(refObjects.getPaginatedRefIdMap().keySet());
        }
        MatchOperation matchKeywordStage = Aggregation.match(criteriaKeyword);

        // sortStage
        AggregationResults<AeroResponseDTO> output = null;
        if (aeroFilterDTO.getSort().equals("countryName") || aeroFilterDTO.getSort().equals("-countryName")) {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(aeroFilterDTO.getSort())) {
                String sortItem = aeroFilterDTO.getSort();
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
                    Aggregation.lookup("aero_airplane_info", "id", "country_id", "aero_airplane_info"),
                    unwind("aero_airplane_info", false),
                    Aggregation.match(Criteria.where("aero_airplane_info.is_deleted").is(0)),
                    Aggregation.lookup("side", "aero_airplane_info.side_id", "uuid", "side_info"),
                    unwind("side_info", true),
                    Aggregation.lookup("object_types", "aero_airplane_info.type_id", "type_id", "objectTypeInfo"),
                    unwind("objectTypeInfo", true),
             //       Aggregation.lookup("keyword_data", "aero_airplane_info.uuid", "ref_id", "objectKeywordInfo"),
                    project("id", "name")
                            .andExpression("'$id'").as("countryId")
                            .andExpression("'$name'").as("countryName")
                            .andExpression("'$aero_airplane_info._id'").as("_id")
                            .andExpression("'$aero_airplane_info.uuid'").as("uuid")
                            .andExpression("'$aero_airplane_info.name'").as("name")
                            .andExpression("'$aero_airplane_info.model'").as("model")
                            .andExpression("'$aero_airplane_info.dim_length'").as("dimLength")
                            .andExpression("'$aero_airplane_info.dim_width'").as("dimWidth")
                            .andExpression("'$aero_airplane_info.dim_height'").as("dimHeight")
                            .andExpression("'$aero_airplane_info.speed_max'").as("speedMax")
                            .andExpression("'$aero_airplane_info.gross_tonnage'").as("grossTonnage")
                            .andExpression("'$aero_airplane_info.payroll_time'").as("payrollTime")
                            .andExpression("'$aero_airplane_info.equipment'").as("equipment")
                            .andExpression("'$aero_airplane_info.permanent_base'").as("permanentBase")
                            .andExpression("'$aero_airplane_info.description'").as("description")
                            .andExpression("'$aero_airplane_info.image_lst'").as("imageLst")
                            .andExpression("'$aero_airplane_info.file_attachment_lst'").as("fileAttachmentLst")
                            .andExpression("'$aero_airplane_info.created_by'").as("createdBy")
                            .andExpression("'$aero_airplane_info.created_date'").as("createdDate")
                            .andExpression("'$aero_airplane_info.modified_by'").as("modifiedBy")
                            .andExpression("'$aero_airplane_info.modified_date'").as("modifiedDate")
                            .andExpression("'$side_info.uuid'").as("sideId")
                            .andExpression("'$side_info.name'").as("sideName")
                            .andExpression("'$objectTypeInfo.type_id'").as("typeId")
                            .andExpression("'$objectTypeInfo.type_desc'").as("typeDesc")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, Countries.class, AeroResponseDTO.class);
        } else {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(aeroFilterDTO.getSort())) {
                String sortItem = aeroFilterDTO.getSort();
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
                    Aggregation.lookup("object_types", "type_id", "type_id", "objectTypeInfo"),
                    unwind("objectTypeInfo", true),
                    Aggregation.lookup("countries", "country_id", "id", "countryInfo"),
                    unwind("countryInfo", true),
             //       Aggregation.lookup("keyword_data", "uuid", "ref_id", "objectKeywordInfo"),
                    project("uuid", "name", "model", "countryId", "dimLength", "dimWidth", "dimHeight", "speedMax", "grossTonnage",
                            "payrollTime", "equipment", "permanentBase", "description", "sideId", "typeId", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$objectTypeInfo.type_desc'").as("typeDesc")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, AeroAirplaneInfo.class, AeroResponseDTO.class);
        }
        List<AeroResponseDTO> results = output.getMappedResults();
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

    private void mapKeywordDtoToResults(List<AeroResponseDTO> results) {
        Map<String, List<String>> refIdKeywordUuidsMap = keywordDataRepository.findByRefIdIn(results.stream().map(AeroResponseDTO::getUuid).collect(toList()))
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
    public AeroResponseDTO findAeroByUuid(String uuid) {
        try {
            Criteria criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code())
                    .andOperator(Criteria.where("uuid").is(uuid));

            MatchOperation matchStage = Aggregation.match(criteria);
            Aggregation aggregation = Aggregation.newAggregation(
                    matchStage,
                    Aggregation.lookup("side", "side_id", "uuid", "sideInfo"),
                    unwind("sideInfo", true),
                    Aggregation.lookup("object_types", "type_id", "type_id", "objectTypeInfo"),
                    unwind("objectTypeInfo", true),
                    Aggregation.lookup("countries", "country_id", "id", "countryInfo"),
                    unwind("countryInfo", true),
                    Aggregation.lookup("keyword_data", "uuid", "ref_id", "objectKeywordInfo"),
                    project("uuid", "name", "model", "countryId", "dimLength", "dimWidth", "dimHeight", "speedMax", "grossTonnage",
                            "payrollTime", "equipment", "permanentBase", "description", "sideId", "typeId", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$objectTypeInfo.type_desc'").as("typeDesc")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst")
            );

            AggregationResults<AeroResponseDTO> output = mongoOps.aggregate(aggregation, AeroAirplaneInfo.class, AeroResponseDTO.class);
            AeroResponseDTO result = output.getUniqueMappedResult();
            if (result != null) {

                // keywordLst
                List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(result.getKeywordUuidLst());
                List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
                result.setKeywordLst(keywordDtoLst);

                // relationshipLst
                List<ObjectRelationshipDeltailDTO> relationshipLst = new ArrayList<>();

                List<ObjectRelationship> objectRelationshipLst = objectRelationshipService.getRelationshipsBySourceObjectId(ObjectType.AIRPLANE.name(), uuid);
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

}
