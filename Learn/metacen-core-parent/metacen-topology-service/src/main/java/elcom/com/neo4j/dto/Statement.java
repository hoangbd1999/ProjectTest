package elcom.com.neo4j.dto;

import lombok.Data;

import java.util.List;

@Data
public class Statement {
    private String statement;
    private List<String> resultDataContents;

}
