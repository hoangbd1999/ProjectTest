package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.Event;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hoangbd
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    Event findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<Event> findByUuidIn(List<String> uuidLst);
}
