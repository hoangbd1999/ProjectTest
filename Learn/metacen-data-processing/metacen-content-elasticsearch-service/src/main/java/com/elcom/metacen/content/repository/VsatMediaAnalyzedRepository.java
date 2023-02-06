package com.elcom.metacen.content.repository;

import com.elcom.metacen.content.model.VsatMediaAnalyzed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VsatMediaAnalyzedRepository extends ElasticsearchRepository<VsatMediaAnalyzed, String> {

    List<VsatMediaAnalyzed> findByFileContentGB18030ContainsOrFileContentUtf8Contains(String textContentUTF8,String textContentGB);
    @Query("{ \"bool\" : { \"must\" : { \"term\" : { \"mediaTypeName\" : \"Audio\" } }, \"should\" : [ { \"term\" : { \"fileContentUtf8\" : \"*test*\" } }, { \"term\" : { \"fileContentGB18030\" : \"*test*\" } } ], \"minimum_should_match\" : 1 } }")
    List<VsatMediaAnalyzed> findTest();

    Optional<VsatMediaAnalyzed> findById(String id);

}
