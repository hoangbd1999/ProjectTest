package com.elcom.metacen.enrich.data.repository;

import com.elcom.metacen.enrich.data.model.VsatMediaAnalyzed;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VsatMediaAnalyzedRepository extends ElasticsearchRepository<VsatMediaAnalyzed, String> {

//    @Override
//    Optional<VsatMediaAnalyzed> findById(String id);
    Optional<VsatMediaAnalyzed> findById(String id);
}
