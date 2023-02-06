package com.elcom.metacen.mapping.data.repository;

import com.elcom.metacen.mapping.data.model.MappingAisMetacen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingAisMetacenRepository extends MongoRepository<MappingAisMetacen, String> {

    MappingAisMetacen findByUuid(String uuid);
}
