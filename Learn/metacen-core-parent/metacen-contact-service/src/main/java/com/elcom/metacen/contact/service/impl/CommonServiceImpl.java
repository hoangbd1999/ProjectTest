package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.converter.CustomProjectAggregationOperation;
import com.elcom.metacen.contact.model.*;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.repository.*;
import com.elcom.metacen.contact.repository.rsql.KeywordDataRepository;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.elcom.metacen.contact.service.CommonService;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.dto.redis.VsatDataSource;
import com.elcom.metacen.dto.redis.VsatVesselType;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.utils.ObjectMapperUtils;
import com.elcom.metacen.utils.StringUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * @author Admin
 */
@Service
public class CommonServiceImpl implements CommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonServiceImpl.class);

    @Autowired
    protected MongoOperations mongoOps;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    CountriesRepository countriesRepository;

    @Autowired
    private ObjectTypesRepository objectTypesRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private AeroAirplaneInfoRepository aeroAirplaneInfoRepository;

    @Autowired
    private MarineVesselInfoRepository marineVesselInfoRepository;

    @Autowired
    private OtherVehicleRepository otherVehicleRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AreasRepository areasRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private OtherObjectRepository otherObjectRepository;

    @Autowired
    private CustomKeywordDataRepository customKeywordDataRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KeywordDataRepository keywordDataRepository;

    @Override
    public List<Countries> getListCountries() {
        List<Countries> lst = null;
        String key = Constant.REDIS_COUNTRIES_LST_KEY;
        try {
            if (this.redisTemplate.hasKey(key)) {
                lst = (List<Countries>) this.redisTemplate.opsForList()
                        .range(key, 0, Constant.REDIS_COUNTRIES_LST_FETCH_MAX);
            } else {
                lst = (List<Countries>) this.countriesRepository.findAllByOrderByNameAsc();
                if (lst != null && !lst.isEmpty()) {
                    for (Countries c : lst) {
                        c.setName("(" + c.getCountryId() + ") " + c.getName());
                    }

                    Long pushValStatus = this.redisTemplate.opsForList().rightPushAll(key, lst);
                    if (pushValStatus != null && !pushValStatus.equals(0L)) {
                        this.redisTemplate.expire(key, 30, TimeUnit.DAYS);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return lst;
    }

    @Override
    public List<ObjectTypes> getListObjectTypes() {
        List<ObjectTypes> lst = null;
        String key = Constant.REDIS_OBJECT_TYPE_LST_KEY;
        try {
            if (this.redisTemplate.hasKey(key)) {
                lst = (List<ObjectTypes>) this.redisTemplate.opsForList()
                        .range(key, 0, Constant.REDIS_OBJECT_TYPE_LST_FETCH_MAX);
            } else {
                lst = this.objectTypesRepository.findAllByIsDeletedOrderByTypeNameAsc(DataDeleteStatus.NOT_DELETED.code());
                if (lst != null && !lst.isEmpty()) {
                    Long pushValStatus = this.redisTemplate.opsForList().rightPushAll(key, lst);
                    LOGGER.info("Redis pushValStatus return: " + pushValStatus);
                    if (pushValStatus != null && !pushValStatus.equals(0L)) {
                        LOGGER.info("Redis set expired return " + this.redisTemplate.expire(key, 300, TimeUnit.SECONDS));
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return lst;
    }

    @Override
    public Map<String, ObjectGeneralInfoDTO> buildObjectGeneralInfoMap(String destObjectType, List<String> destObjectIds) {
        Map<String, ObjectGeneralInfoDTO> objectGeneralInfoMap = new HashMap<>();

        // PEOPLE
        if (destObjectType.equalsIgnoreCase(ObjectType.PEOPLE.name())) {
            List<People> peopleLst = peopleRepository.findByUuidIn(destObjectIds);
            for (People item : peopleLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // ORGANISATION
        if (destObjectType.equalsIgnoreCase(ObjectType.ORGANISATION.name())) {
            List<Organisation> organisationLst = organisationRepository.findByUuidIn(destObjectIds);
            for (Organisation item : organisationLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // AIRPLANE
        if (destObjectType.equalsIgnoreCase(ObjectType.AIRPLANE.name())) {
            List<AeroAirplaneInfo> aeroAirplaneLst = aeroAirplaneInfoRepository.findByUuidIn(destObjectIds);
            for (AeroAirplaneInfo item : aeroAirplaneLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // VESSEL
        if (destObjectType.equalsIgnoreCase(ObjectType.VESSEL.name())) {
            List<MarineVesselInfo> marineVesselLst = marineVesselInfoRepository.findByUuidIn(destObjectIds);
            for (MarineVesselInfo item : marineVesselLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // OTHER_VEHICLE
        if (destObjectType.equalsIgnoreCase(ObjectType.OTHER_VEHICLE.name())) {
            List<OtherVehicle> otherVehicleLst = otherVehicleRepository.findByUuidIn(destObjectIds);
            for (OtherVehicle item : otherVehicleLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // EVENT
        if (destObjectType.equalsIgnoreCase(ObjectType.EVENT.name())) {
            List<Event> eventLst = eventRepository.findByUuidIn(destObjectIds);
            for (Event item : eventLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // AREA
        if (destObjectType.equalsIgnoreCase(ObjectType.AREA.name())) {
            List<Areas> areaLst = areasRepository.findByUuidIn(destObjectIds);
            for (Areas item : areaLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // RESOURCES
        if (destObjectType.equalsIgnoreCase(ObjectType.RESOURCES.name())) {
        }

        // INFRASTRUCTURE
        if (destObjectType.equalsIgnoreCase(ObjectType.INFRASTRUCTURE.name())) {
            List<Infrastructure> infrastructureLst = infrastructureRepository.findByUuidIn(destObjectIds);
            for (Infrastructure item : infrastructureLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        // OTHER_OBJECT
        if (destObjectType.equalsIgnoreCase(ObjectType.OTHER_OBJECT.name())) {
            List<OtherObject> otherObjectLst = otherObjectRepository.findByUuidInAndIsDeleted(destObjectIds, DataDeleteStatus.NOT_DELETED.code());
            for (OtherObject item : otherObjectLst) {
                ObjectGeneralInfoDTO objectGeneralInfoDTO = buildObjectGeneralInfoDTO(destObjectType, item.getId(), item.getUuid(), item.getName(), item.getSideId(), item.getIsDeleted());
                objectGeneralInfoMap.put(item.getUuid(), objectGeneralInfoDTO);
            }
        }

        return objectGeneralInfoMap;
    }

    @Override
    public Page<ObjectGeneralInfoDTO> filterObject(ObjectCriteria objectCriteria) {
        Integer page = objectCriteria.getPage() > 0 ? objectCriteria.getPage() : 0;
        Pageable pageable = PageRequest.of(page, objectCriteria.getSize());

        List<Criteria> andCriteriasOne = new ArrayList<>();
        List<Criteria> andCriteriasTwo = new ArrayList<>();

        if (objectCriteria.getObjectTypeLst() != null && !objectCriteria.getObjectTypeLst().isEmpty()) {
            List<String> objectTypeLst = objectCriteria.getObjectTypeLst();
            if (objectTypeLst.stream().anyMatch(ObjectType.VEHICLE.name()::equals)) {
                List<String> objectTypeNewList = new ArrayList<String>(objectTypeLst);
                objectTypeNewList.remove(ObjectType.VEHICLE.name());
                objectTypeNewList.addAll(Arrays.asList(ObjectType.AIRPLANE.name(), ObjectType.VESSEL.name(), ObjectType.OTHER_VEHICLE.name()));

                andCriteriasOne.add(Criteria.where("objectType").in(objectTypeNewList));
            } else {
                andCriteriasOne.add(Criteria.where("objectType").in(objectTypeLst));
            }
        }
        if(objectCriteria.getKeyId() != null && !objectCriteria.getKeyId().isEmpty()){
            andCriteriasOne.add(Criteria.where("uuid").nin(objectCriteria.getKeyId()));
        }
        if (objectCriteria.getCountryIds() != null && !objectCriteria.getCountryIds().isEmpty()) {
            andCriteriasOne.add(Criteria.where("country_id").in(objectCriteria.getCountryIds()));
        }
        if (objectCriteria.getSideIds() != null && !objectCriteria.getSideIds().isEmpty()) {
            andCriteriasOne.add(Criteria.where("side_id").in(objectCriteria.getSideIds()));
        }

        if (!StringUtil.isNullOrEmpty(objectCriteria.getTerm())) {
            String termReplaceAll = "";
            String term = objectCriteria.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + objectCriteria.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("id").regex(term, "i"),
                    Criteria.where("name").regex(term, "i"),
                    Criteria.where("sideName").regex(term, "i")
            );
            andCriteriasTwo.add(termCriteria);
        }
//        if (objectCriteria.getKeywordIds() != null && !objectCriteria.getKeywordIds().isEmpty()) {
//            andCriteriasTwo.add(Criteria.where("keywordUuidLst").in(objectCriteria.getKeywordIds()));
//        }

        long elementsToSkip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long maxElements = pageable.getPageSize();

        AggregateKeywordDataObjectGeneralInfoDTO refObjects = null;
        if (objectCriteria.getKeywordIds() != null && !objectCriteria.getKeywordIds().isEmpty()) {
            refObjects = customKeywordDataRepository.findByKeywordIdsAndType(objectCriteria.getKeywordIds(), 1, elementsToSkip, maxElements);
            Criteria keywordCriteria = Criteria.where("uuid").in(refObjects.getPaginatedRefIdMap().keySet());
            andCriteriasTwo.add(keywordCriteria);
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
        aggregationOperations.add(project("objectType", "id", "uuid", "name", "side_id", "country_id"));
        aggregationOperations.add(matchOneStage);
        aggregationOperations.add(Aggregation.lookup("side", "side_id", "uuid", "sideInfo"));
        aggregationOperations.add(unwind("sideInfo", true));
//      aggregationOperations.add(Aggregation.lookup("keyword_data", "uuid", "ref_id", "objKeyword"));
        aggregationOperations.add(project("objectType", "id", "uuid", "name").andExpression("'$country_id'").as("countryId").andExpression("'$side_id'").as("sideId").andExpression("'$sideInfo.name'").as("sideName"));
        aggregationOperations.add(matchTwoStage);
   //     aggregationOperations.add(sort(Sort.Direction.ASC, "name"));
        aggregationOperations.add(new CustomProjectAggregationOperation(query11));
        aggregationOperations.add(new CustomProjectAggregationOperation(query12));
        TypedAggregation<People> aggregation = Aggregation.newAggregation(
                People.class,
                aggregationOperations
        );

        AggregationResults<AggregateObjectGeneralInfoDTO> output = mongoOps.aggregate(aggregation, AggregateObjectGeneralInfoDTO.class);
        List<AggregateObjectGeneralInfoDTO> aggregateObjectGeneralInfoDTOLst = output.getMappedResults();

        List<ObjectGeneralInfoDTO> results = new ArrayList<>();
        long total = 0;
        if (!aggregateObjectGeneralInfoDTOLst.isEmpty()) {
            results = aggregateObjectGeneralInfoDTOLst.get(0).getPaginatedResults();
//            total = aggregateObjectGeneralInfoDTOLst.get(0).getTotalCount().getCount();
            total = refObjects != null && refObjects.getTotalCount() != null ? refObjects.getTotalCount().getCount() : aggregateObjectGeneralInfoDTOLst.get(0).getTotalCount().getCount();

//            results = results.stream()
//                    .map(x -> {
//                        List<Keyword> keywordLst = keywordService.findKeywordsByUuidList(x.getKeywordUuidLst());
//                        List<KeywordDTO> keywordDtoLst = ObjectMapperUtils.mapAll(keywordLst, KeywordDTO.class);
//                        x.setKeywordLst(keywordDtoLst);
//
//                        return x;
//                    })
//                    .collect(Collectors.toList());

            mapKeywordDtoToResults(results);
        }

        return new PageImpl<>(results, pageable, total);
    }

    private void mapKeywordDtoToResults(List<ObjectGeneralInfoDTO> results) {
        Map<String, List<String>> refIdKeywordUuidsMap = keywordDataRepository.findByRefIdIn(results.stream().map(ObjectGeneralInfoDTO::getUuid).collect(toList()))
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
    public List<VsatVesselType> getListVesselType() {
        List<VsatVesselType> lst = null;
        String key = Constant.REDIS_VESSEL_LST_KEY;
        try {
            if (this.redisTemplate.hasKey(key))
                lst = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return lst;
    }

    @Override
    public List<VsatDataSource> getListVsatDataSource() {
        List<VsatDataSource> lst = null;
        String key = Constant.REDIS_VSAT_DATA_SOURCE_LST_KEY;
        try {
            if (this.redisTemplate.hasKey(key)) {
                lst = (List<VsatDataSource>) this.redisTemplate.opsForList()
                        .range(key, 0, Constant.REDIS_VSAT_DATA_SOURCE_LST_FETCH_MAX);
            }
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return lst;
    }

    private ObjectGeneralInfoDTO buildObjectGeneralInfoDTO(String destObjectType, String id, String uuid, String name, String sideId, int isDeleted) {
        ObjectGeneralInfoDTO objectGeneralInfoDTO = new ObjectGeneralInfoDTO();
        objectGeneralInfoDTO.setObjectType(destObjectType);
        objectGeneralInfoDTO.setId(id);
        objectGeneralInfoDTO.setUuid(uuid);
        objectGeneralInfoDTO.setName(name);
        objectGeneralInfoDTO.setSideId(sideId);
        objectGeneralInfoDTO.setIsDeleted(isDeleted);

        return objectGeneralInfoDTO;
    }
}
