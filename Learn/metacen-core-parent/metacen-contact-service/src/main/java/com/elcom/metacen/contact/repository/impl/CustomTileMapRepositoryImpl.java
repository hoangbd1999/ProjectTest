package com.elcom.metacen.contact.repository.impl;


import com.elcom.metacen.contact.model.TileMap;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapFilterDTO;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapResponseDTO;
import com.elcom.metacen.contact.repository.rsql.CustomTileMapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.List;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Component
public class CustomTileMapRepositoryImpl extends BaseCustomRepositoryImpl<TileMap> implements CustomTileMapRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTileMapRepositoryImpl.class);

    @Override
    public Page<TileMapResponseDTO> search(TileMapFilterDTO tileMapFilterDTO, Pageable pageable) {
        Criteria criteria;
        criteria = Criteria.where("name").regex(tileMapFilterDTO.getTerm(),"i");
        Criteria criteria1;
        criteria1 = Criteria.where("coordinates").regex(tileMapFilterDTO.getTerm(),"i");


        MatchOperation matchStage = Aggregation.match(criteria);
        MatchOperation matchStage1 = Aggregation.match(criteria1);

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                matchStage1,
                project("id", "name", "coordinates"),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
        AggregationResults<TileMapResponseDTO> output = mongoOps.aggregate(aggregation, TileMap.class, TileMapResponseDTO.class);
        List<TileMapResponseDTO> results = output.getMappedResults();

        // total
        long total = mongoOps.count(Query.query(criteria).limit(-1).skip(-1), domain);

        return new PageImpl<>(results, pageable, results.size());
    }

}

