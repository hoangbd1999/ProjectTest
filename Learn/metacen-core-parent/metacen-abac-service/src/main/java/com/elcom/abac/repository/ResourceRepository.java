package com.elcom.abac.repository;

import com.elcom.abac.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource,Integer> {

//    @Query(value = "select * from resources where urlpatterns_length = 29 order by urlpatterns_length desc ",nativeQuery = true)
    List<Resource> findAllByOrderByUrlPatternsLengthDesc();

    List<Resource> findByCodeIn(List<String> resourceCode);

}
