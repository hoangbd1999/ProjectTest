package com.elcom.metacen.contact.repository;

//import com.elcom.metacen.contact.model.VsatDataSource;
import com.elcom.metacen.dto.redis.VsatDataSource;
import com.elcom.metacen.dto.redis.VsatVesselType;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
//@Repository
//public interface VsatVesselTypeRepository extends MongoRepository<VsatVesselType, Integer> {
//
//    List<VsatVesselType> findAllByOrderByTypeCodeAsc();
//}

public interface VsatVesselTypeRepository extends MongoRepository<VsatDataSource, Integer> {

    List<VsatDataSource> findAllByOrderByDataSourceIdDesc();
}
