package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectFiles;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



/**
 *
 * @author hoangbd
 */
@Repository
public interface ObjectFilesRepository extends CrudRepository<ObjectFiles, Long> {

    List<ObjectFiles> findAllByObjectId(String objectId);

    @Query(value = "select * from metacen_contact.object_files where id = ?1",nativeQuery = true)
    ObjectFiles findOne(long id);

    @Transactional
    @Modifying(
            clearAutomatically = true
    )
    @Query("UPDATE ObjectFiles oi SET oi.isDeleted = 1 WHERE oi.objectId = :objectId")
    int delete(@Param("objectId") String objectId);
}
