package elcom.com.neo4j.dto;

import elcom.com.neo4j.model.Topology;
import lombok.Data;

import java.util.List;

@Data
public class TopologyPageDto {
    private List<Topology> data;
    private Long size;
}
