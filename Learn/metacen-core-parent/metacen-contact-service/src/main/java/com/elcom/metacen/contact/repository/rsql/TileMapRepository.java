package com.elcom.metacen.contact.repository.rsql;

import com.elcom.metacen.contact.model.TileMap;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author Admin
 */
@Repository
public interface TileMapRepository extends MongoRepository<TileMap, String> {

    List<TileMap> findAllByOrderByNameAsc();
}
