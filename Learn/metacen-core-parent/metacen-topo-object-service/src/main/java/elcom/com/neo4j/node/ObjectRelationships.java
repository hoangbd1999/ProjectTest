package elcom.com.neo4j.node;

import lombok.Data;

@Data
public class ObjectRelationships {
    private Integer start ;
    private Integer end ;
    private String startTime ;
    private String endTime ;
    private String uuidStart ;
    private String uuidEnd ;
    private String note ;
    private Integer page ;
    private Integer size;
    private String id;
    private Integer type;

}
