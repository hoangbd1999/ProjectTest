package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.ObjectGroup;
import com.elcom.metacen.contact.model.ObjectGroupMapping;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupConfirmedFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupUnconfirmedFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupMappingDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupResponseDTO;
import com.elcom.metacen.contact.repository.CustomObjectGroupMappingRepository;
import com.elcom.metacen.contact.repository.CustomObjectGroupRepository;
import com.elcom.metacen.contact.repository.ObjectGroupMappingRepository;
import com.elcom.metacen.contact.repository.ObjectGroupRepository;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.utils.ObjectMapperUtils;
import com.elcom.metacen.utils.StringUtil;
import org.modelmapper.ModelMapper;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;


/**
 * @author Admin
 */
@Component
public class CustomObjectGroupRepositoryImpl extends BaseCustomRepositoryImpl<ObjectGroup> implements CustomObjectGroupRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomObjectGroupRepositoryImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private ObjectGroupMappingRepository objectGroupMappingRepository;

    @Autowired
    private CustomObjectGroupMappingRepository customObjectGroupMappingRepository;

    @Autowired
    private ObjectGroupRepository objectGroupRepository;

    @Override
    public Page<ObjectGroupResponseDTO> search(ObjectGroupUnconfirmedFilterDTO objectGroupUnconfirmedFilterDTO, Pageable pageable) throws ParseException {
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
//        if (!StringUtil.isNullOrEmpty(objectGroupUnconfirmedFilterDTO.getTerm())) {
//            String term = searchSpecialCharacter(objectGroupUnconfirmedFilterDTO.getTerm().trim());
//            Criteria termCriteria = new Criteria();
//            termCriteria.orOperator(
//                    Criteria.where("configName").regex(term, "i")
//            );
//            andCriterias.add(termCriteria);
//        }
        andCriterias.add(Criteria.where("isConfirmed").is(DataDeleteStatus.NOT_DELETED.code()));
        if (!StringUtil.isNullOrEmpty(objectGroupUnconfirmedFilterDTO.getConfigName())) {
            String configName = searchSpecialCharacter(objectGroupUnconfirmedFilterDTO.getConfigName().trim());
            andCriterias.add(Criteria.where("configName").regex(configName, "i"));
        }
        if (objectGroupUnconfirmedFilterDTO.getFromTime() != null && !objectGroupUnconfirmedFilterDTO.getFromTime().equals("")) {
            andCriterias.add(Criteria.where("createdDate").gte(objectGroupUnconfirmedFilterDTO.getFromTime()));
        }
        if (objectGroupUnconfirmedFilterDTO.getToTime() != null && !objectGroupUnconfirmedFilterDTO.getToTime().equals("")) {
            andCriterias.add(Criteria.where("createdDate").lte(objectGroupUnconfirmedFilterDTO.getToTime()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);
        SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
        // matchTermObject
        Criteria criteriaTerm = new Criteria();
        List<ObjectGroupMappingDTO> refObjects = null;
        List<String> listGroupId = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(objectGroupUnconfirmedFilterDTO.getTermObject())) {
            refObjects = customObjectGroupMappingRepository.findByObjIdAndObjName(objectGroupUnconfirmedFilterDTO.getTermObject());
            if (refObjects != null) {
                for (int i = 0; i < refObjects.size(); i++) {
                    listGroupId.add(refObjects.get(i).getGroupId());
                }
                List<ObjectGroupResponseDTO> checkUnConfirmed = checkUnConfirmed(listGroupId);
                listGroupId.clear();
                for (int i = 0; i < checkUnConfirmed.size(); i++) {
                    listGroupId.add(checkUnConfirmed.get(i).getUuid());
                }
                Set<String> set = new HashSet<>(listGroupId);
                listGroupId.clear();
                listGroupId.addAll(set);
                Criteria termCriteria = Criteria.where("uuid").in(listGroupId);
                criteriaTerm = termCriteria;
            }
        }
        // match objId and objName of term
        Criteria searchTerm = new Criteria();
        List<ObjectGroupMappingDTO> searchObjects = null;
        List<String> listId = new ArrayList<>();

        if (!StringUtil.isNullOrEmpty(objectGroupUnconfirmedFilterDTO.getTerm())) {
            String term = searchSpecialCharacter(objectGroupUnconfirmedFilterDTO.getTerm().trim());
            searchObjects = customObjectGroupMappingRepository.findByObjIdAndObjName(term);
            if (searchObjects != null) {
                for (int i = 0; i < searchObjects.size(); i++) {
                    listId.add(searchObjects.get(i).getGroupId());
                }
                List<ObjectGroupResponseDTO> checkUnConfirmed = checkUnConfirmed(listId);
                listId.clear();
                for (int i = 0; i < checkUnConfirmed.size(); i++) {
                    listId.add(checkUnConfirmed.get(i).getUuid());
                }
                Set<String> set = new HashSet<>(listId);
                listId.clear();
                listId.addAll(set);
                Criteria searchCriteria = new Criteria();
                searchCriteria.orOperator(
                        Criteria.where("configName").regex(term, "i"),
                        Criteria.where("uuid").in(listId)
                );

                searchTerm = searchCriteria;
            }
        }
        MatchOperation matchTermStage = Aggregation.match(criteriaTerm);
        MatchOperation matchSearchTermStage = Aggregation.match(searchTerm);
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                project("uuid", "name", "note", "configName", "configUuid", "isConfirmed", "configTogetherTime", "configDistanceLevel", "isDeleted", "confirmDate", "eventTimes",
                        "updatedAt", "updatedBy", "firstTogetherTime", "mappingPairInfos", "lastTogetherTime", "createdBy", "createdDate", "modifiedBy", "modifiedDate"),
                matchTermStage,
                matchSearchTermStage,
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
        AggregationResults<ObjectGroupResponseDTO> output = mongoOps.aggregate(aggregation, ObjectGroup.class, ObjectGroupResponseDTO.class);
        List<ObjectGroupResponseDTO> results = output.getMappedResults();
        if (!results.isEmpty()) {

            List<ObjectGroupResponseDTO> finalResults = results;
            results = results.stream()
                    .map(x -> {
                        List<ObjectGroupMapping> ObjectGroupMappingLst = objectGroupMappingRepository.findByGroupIdInAndIsDeleted(x.getUuid(), DataDeleteStatus.NOT_DELETED.code());
                        List<ObjectGroupMappingDTO> objectGroupDtoLst = ObjectMapperUtils.mapAll(ObjectGroupMappingLst, ObjectGroupMappingDTO.class);
                        for (ObjectGroupMappingDTO listData : objectGroupDtoLst) {
                            List<ObjectGroupMapping> ObjectGroupMappingList = objectGroupMappingRepository.findByObjIdInAndIsDeleted(listData.getObjId(), DataDeleteStatus.NOT_DELETED.code());
                            //   listData.setGroupName(x.getName());
//                            List<ObjectGroupMapping> filtered = ObjectGroupMappingList.stream()
//                                    .filter(n -> groupId.add(n.getGroupId()))
//                                    .collect(Collectors.toList());
                            List<String> groupName = new ArrayList<>();
                            List<String> groupId = new ArrayList<>();
                            for (ObjectGroupMapping objectGroupMappingList : ObjectGroupMappingList) {
                                groupId.add(objectGroupMappingList.getGroupId());
                            }
                            List<ObjectGroupResponseDTO> resultList = findUuid(groupId);
                            for (int i = 0; i < resultList.size(); i++) {
                                groupName.add(resultList.get(i).getName());
                            }
                            listData.setGroupName(groupName);
                        }
                        x.setObjects(objectGroupDtoLst);
                        return x;
                    })
                    .collect(Collectors.toList());
        }
        // total
        long total = mongoOps.count(Query.query(allCriteria).limit(-1).skip(-1), domain);
        if (results.isEmpty()) {
            total = 0;
        } else if (refObjects != null) {
            total = listGroupId.size();
        } else if (searchObjects != null) {
            total = listId.size();
        }
        return new PageImpl<>(results, pageable, total);
    }


    @Override
    public Page<ObjectGroupResponseDTO> search(ObjectGroupConfirmedFilterDTO objectGroupConfirmedFilterDTO, Pageable pageable) throws ParseException {
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(objectGroupConfirmedFilterDTO.getTerm())) {
            String term = searchSpecialCharacter(objectGroupConfirmedFilterDTO.getTerm().trim());
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("configName").regex(term, "i"),
                    Criteria.where("name").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        andCriterias.add(Criteria.where("isConfirmed").is(DataDeleteStatus.DELETED.code()));
        if (!StringUtil.isNullOrEmpty(objectGroupConfirmedFilterDTO.getName())) {
            String name = searchSpecialCharacter(objectGroupConfirmedFilterDTO.getName().trim());
            andCriterias.add(Criteria.where("name").regex(name, "i"));
        }
        if (!StringUtil.isNullOrEmpty(objectGroupConfirmedFilterDTO.getConfigName())) {
            String configName = searchSpecialCharacter(objectGroupConfirmedFilterDTO.getConfigName().trim());
            andCriterias.add(Criteria.where("configName").regex(configName, "i"));
        }
        if (objectGroupConfirmedFilterDTO.getFromTime() != null && !objectGroupConfirmedFilterDTO.getFromTime().equals("")) {
            andCriterias.add(Criteria.where("confirmDate").gte(objectGroupConfirmedFilterDTO.getFromTime()));
        }
        if (objectGroupConfirmedFilterDTO.getToTime() != null && !objectGroupConfirmedFilterDTO.getToTime().equals("")) {
            andCriterias.add(Criteria.where("confirmDate").lte(objectGroupConfirmedFilterDTO.getToTime()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);
        SortOperation sortStage = sort(Sort.Direction.DESC, "confirm_date");
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                Aggregation.lookup("object_group_mapping", "uuid", "group_id", "objectGroupInfo"),
                project("uuid", "name", "note", "configName", "configUuid", "isConfirmed", "configTogetherTime", "configDistanceLevel", "isDeleted", "confirmDate", "eventTimes",
                        "updatedAt", "updatedBy", "firstTogetherTime", "mappingPairInfos", "lastTogetherTime", "createdBy", "createdDate", "modifiedBy", "modifiedDate"),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
        AggregationResults<ObjectGroupResponseDTO> output = mongoOps.aggregate(aggregation, ObjectGroup.class, ObjectGroupResponseDTO.class);
        List<ObjectGroupResponseDTO> results = output.getMappedResults();
        if (!results.isEmpty()) {
            List<ObjectGroupResponseDTO> finalResults = results;
            results = results.stream()
                    .map(x -> {
                        List<ObjectGroupMapping> ObjectGroupMappingLst = objectGroupMappingRepository.findByGroupIdInAndIsDeleted(x.getUuid(), DataDeleteStatus.NOT_DELETED.code());
                        List<ObjectGroupMappingDTO> objectGroupDtoLst = ObjectMapperUtils.mapAll(ObjectGroupMappingLst, ObjectGroupMappingDTO.class);
//                        for (int i = 0; i < objectGroupDtoLst.size(); i++) {
//                            for (int j = 0; j < finalResults.size(); j++) {
//                                if(objectGroupDtoLst.get(i).getGroupId().equalsIgnoreCase(finalResults.get(j).getUuid())) {
//                                    objectGroupDtoLst.get(i).setGroupName(finalResults.get(j).getName());
//                                }
//                            }
//                        }
                        x.setObjects(objectGroupDtoLst);
                        x.setCountNumber(ObjectGroupMappingLst.size());
                        return x;
                    })
                    .collect(Collectors.toList());
        }
        // total
        long total = mongoOps.count(Query.query(allCriteria).limit(-1).skip(-1), domain);

        return new PageImpl<>(results, pageable, total);
    }

    private String searchSpecialCharacter(String keyword) {
        if (!StringUtil.isNullOrEmpty(keyword)) {
            String termReplaceAll = "";
            String term = keyword.trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
                return term = ".*" + termReplaceAll + ".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*", "\\\\*");
                return term = ".*" + termReplaceAll + ".*";
            } else {
                return term = ".*" + keyword.trim() + ".*";
            }
        }
        return null;
    }


    public List<ObjectGroupResponseDTO> checkUnConfirmed(List<String> groupId) {
        Criteria criteria;
        criteria = Criteria.where("isConfirmed").is(DataDeleteStatus.NOT_DELETED.code());

        Criteria objectCriteria = Criteria.where("uuid").in(groupId);
        MatchOperation match = Aggregation.match(criteria);
        MatchOperation matchTermStage = Aggregation.match(objectCriteria);
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                project("uuid"),
                matchTermStage);
        AggregationResults<ObjectGroupResponseDTO> output = mongoOps.aggregate(aggregation, ObjectGroup.class, ObjectGroupResponseDTO.class);
        List<ObjectGroupResponseDTO> results = output.getMappedResults();
        return results;
    }

    public List<ObjectGroupResponseDTO> findUuid(List<String> groupId) {
        Criteria criteria;
        criteria = Criteria.where("isConfirmed").is(DataDeleteStatus.DELETED.code());

        Criteria objectCriteria = Criteria.where("uuid").in(groupId);
        MatchOperation match = Aggregation.match(criteria);
        MatchOperation matchTermStage = Aggregation.match(objectCriteria);
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                project("uuid", "name"),
                matchTermStage);
        AggregationResults<ObjectGroupResponseDTO> output = mongoOps.aggregate(aggregation, ObjectGroup.class, ObjectGroupResponseDTO.class);
        List<ObjectGroupResponseDTO> results = output.getMappedResults();
        return results;
    }
}
