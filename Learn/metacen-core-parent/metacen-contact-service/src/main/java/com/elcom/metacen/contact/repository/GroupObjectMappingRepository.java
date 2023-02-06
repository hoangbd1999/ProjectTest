/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.GroupObjectMapping;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author hoangbd
 */
@Repository
public interface GroupObjectMappingRepository extends CrudRepository<GroupObjectMapping, Long> {

    List<GroupObjectMapping> findAllByGroupIdAndIsDeleted(UUID groupId, int isDeleted);
//
//    Page<People> findAll(Pageable pageable);
//
    @Transactional
    @Modifying(
            clearAutomatically = true
    )
    @Query("UPDATE GroupObjectMapping g SET g.isDeleted = 1 WHERE g.groupId = :groupId")
    int delete(@Param("groupId") UUID groupId);
}
