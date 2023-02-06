package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.AeroAirplaneInfo;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AeroAirplaneInfoRepository extends MongoRepository<AeroAirplaneInfo, String> {

    AeroAirplaneInfo findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<AeroAirplaneInfo> findByUuidIn(List<String> uuidLst);
}
