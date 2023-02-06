package elcom.com.neo4j.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationshipLstDTO {
    private String destObjectId;
    private String destObjectType;
    private String fromTime;
    private String note;
    private Integer relationshipType;
    private String toTime;
    private String id;
    private String name;
}
