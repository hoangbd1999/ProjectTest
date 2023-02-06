package elcom.com.neo4j.dto;

import elcom.com.neo4j.node.Object;
import elcom.com.neo4j.node.Relationships;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MapRecordNeo4j {
    Map<String, Object> listNode ;
    Map<String,Integer> listNodeId;
    Map<String, Relationships> listRelationShipsAis ;
    Map<String, Relationships> listRelationShipsMedia ;
    Map<String,Integer> indexNode ;
    List<String> listKeyMedia ;
    List<String> listKeyAis ;
}
