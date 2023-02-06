package elcom.com.neo4j.dto;

import lombok.Data;

@Data
public class AisValueSpark {
    private ObjectInfo src;
    private ObjectInfo dest;
    private Integer count;
    private String eventTime;
}
