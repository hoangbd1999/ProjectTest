package elcom.com.neo4j.dto;

import elcom.com.neo4j.node.LinkObject;
import elcom.com.neo4j.node.LinkRelationships;
import elcom.com.neo4j.node.Object;
import elcom.com.neo4j.node.Relationships;
import lombok.Data;

import java.util.List;

@Data
public class ResponseLinkTopo {
    List<LinkObject> nodes;
    List<LinkRelationships> relationships;
}
