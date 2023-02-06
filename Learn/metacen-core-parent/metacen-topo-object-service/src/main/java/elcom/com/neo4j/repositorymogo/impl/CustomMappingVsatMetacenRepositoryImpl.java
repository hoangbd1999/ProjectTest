package elcom.com.neo4j.repositorymogo.impl;

import elcom.com.neo4j.dto.MappingVsatFilterDTO;
import elcom.com.neo4j.dto.MappingVsatResponseDTO;
import elcom.com.neo4j.model.MappingVsatMetacen;
import elcom.com.neo4j.repositorymogo.CustomMappingVsatMetacenRepository;
import elcom.com.neo4j.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;


/**
 * @author Admin
 */
@Component
public class CustomMappingVsatMetacenRepositoryImpl extends BaseCustomRepositoryImpl<MappingVsatMetacen> implements CustomMappingVsatMetacenRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMappingVsatMetacenRepositoryImpl.class);

    @Override
    public Page<MappingVsatResponseDTO> search(MappingVsatFilterDTO mappingVsatFilterDTO, Pageable pageable) {
        Criteria criteria;
        criteria = Criteria.where("id").ne(null);

        List<Criteria> andCriterias = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(mappingVsatFilterDTO.getTerm())) {
            String term = ".*" + mappingVsatFilterDTO.getTerm().trim() + ".*";
            Criteria termCriteria = new Criteria();
            termCriteria.orOperator(
                    Criteria.where("vsatDataSourceName").regex(term, "i"),
                    Criteria.where("vsatIpAddress").regex(term, "i"),
                    Criteria.where("objectType").regex(term, "i"),
                    Criteria.where("objectId").regex(term, "i"),
                    Criteria.where("objectName").regex(term, "i")
            );
            andCriterias.add(termCriteria);
        }
        if (mappingVsatFilterDTO.getVsatDataSourceIds() != null && !mappingVsatFilterDTO.getVsatDataSourceIds().isEmpty()) {
            andCriterias.add(Criteria.where("vsatDataSourceId").in(mappingVsatFilterDTO.getVsatDataSourceIds()));
        }
        if (mappingVsatFilterDTO.getVsatIpAddress() != null && !mappingVsatFilterDTO.getVsatIpAddress().isEmpty()) {
            andCriterias.add(Criteria.where("vsatIpAddress").in(mappingVsatFilterDTO.getVsatIpAddress()));
        }
        if (mappingVsatFilterDTO.getObjectTypes() != null && !mappingVsatFilterDTO.getObjectTypes().isEmpty()) {
            andCriterias.add(Criteria.where("objectType").in(mappingVsatFilterDTO.getObjectTypes()));
        }
        if (!StringUtil.isNullOrEmpty(mappingVsatFilterDTO.getObjectId())) {
            andCriterias.add(Criteria.where("objectId").regex(".*" + mappingVsatFilterDTO.getObjectId().trim() + ".*", "i"));
        }

        Criteria allCriteria = criteria;
        if (andCriterias.size() > 0) {
            allCriteria = allCriteria.andOperator(andCriterias.stream().toArray(Criteria[]::new));
        }

        // matchStage
        MatchOperation matchStage = Aggregation.match(allCriteria);

        // sortStage
        SortOperation sortStage = sort(Sort.Direction.DESC, "created_date");
        if (!StringUtil.isNullOrEmpty(mappingVsatFilterDTO.getSort())) {
            String sortItem = mappingVsatFilterDTO.getSort();
            if (sortItem.substring(0, 1).equals("-")) {
                sortStage = sort(Sort.Direction.DESC, sortItem.substring(1));
            } else {
                sortStage = sort(Sort.Direction.ASC, sortItem);
            }
        }
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                project("uuid", "vsatDataSourceId", "vsatDataSourceName", "vsatIpAddress", "objectType", "objectId", "objectUuid", "objectName",
                        "createdBy", "createdDate", "modifiedBy", "modifiedDate"),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
        AggregationResults<MappingVsatResponseDTO> output = mongoOps.aggregate(aggregation, MappingVsatMetacen.class, MappingVsatResponseDTO.class);
        List<MappingVsatResponseDTO> results = output.getMappedResults();

        // total
        long total = mongoOps.count(Query.query(allCriteria).limit(-1).skip(-1), domain);

        return new PageImpl<>(results, pageable, total);
    }
}
