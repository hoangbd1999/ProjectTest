package com.elcom.metacen.data.process.repository.impl;

import com.elcom.metacen.data.process.model.ObjectGroupConfig;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigResponseDTO;
import com.elcom.metacen.data.process.repository.CustomObjectGroupConfigRepository;
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
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;


/**
 * @author Admin
 */
@Component
public class CustomObjectGroupConfigRepositoryImpl extends BaseCustomRepositoryImpl<ObjectGroupConfig> implements CustomObjectGroupConfigRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomObjectGroupConfigRepositoryImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<ObjectGroupConfigResponseDTO> search(ObjectGroupConfigFilterDTO objectGroupConfigFilterDTO, Pageable pageable) throws ParseException {
        Criteria criteria;
        criteria = Criteria.where("id").ne(null);

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(objectGroupConfigFilterDTO.getTerm())) {
            String termReplaceAll = "";
            String term = objectGroupConfigFilterDTO.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + objectGroupConfigFilterDTO.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("name").regex(term, "i")
                   // Criteria.where("coordinates").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (!StringUtil.isNullOrEmpty(objectGroupConfigFilterDTO.getName())) {
            andCriterias.add(Criteria.where("name").regex(objectGroupConfigFilterDTO.getName(),"i"));
        }
        if (!StringUtil.isNullOrEmpty(objectGroupConfigFilterDTO.getCoordinates())) {
            andCriterias.add(Criteria.where("coordinates").regex(objectGroupConfigFilterDTO.getCoordinates(),"i"));
        }
        if (objectGroupConfigFilterDTO.getIsActive() != null && !objectGroupConfigFilterDTO.getIsActive().isEmpty()) {
            andCriterias.add(Criteria.where("isActive").in(objectGroupConfigFilterDTO.getIsActive()));
        }
        if (objectGroupConfigFilterDTO.getStartTime() != null && !objectGroupConfigFilterDTO.getStartTime().equals("")) {
            andCriterias.add(Criteria.where("startTime").gte(objectGroupConfigFilterDTO.getStartTime()));
        }
        if (objectGroupConfigFilterDTO.getEndTime() != null && !objectGroupConfigFilterDTO.getEndTime().equals("")) {
            andCriterias.add(Criteria.where("endTime").lte(objectGroupConfigFilterDTO.getEndTime()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // sortStage
        SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
        if (!StringUtil.isNullOrEmpty(objectGroupConfigFilterDTO.getSort())) {
            String sortItem = objectGroupConfigFilterDTO.getSort();
            if (sortItem.substring(0, 1).equals("-")) {
                sortStage = sort(Sort.Direction.DESC, sortItem.substring(1));
            } else {
                sortStage = sort(Sort.Direction.ASC, sortItem);
            }
        }
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                project("uuid", "name", "areaUuid", "coordinates", "isActive", "startTime", "endTime",
                        "createdBy", "createdDate", "modifiedBy", "modifiedDate"),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
        AggregationResults<ObjectGroupConfigResponseDTO> output = mongoOps.aggregate(aggregation, ObjectGroupConfig.class, ObjectGroupConfigResponseDTO.class);
        List<ObjectGroupConfigResponseDTO> results = output.getMappedResults();

        // total
        long total = mongoOps.count(Query.query(allCriteria).limit(-1).skip(-1), domain);

        return new PageImpl<>(results, pageable, total);
    }
}
