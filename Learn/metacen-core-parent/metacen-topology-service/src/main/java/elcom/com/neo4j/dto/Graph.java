package elcom.com.neo4j.dto;

import lombok.Data;

@Data
public class Graph {
    private String startTime;
    private String endTime;
    private Integer type;
    private String ip;
    private Integer typeRelation;
}
