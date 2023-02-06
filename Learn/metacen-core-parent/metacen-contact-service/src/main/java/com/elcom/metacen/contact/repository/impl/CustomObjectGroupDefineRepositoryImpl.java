package com.elcom.metacen.contact.repository.impl;

import com.elcom.metacen.contact.model.ObjectGroupDefine;

import com.elcom.metacen.contact.model.ObjectTypes;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineMappingDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineResponseDTO;
import com.elcom.metacen.contact.repository.CustomObjectGroupDefineRepository;

import com.elcom.metacen.contact.repository.ObjectTypesRepository;
import com.elcom.metacen.enums.DataDeleteStatus;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;


/**
 * @author Admin
 */
@Component
public class CustomObjectGroupDefineRepositoryImpl extends BaseCustomRepositoryImpl<ObjectGroupDefine> implements CustomObjectGroupDefineRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomObjectGroupDefineRepositoryImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private ObjectTypesRepository objectTypesRepository;

    @Override
    public Page<ObjectGroupDefineResponseDTO> search(ObjectGroupDefineFilterDTO objectGroupDefineFilterDTO, Pageable pageable){
        Criteria criteria;
        criteria = Criteria.where("isDeleted").is(DataDeleteStatus.NOT_DELETED.code());

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(objectGroupDefineFilterDTO.getTerm())) {
            String term = searchSpecialCharacter(objectGroupDefineFilterDTO.getTerm().trim());
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("name").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);
        SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                project("uuid", "name", "note", "objects", "isDeleted",
                        "createdBy", "createdDate", "modifiedBy", "modifiedDate"),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
        AggregationResults<ObjectGroupDefineResponseDTO> output = mongoOps.aggregate(aggregation, ObjectGroupDefine.class, ObjectGroupDefineResponseDTO.class);
        List<ObjectGroupDefineResponseDTO> results = output.getMappedResults();
        if (!results.isEmpty()) {
            results = results.stream()
                    .map(x -> {
                        List<ObjectGroupDefineMappingDTO> listData = x.getObjects();
                        for (int i = 0; i < listData.size(); i++) {
                            ObjectTypes objectTypes = objectTypesRepository.findByTypeIdAndIsDeleted(listData.get(i).getTypeId(),DataDeleteStatus.NOT_DELETED.code());
                            listData.get(i).setTypeName(objectTypes.getTypeName());
                            x.setObjects(listData);
                            x.setCountNumber(listData.size());
                        }
                        return x;
                    })
                    .collect(Collectors.toList());
        }
        // total
        long total = mongoOps.count(Query.query(allCriteria).limit(-1).skip(-1), domain);
        if(results.isEmpty()){
            total = 0;
        }
        return new PageImpl<>(results, pageable, total);
    }


    private String searchSpecialCharacter(String keyword){
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
}
