package elcom.com.neo4j.dto;

import lombok.Data;
import org.apache.flink.types.Row;
//import org.apache.spark.sql.Row;

import java.util.List;

@Data
public class DataNeo4j {
    private String key;
    private List<Row>  medias;
}
