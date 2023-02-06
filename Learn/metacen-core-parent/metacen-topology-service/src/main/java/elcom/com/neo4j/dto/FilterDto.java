package elcom.com.neo4j.dto;

import lombok.Data;

import java.util.List;

@Data
public class FilterDto {
    String startTime;
    String endTime;
    Integer type;
    Integer typeRelation;
    String ips;
    List<Integer> typeData;
    List<Integer> dataSource;
    Integer deep;
    Boolean exactly;
    String ids;
    Integer page;
}
