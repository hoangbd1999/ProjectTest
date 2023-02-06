package elcom.com.neo4j.dto;

import elcom.com.neo4j.node.Object;
import elcom.com.neo4j.node.Relationships;
import lombok.Data;

import java.util.List;

@Data
public class ResponseTopo {
    List<Object> nodes;
    List<Relationships> relationships;
}
