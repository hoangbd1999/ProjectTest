/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.repositorymogo;

import elcom.com.neo4j.model.MappingVsatMetacen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface MappingVsatMetacenRepository extends MongoRepository<MappingVsatMetacen, String> {

    MappingVsatMetacen findByUuid(String uuid);
}
