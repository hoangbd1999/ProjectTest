package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 *
 * @author hoangbd
 */
@Repository
public interface CommentsRepository extends CrudRepository<Comments, Long> {

    Optional<Comments> findByIdAndIsDeleted(Long id, int isDeleted);

    Page<Comments> findAllByIsDeleted(Pageable pageable,int isDeleted);

    @Transactional
    @Modifying(
            clearAutomatically = true
    )
    @Query("UPDATE Comments c SET c.isDeleted = 1 WHERE c.id = :id")
    int delete(@Param("id") Long id);
}
