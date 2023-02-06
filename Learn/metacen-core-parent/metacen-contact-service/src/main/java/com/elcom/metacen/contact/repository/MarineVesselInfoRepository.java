package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.MarineVesselInfo;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hoangbd
 */
@Repository
public interface MarineVesselInfoRepository extends MongoRepository<MarineVesselInfo, String> {

    MarineVesselInfo findByMmsiAndIsDeleted(long mmsi, int isDeleted);

    MarineVesselInfo findByUuidAndIsDeleted(String uuid, int isDeleted);

    List<MarineVesselInfo> findByUuidIn(List<String> uuidLst);
}
