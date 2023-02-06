package elcom.com.neo4j.dto;

import lombok.Data;

import java.util.List;

@Data
public class FilterDto {
    String startTime;
    String endTime;
    String search;
    String ip;
    List<Integer> typeData;
    List<String> typeObject;
    List<Integer> dataSource;
    String ids;
    Integer page;
}
