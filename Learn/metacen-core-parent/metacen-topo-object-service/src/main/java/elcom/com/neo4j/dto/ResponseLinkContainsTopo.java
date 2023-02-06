package elcom.com.neo4j.dto;

import elcom.com.neo4j.node.LinkObject;
import elcom.com.neo4j.node.LinkRelationships;
import elcom.com.neo4j.node.ObjectRelationships;
import lombok.Data;

import java.util.List;

@Data
public class ResponseLinkContainsTopo {
    List<LinkObject> nodes;
    List<ObjectRelationships> relationships;
}
