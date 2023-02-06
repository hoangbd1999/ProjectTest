package com.elcom.metacen.contact.repository;

import com.elcom.metacen.dto.redis.Countries;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface CountriesRepository extends MongoRepository<Countries, Integer> {

    List<Countries> findAllByOrderByNameAsc();
}
