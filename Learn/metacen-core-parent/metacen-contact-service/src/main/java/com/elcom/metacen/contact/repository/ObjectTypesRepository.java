package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectTypes;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author anhdv
 */
@Repository
public interface ObjectTypesRepository extends MongoRepository<ObjectTypes, Long> {

    List<ObjectTypes> findAllByIsDeletedOrderByTypeNameAsc(int isDeleted);

    ObjectTypes findByTypeNameAndTypeCodeAndIsDeleted(String typeName, String typeCode, int isDeleted);

    ObjectTypes findByTypeIdAndIsDeleted(String typeId, int isDeleted);

}
