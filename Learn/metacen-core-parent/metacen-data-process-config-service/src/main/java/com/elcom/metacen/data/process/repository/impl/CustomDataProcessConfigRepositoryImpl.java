package com.elcom.metacen.data.process.repository.impl;

import com.elcom.metacen.data.process.model.DataProcessConfig;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigResponseDTO;
import com.elcom.metacen.data.process.repository.CustomDataProcessConfigRepository;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;


/**
 * @author Admin
 */
@Component
public class CustomDataProcessConfigRepositoryImpl extends BaseCustomRepositoryImpl<DataProcessConfig> implements CustomDataProcessConfigRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDataProcessConfigRepositoryImpl.class);

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<DataProcessConfigResponseDTO> search(DataProcessConfigFilterDTO dataProcessConfigFilterDTO, Pageable pageable) throws ParseException {
        Criteria criteria;
        criteria = Criteria.where("id").ne(null);

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(dataProcessConfigFilterDTO.getTerm())) {
            String termReplaceAll = "";
            String term = dataProcessConfigFilterDTO.getTerm().trim();
            if (term.contains("(") || term.contains(")")) {
                termReplaceAll = term.replaceAll("\\(","\\\\(").replaceAll("\\)", "\\\\)");
                term = ".*"+ termReplaceAll +".*";
            } else if (term.contains("*")) {
                termReplaceAll = term.replaceAll("\\*","\\\\*");
                term = ".*" + termReplaceAll +".*";
            } else {
                term = ".*" + dataProcessConfigFilterDTO.getTerm().trim() + ".*";
            }
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("name").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (dataProcessConfigFilterDTO.getDataTypes() != null && !dataProcessConfigFilterDTO.getDataTypes().isEmpty()) {
            andCriterias.add(Criteria.where("dataType").in(dataProcessConfigFilterDTO.getDataTypes()));
        }
        if (dataProcessConfigFilterDTO.getProcessTypes() != null && !dataProcessConfigFilterDTO.getProcessTypes().isEmpty()) {
            andCriterias.add(Criteria.where("processType").in(dataProcessConfigFilterDTO.getProcessTypes()));
        }
        if (dataProcessConfigFilterDTO.getDataVendors() != null && !dataProcessConfigFilterDTO.getDataVendors().isEmpty()) {
            andCriterias.add(Criteria.where("dataVendor").in(dataProcessConfigFilterDTO.getDataVendors()));
        }
        if (dataProcessConfigFilterDTO.getStatus() != null && !dataProcessConfigFilterDTO.getStatus().isEmpty()) {
            andCriterias.add(Criteria.where("status").in(dataProcessConfigFilterDTO.getStatus()));
        }
        if (dataProcessConfigFilterDTO.getStartTime() != null && !dataProcessConfigFilterDTO.getStartTime().equals("")) {
            andCriterias.add(Criteria.where("startTime").gte(dataProcessConfigFilterDTO.getStartTime()));
        }
        if (dataProcessConfigFilterDTO.getEndTime() != null && !dataProcessConfigFilterDTO.getEndTime().equals("")) {
            andCriterias.add(Criteria.where("endTime").lte(dataProcessConfigFilterDTO.getEndTime()));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // sortStage
        SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
        if (!StringUtil.isNullOrEmpty(dataProcessConfigFilterDTO.getSort())) {
            String sortItem = dataProcessConfigFilterDTO.getSort();
            if (sortItem.substring(0, 1).equals("-")) {
                sortStage = sort(Sort.Direction.DESC, sortItem.substring(1));
            } else {
                sortStage = sort(Sort.Direction.ASC, sortItem);
            }
        }
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                project("uuid", "name", "dataType", "processType", "dataVendor", "detailConfig", "status", "startTime", "endTime",
                        "createdBy", "createdDate", "modifiedBy", "modifiedDate"),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
        AggregationResults<DataProcessConfigResponseDTO> output = mongoOps.aggregate(aggregation, DataProcessConfig.class, DataProcessConfigResponseDTO.class);
        List<DataProcessConfigResponseDTO> results = output.getMappedResults();

        // total
        long total = mongoOps.count(Query.query(allCriteria).limit(-1).skip(-1), domain);

        return new PageImpl<>(results, pageable, total);
    }
}
