package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.repository.CustomKeywordDataRepository;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.ObjectRelationship;
import com.elcom.metacen.contact.model.Organisation;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.*;

import com.elcom.metacen.contact.repository.CustomOrganisationRepository;
import com.elcom.metacen.contact.service.CommonService;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.contact.service.ObjectRelationshipService;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.enums.OrganisationType;
import com.elcom.metacen.utils.DateUtils;
import com.elcom.metacen.utils.ObjectMapperUtils;
import com.elcom.metacen.utils.StringUtil;

import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Query;

/**
 * @author Admin
 */
@Component
public class CustomOrganisationRepositoryImpl extends BaseCustomRepositoryImpl<Organisation> implements CustomOrganisationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOrganisationRepositoryImpl.class);

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private ObjectRelationshipService objectRelationshipService;

    @Autowired
    private CommonService commonService;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    private CustomKeywordDataRepository customKeywordDataRepository;

    @Autowired
    private KeywordDataRepository keywordDataRepository;

    @Override
    public Page<OrganisationDTO> search(OrganisationFilterDTO organisationFilterDTO, Pageable pageable) {
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(organisationFilterDTO.getTerm())) {
            String termReplaceAll = "";
            String term = organisationFilterDTO.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + organisationFilterDTO.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("id").regex(term, "i"),
                    Criteria.where("name").regex(term, "i"),
                    Criteria.where("headquarters").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (organisationFilterDTO.getOrganisationTypeLst() != null && !organisationFilterDTO.getOrganisationTypeLst().isEmpty()) {
            andCriterias.add(Criteria.where("organisationType").in(organisationFilterDTO.getOrganisationTypeLst()));
        }
        if (organisationFilterDTO.getCountryIds() != null && !organisationFilterDTO.getCountryIds().isEmpty()) {
            andCriterias.add(Criteria.where("countryId").in(organisationFilterDTO.getCountryIds()));
        }
        if (organisationFilterDTO.getSideIds() != null && !organisationFilterDTO.getSideIds().isEmpty()) {
            andCriterias.add(Criteria.where("sideId").in(organisationFilterDTO.getSideIds()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // matchKeywordStage
        Criteria criteriaKeyword = new Criteria();
//        if (organisationFilterDTO.getKeywordIds() != null && !organisationFilterDTO.getKeywordIds().isEmpty()) {
//            criteriaKeyword = Criteria.where("keywordUuidLst").in(organisationFilterDTO.getKeywordIds());
//        }
        long elementsToSkip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long maxElements = pageable.getPageSize();
        AggregateKeywordDataObjectGeneralInfoDTO refObjects = null;
        if (organisationFilterDTO.getKeywordIds() != null && !organisationFilterDTO.getKeywordIds().isEmpty()) {
            refObjects = customKeywordDataRepository.findByKeywordIdsAndType(organisationFilterDTO.getKeywordIds(), 1, elementsToSkip, maxElements);
            criteriaKeyword = Criteria.where("uuid").in(refObjects.getPaginatedRefIdMap().keySet());
        }
        MatchOperation matchKeywordStage = Aggregation.match(criteriaKeyword);

        // sortStage
        AggregationResults<OrganisationDTO> output = null;
        if (organisationFilterDTO.getSort().equals("countryName") || organisationFilterDTO.getSort().equals("-countryName")) {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(organisationFilterDTO.getSort())) {
                String sortItem = organisationFilterDTO.getSort();
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
                    Aggregation.lookup("organisation", "id", "country_id", "organisation"),
                    unwind("organisation", false),
                    Aggregation.match(Criteria.where("organisation.is_deleted").is(0)),
                    Aggregation.lookup("side", "organisation.side_id", "uuid", "side_info"),
                    unwind("side_info", true),
               //     Aggregation.lookup("keyword_data", "other_vehicle.uuid", "ref_id", "objectKeywordInfo"),
                    project("id", "name")
                            .andExpression("'$id'").as("countryId")
                            .andExpression("'$name'").as("countryName")
                            .andExpression("'$organisation._id'").as("_id")
                            .andExpression("'$organisation.uuid'").as("uuid")
                            .andExpression("'$organisation.name'").as("name")
                            .andExpression("'$organisation.organisation_type'").as("organisationType")
                            .andExpression("'$organisation.headquarters'").as("headquarters")
                            .andExpression("'$organisation.description'").as("description")
                            .andExpression("'$organisation.image_lst'").as("imageLst")
                            .andExpression("'$organisation.file_attachment_lst'").as("fileAttachmentLst")
                            .andExpression("'$organisation.created_by'").as("createdBy")
                            .andExpression("'$organisation.created_date'").as("createdDate")
                            .andExpression("'$organisation.modified_by'").as("modifiedBy")
                            .andExpression("'$organisation.modified_date'").as("modifiedDate")
                            .andExpression("'$side_info.uuid'").as("sideId")
                            .andExpression("'$side_info.name'").as("sideName")
                            .andExpression("'$objectKeywordInfo.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, Countries.class, OrganisationDTO.class);
        } else {
            SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
            if (!StringUtil.isNullOrEmpty(organisationFilterDTO.getSort())) {
                String sortItem = organisationFilterDTO.getSort();
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
              //      Aggregation.lookup("keyword_data", "uuid", "ref_id", "objKeyword"),
                    project("uuid", "name", "organisationType", "countryId", "headquarters", "sideId", "description", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objKeyword.keyword_id'").as("keywordUuidLst"),
                    matchKeywordStage,
                    Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                    Aggregation.limit(pageable.getPageSize())
            );
            output = mongoOps.aggregate(aggregation, Organisation.class, OrganisationDTO.class);
        }
        List<OrganisationDTO> results = output.getMappedResults();
        if (!results.isEmpty()) {
//            results = results.stream()
//                    .map(x -> {
//                        List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(x.getKeywordUuidLst());
//                        List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
//                        x.setKeywordLst(keywordDtoLst);
//                        x.setOrganisationTypeName(OrganisationType.of(x.getOrganisationType()).description());
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

    private void mapKeywordDtoToResults(List<OrganisationDTO> results) {
        Map<String, List<String>> refIdKeywordUuidsMap = keywordDataRepository.findByRefIdIn(results.stream().map(OrganisationDTO::getUuid).collect(toList()))
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
    public OrganisationDTO findByUuid(String uuid) {
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
                    Aggregation.lookup("keyword_data", "uuid", "ref_id", "objKeyword"),
                    project("uuid", "name", "organisationType", "countryId", "headquarters", "sideId", "description", "imageLst", "fileAttachmentLst",
                            "createdBy", "createdDate", "modifiedBy", "modifiedDate")
                            .andExpression("'$sideInfo.name'").as("sideName")
                            .andExpression("'$countryInfo.name'").as("countryName")
                            .andExpression("'$objKeyword.keyword_id'").as("keywordUuidLst")
            );

            AggregationResults<OrganisationDTO> output = mongoOps.aggregate(aggregation, Organisation.class, OrganisationDTO.class);
            OrganisationDTO result = output.getUniqueMappedResult();
            if (result != null) {
                // organisationTypeName
                result.setOrganisationTypeName(OrganisationType.of(result.getOrganisationType()).description());

                // keywordLst
                List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(result.getKeywordUuidLst());
                List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
                result.setKeywordLst(keywordDtoLst);

                // relationshipLst
                List<ObjectRelationshipDeltailDTO> relationshipLst = new ArrayList<>();

                List<ObjectRelationship> objectRelationshipLst = objectRelationshipService.getRelationshipsBySourceObjectId(ObjectType.ORGANISATION.name(), uuid);
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
            LOGGER.error("findByUuid().ex: " + ex.toString());
            ex.printStackTrace();
        }

        return null;
    }
}
