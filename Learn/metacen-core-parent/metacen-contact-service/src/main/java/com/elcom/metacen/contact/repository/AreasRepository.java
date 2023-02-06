package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Areas;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreasRepository extends MongoRepository<Areas, String> {

    Areas findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<Areas> findByUuidIn(List<String> uuidLst);
}
