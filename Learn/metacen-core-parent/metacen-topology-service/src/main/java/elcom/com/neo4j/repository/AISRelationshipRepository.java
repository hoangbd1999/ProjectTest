package elcom.com.neo4j.repository;

import elcom.com.neo4j.node.AIS;
import elcom.com.neo4j.node.Object;
import elcom.com.neo4j.node.ObjectToNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AISRelationshipRepository extends Neo4jRepository<Object, String>{

//    @Query("MATCH (m:Object)<-[r:AIS]-(a:Object) RETURN r LIMIT 10")
//    List<Object> graph();
    @Query("MATCH (m:Object)<-[r:AIS]-(a:Object) RETURN m,r,a LIMIT 10")
    List<Object> graph();

}
