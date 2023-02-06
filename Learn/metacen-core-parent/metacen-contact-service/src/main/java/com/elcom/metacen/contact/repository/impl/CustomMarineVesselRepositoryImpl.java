package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.repository.CustomKeywordDataRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.MarineVesselInfo;
import com.elcom.metacen.contact.model.ObjectRelationship;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.CustomMarineVesselRepository;
import com.elcom.metacen.contact.repository.KeywordRepository;
import com.elcom.metacen.contact.repository.MarineVesselInfoRepository;
import com.elcom.metacen.contact.service.CommonService;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.contact.service.ObjectRelationshipService;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.ObjectMapperUtils;
import com.elcom.metacen.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
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

@Component
public class CustomMarineVesselRepositoryImpl extends BaseCustomRepositoryImpl<MarineVesselInfo> implements CustomMarineVesselRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMarineVesselRepositoryImpl.class);

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private ObjectRelationshipService objectRelationshipService;

    @Autowired
    private CommonService commonService;

    @Autowired
    MarineVesselInfoRepository marineVesselInfoRepository;

    @Autowired
    private CustomKeywordDataRepository customKeywordDataRepository;

    @Autowired
    private KeywordDataRepository keywordDataRepository;

    @Override
    public Page<MarineVesselResponseDTO> search(MarineVesselFilterDTO marineVesselFilterDTO, Pageable pageable) {
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(marineVesselFilterDTO.getTerm())) {
            String termReplaceAll = "";
            String term = marineVesselFilterDTO.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + marineVesselFilterDTO.getTerm().trim() + ".*";
            }
            Long mmsi = null;
            if(StringUtils.isNumeric(term.replace(".*", "")) == true) {
                 mmsi = Long.parseLong(term.replace(".*", ""));
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("id").regex(term, "i"),
                    Criteria.where("name").regex(term, "i"),
                    Criteria.where("mmsi").is(mmsi),
                    Criteria.where("dimA").regex(term, "i"),
                    Criteria.where("dimC").regex(term, "i"),
                    Criteria.where("imo").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (!StringUtil.isNullOrEmpty(marineVesselFilterDTO.getName())) {
            String nameReplaceAll = "";
            String name = marineVesselFilterDTO.getName().trim();
            if (name.contains("(") || name.contains(")")) {
                nameReplaceAll = name.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                name = ".*"+ nameReplaceAll +".*";
            } else if (name.contains("*")) {
                nameReplaceAll = name.replaceAll("\\*","\\\\*");
                name = ".*" + nameReplaceAll +".*";
            } else {
                name = ".*" + marineVesselFilterDTO.getName().trim() + ".*";
            }
            andCriterias.add(Criteria.where("name").regex(name,"i"));
        }
        if (marineVesselFilterDTO.getMmsi() != null) {
            andCriterias.add(Criteria.where("mmsi").is(marineVesselFilterDTO.getMmsi()));
        }
        if (marineVesselFilterDTO.getCountryIds() != null && !marineVesselFilterDTO.getCountryIds().isEmpty()) {
            andCriterias.add(Criteria.where("countryId").in(marineVesselFilterDTO.getCountryIds()));
        }
        if (marineVesselFilterDTO.getSideIds() != null && !marineVesselFilterDTO.getSideIds().isEmpty()) {
            andCriterias.add(Criteria.where("sideId").in(marineVesselFilterDTO.getSideIds()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // matchKeywordStage
        Criteria criteriaKeyword = new Criteria();
//        if (marineVesselFilterDTO.getKeywordIds() != null && !marineVesselFilterDTO.getKeywordIds().isEmpty()) {
//            criteriaKeyword = Criteria.where("keywordUuidLst").in(marineVesselFilterDTO.getKeywordIds());
//        }
        long elementsToSkip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long maxElements = pageable.getPageSize();
        AggregateKeywordDataObjectGeneralInfoDTO refObjects = null;
        if (marineVesselFilterDTO.getKeywordIds() != null && !marineVesselFilterDTO.getKeywordIds().isEmpty()) {
            refObjects = customKeywordDataRepository.findByKeywordIdsAndType(marineVesselFilterDTO.getKeywordIds(), 1, elementsToSkip, maxElements);
            criteriaKeyword = Criteria.where("uuid").in(refObjects.getPaginatedRefIdMap().keySet());
        }
        MatchOperation matchKeywordStage = Aggregation.match(criteriaKeyword);

        // sortStage
        AggregationResults<MarineVesselResponseDTO> output = null;
        if (marineVesselFilterDTO.getSort().equals("countryName") || marineVesselFilterDTO.getSort().equals("-countryName")) {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(marineVesselFilterDTO.getSort())) {
                String sortItem = marineVesselFilterDTO.getSort();
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
                    Aggregation.lookup("marine_vessel_info", "id", "country_id", "marine_vessel_info"),
                    unwind("marine_vessel_info", false),
                    Aggregation.match(Criteria.where("marine_vessel_info.is_deleted").is(0)),
                    Aggregation.lookup("side", "marine_vessel_info.side_id", "uuid", "side_info"),
                    unwind("side_info", true),
                    Aggregation.lookup("object_types", "marine_vessel_info.type_id", "type_id", "objectTypeInfo"),
                    unwind("objectTypeInfo", true),
             //       Aggregation.lookup("keyword_data", "marine_vessel_info.uuid", "ref_id", "objectKeywordInfo"),
                    project("id", "name")
                            .andExpression("'$id'").as("countryId")
                            .andExpression("'$name'").as("countryName")
                            .andExpression("'$marine_vessel_info._id'").as("_id")
                            .andExpression("'$marine_vessel_info.uuid'").as("uuid")
                            .andExpression("'$marine_vessel_info.name'").as("name")
                            .andExpression("'$marine_vessel_info.mmsi'").as("mmsi")
                            .andExpression("'$marine_vessel_info.imo'").as("imo")
                            .andExpression("'$marine_vessel_info.dim_a'").as("dimA")
                            .andExpression("'$marine_vessel_info.dim_c'").as("dimC")
                            .andExpression("'$marine_vessel_info.payroll'").as("payroll")
                            .andExpression("'$marine_vessel_info.description'").as("description")
                            .andExpression("'$marine_vessel_info.equipment'").as("equipment")
                            .andExpression("'$marine_vessel_info.draught'").as("draught")
                            .andExpression("'$marine_vessel_info.gross_tonnage'").as("grossTonnage")
                            .andExpression("'$marine_vessel_info.speed_max'").as("speedMax")
                            .andExpression("'$marine_vessel_info.image_lst'").as("imageLst")
                            .andExpression("'$marine_vessel_info.file_attachment_lst'").as("fileAttachmentLst")
                            .andExpression("'$marine_vessel_info.created_by'").as("createdBy")
                            .andExpression("'$marine_vessel_info.created_date'").as("createdDate")
                            .andExpression("'$marine_vessel_info.modified_by'").as("modifiedBy")
                            .andExpression("'$marine_vessel_info.modified_date'").as("modifiedDate")
                            .andExpression("'$side_info.uuid'").as("sideId")
                            .andExpression("'$side_info.name'").as("sideName")
                            .andExpression("'$objectTypeInfo.type_id'").as("typeId")
                            .andExpression("'$objectTypeInfo.type_name'").as("typeName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, Countries.class, MarineVesselResponseDTO.class);
        } else {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(marineVesselFilterDTO.getSort())) {
                String sortItem = marineVesselFilterDTO.getSort();
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
                //    Aggregation.lookup("keyword_data", "uuid", "ref_id", "objectKeywordInfo"),
                    project("uuid", "mmsi", "name", "imo", "countryId", "typeId",
                            "dimA", "dimC", "payroll", "description", "equipment", "draught", "engineType", "grossTonnage", "speedMax", "sideId",
                            "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$objectTypeInfo.type_name'").as("typeName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, MarineVesselInfo.class, MarineVesselResponseDTO.class);
        }
        List<MarineVesselResponseDTO> results = output.getMappedResults();

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

    private void mapKeywordDtoToResults(List<MarineVesselResponseDTO> results) {
        Map<String, List<String>> refIdKeywordUuidsMap = keywordDataRepository.findByRefIdIn(results.stream().map(MarineVesselResponseDTO::getUuid).collect(toList()))
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
    public MarineVesselResponseDTO findMarineVesselByUuid(String uuid) {
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
                    project("uuid", "mmsi", "name", "imo", "countryId", "typeId",
                            "dimA", "dimC", "payroll", "description", "equipment", "draught", "engineType", "grossTonnage", "speedMax","sideId",
                            "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$objectTypeInfo.type_name'").as("typeName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst")
            );

            AggregationResults<MarineVesselResponseDTO> output = mongoOps.aggregate(aggregation, MarineVesselInfo.class, MarineVesselResponseDTO.class);
            MarineVesselResponseDTO result = output.getUniqueMappedResult();
            if (result != null) {

                // keywordLst
                List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(result.getKeywordUuidLst());
                List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
                result.setKeywordLst(keywordDtoLst);

                // relationshipLst
                List<ObjectRelationshipDeltailDTO> relationshipLst = new ArrayList<>();

                List<ObjectRelationship> objectRelationshipLst = objectRelationshipService.getRelationshipsBySourceObjectId(ObjectType.VESSEL.name(), uuid);
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

    @Override
    public List<MarineVesselInfo> findListMarineVessel(List<Integer> mmsi) {
        Criteria criteria;
        criteria = Criteria.where("mmsi").in(mmsi);
        Query query = new Query().addCriteria(criteria);
        List<MarineVesselInfo> listMarine = mongoOps.find(query, domain);
        return listMarine;
    }
}
